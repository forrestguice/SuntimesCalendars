/*
    Copyright (C) 2024 Forrest Guice
    This file is part of SuntimesCalendars.

    SuntimesCalendars is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesCalendars is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesCalendars.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.forrestguice.suntimeswidget.calendar.ui.template;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTask;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TemplatePreviewAdapter
 */
public class TemplatePreviewAdapter extends RecyclerView.Adapter<TemplatePreviewAdapter.CalendarEntryPreviewViewHolder>
{
    protected final WeakReference<Context> contextRef;
    protected String calendar;
    protected SuntimesCalendar calendarObj;
    protected CalendarEventTemplate template;
    protected SuntimesCalendarSettings settings;
    protected ArrayList<CalendarEntryPreviewItem> data = new ArrayList<>();

    public TemplatePreviewAdapter(Context context, String calendar, SuntimesCalendarSettings settings, CalendarEventTemplate template, String[] location)
    {
        this.contextRef = new WeakReference<>(context);
        this.calendar = calendar;
        this.settings = settings;
        this.calendarObj = new SuntimesCalendarFactory().createCalendar(context, SuntimesCalendarDescriptor.getDescriptor(context, calendar), getSettings());
        this.template = template;
        this.location = location;
        //initData(context);
    }

    /**
     * initializes array of CalendarEntryPreviewItem
     * @param context context
     */
    public void initData(Context context)
    {
        data.clear();

        final Handler handler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Context context = contextRef.get();

                final SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(context.getContentResolver(), new String[] { calendarObj.calendarName() });
                SuntimesCalendarTaskInterface task = new SuntimesCalendarTask(context);
                task.queryConfig();

                long[] defWindow = calendarObj.defaultWindow();
                long[] window = task.getWindow(defWindow[0], defWindow[1]); //task.getWindow(0, 0);
                SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(0, 1, "");
                SuntimesCalendarSettings settings = new SuntimesCalendarSettings()
                {
                    @NonNull @Override
                    public CalendarEventTemplate loadPrefCalendarTemplate(Context context, String calendar, @NonNull CalendarEventTemplate defaultTemplate) {
                        return template;
                    }
                };

                SuntimesCalendar.CalendarInitializer initializer = new SuntimesCalendar.CalendarInitializer()
                {
                    @Override
                    public long calendarID() {
                        return 10;
                    }

                    @Override
                    public boolean onStarted()
                    {
                        if (adapterListener != null) {
                            adapterListener.onStartLoading();
                        }
                        return true;
                    }

                    @Override
                    public void processEventValues(ContentValues... eventValues) {
                        for (ContentValues values : eventValues) {
                            data.add(new CalendarEntryPreviewItem(values));
                        }
                    }

                    @Override
                    public void onFinished()
                    {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                                if (adapterListener != null) {
                                    adapterListener.onLoaded();
                                }
                            }
                        });
                    }
                };
                calendarObj.initCalendar(settings, adapter, task, progress, window, initializer);
            }
        });
    }

    // an alternate implementation storing TemplatePreviewItems instead
    /*protected void initData(Context context)
    {
        CalendarEventStrings eventStrings = calendarObj.defaultStrings();
        for (int i=0; i<eventStrings.getCount(); i++)
        {
            String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarObj.calendarName(), calendarObj.defaultStrings()).getValues();
            ContentValues values = createMockValues(calendarObj, location, strings[i]);
            data.add(new TemplatePreviewItem(calendarObj, template, i, location, values));
        }
    }*/

    public SuntimesCalendarSettings getSettings() {
        return settings;
    }

    public void setCalendar(Context context, String value)
    {
        calendar = value;
        initData(context);
        notifyDataSetChanged();
    }
    public String getCalendar() {
        return calendar;
    }

    public void setTemplate(Context context, CalendarEventTemplate value)
    {
        template = value;
        initData(context);
        notifyDataSetChanged();
    }
    public CalendarEventTemplate getTemplate() {
        return template;
    }

    public void setLocation(Context context, String[] value)
    {
        location = value;
        initData(context);
        notifyDataSetChanged();
    }
    public String[] getLocation() {
        return location;
    }
    protected String[] location;

    @NonNull
    @Override
    public CalendarEntryPreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(CalendarEntryPreviewViewHolder.getSuggestedLayoutID(), parent, false);
        return new CalendarEntryPreviewViewHolder(contextRef.get(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarEntryPreviewViewHolder holder, int position)
    {
        Context context = (contextRef != null ? contextRef.get() : null);
        if (context == null) {
            Log.w("TemplatePreviewHolder", "onBindViewHolder: null context!");
            return;
        }
        if (holder == null) {
            Log.w("TemplatePreviewHolder", "onBindViewHolder: null view holder!");
            return;
        }
        holder.bindDataToPosition(context, position, data.get(position));
        attachListeners(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull CalendarEntryPreviewViewHolder holder) {
        detachListeners(holder);
    }

    @Override
    public int getItemCount() {
        return (data != null ? data.size() : 0);
    }

    private void attachListeners(final CalendarEntryPreviewViewHolder holder, final int position) {
    }
    private void detachListeners(final CalendarEntryPreviewViewHolder holder) {
    }

    /**
     * AdapterListener
     */
    public static abstract class AdapterListener {
        public void onStartLoading() {}
        public void onLoaded() {}
    }

    public AdapterListener adapterListener = null;
    public void setAdapterListener( AdapterListener listener ) {
        this.adapterListener = listener;
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
     * CalendarEntryPreviewItem
     */
    protected static final class CalendarEntryPreviewItem
    {
        public CalendarEntryPreviewItem(ContentValues values) {
            this.values = values;
        }

        protected ContentValues values;
        public ContentValues getValues() {
            return values;
        }
    }

    /**
     * CalendarEntryPreviewViewHolder
     */
    public static class CalendarEntryPreviewViewHolder extends RecyclerView.ViewHolder
    {
        public TextView text_name;
        public TextView text_location;
        public TextView text_description;
        public TextView text_time, text_date, text_month, text_day;
        private final Utils utils = new Utils();

        public static int getSuggestedLayoutID() {
            return R.layout.layout_item_calendar_day;
        }

        public CalendarEntryPreviewViewHolder(Context context, View itemView)
        {
            super(itemView);
            text_name = (TextView) itemView.findViewById(R.id.text_name);
            text_time = (TextView) itemView.findViewById(R.id.text_time);
            text_date = (TextView) itemView.findViewById(R.id.text_date);
            text_day = (TextView) itemView.findViewById(R.id.text_day);
            text_month = (TextView) itemView.findViewById(R.id.text_month);
            text_location = (TextView) itemView.findViewById(R.id.text_location);
            text_description = (TextView) itemView.findViewById(R.id.text_description);
        }

        public void bindDataToPosition(Context context, int position, CalendarEntryPreviewItem item)
        {
            ContentValues values = item.getValues();
            if (values != null)
            {
                text_name.setText(values.getAsString(CalendarContract.Events.TITLE));
                text_description.setText(values.getAsString(CalendarContract.Events.DESCRIPTION));
                text_location.setText(values.getAsString(CalendarContract.Events.EVENT_LOCATION));
                text_location.setVisibility(text_location.getText().toString().isEmpty() ? View.GONE : View.VISIBLE);

                long startMillis = values.getAsLong(CalendarContract.Events.DTSTART);

                if (text_time != null) {
                    text_time.setText(getTimeDisplay(context, startMillis, values.getAsLong(CalendarContract.Events.DTEND)));
                }

                Calendar now = Calendar.getInstance();
                Calendar eventStart = Calendar.getInstance();
                eventStart.setTimeInMillis(startMillis);

                if (text_date != null) {
                    text_date.setText(getDateDisplay(context, eventStart));
                    text_date.setVisibility((eventStart.get(Calendar.DATE) != now.get(Calendar.DATE)) ? View.VISIBLE : View.GONE);
                }

                if (text_month != null) {
                    String monthName = utils.monthDisplayString(context, eventStart);
                    /*if (Build.VERSION.SDK_INT >= 26) {
                        monthName = Month.of(eventStart.get(Calendar.MONTH)+1).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
                    }*/
                    text_month.setText(monthName);
                }
                if (text_day != null) {
                    text_day.setText("" + eventStart.get(Calendar.DAY_OF_MONTH));
                }
            }
        }

        protected CharSequence getDateDisplay(Context context, Calendar calendar) {

            DateFormat format = android.text.format.DateFormat.getLongDateFormat(context);
            format.setTimeZone(calendar.getTimeZone());
            return format.format(calendar.getTime());
        }

        protected CharSequence getTimeDisplay(Context context, long millisStart, long millisEnd)
        {
            Calendar eventStart = Calendar.getInstance();
            eventStart.setTimeInMillis(millisStart);

            if (millisStart == millisEnd) {
                return context.getString(R.string.timespan_format0, timeDisplayString(context, eventStart));

            } else {
                Calendar eventEnd = Calendar.getInstance();
                eventEnd.setTimeInMillis(millisEnd);
                return context.getString(R.string.timespan_format1, timeDisplayString(context, eventStart), timeDisplayString(context, eventEnd));
            }
        }

        protected String timeDisplayString(Context context, @NonNull Calendar calendar)
        {
            //String mode = UnitSettings.loadTimeFormatPref(context);
            //switch (mode) {
            //    case UnitSettings.TIMEFORMAT_12: return utils.calendarTime12DisplayString(context, calendar);
            //    case UnitSettings.TIMEFORMAT_24: return utils.calendarTime24DisplayString(context, calendar);
            //    default: return utils.calendarTimeSysDisplayString(context, calendar);
            //}
            return utils.calendarTimeSysDisplayString(context, calendar);    // TODO
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
     * TemplatePreviewItem
     */
    @Deprecated
    protected static final class TemplatePreviewItem
    {
        public TemplatePreviewItem(SuntimesCalendar calendar, CalendarEventTemplate template, int eventIndex, String[] location, ContentValues values)
        {
            this.calendar = calendar;
            this.template = template;
            this.eventIndex = eventIndex;
            this.location = location;
            this.values = values;
        }

        protected SuntimesCalendar calendar;
        public SuntimesCalendar getCalendar() {
            return calendar;
        }

        protected CalendarEventTemplate template;
        public CalendarEventTemplate getTemplate() {
            return template;
        }

        public String[] getLocation() {
            return location;
        }
        protected String[] location;

        protected int eventIndex;
        public int getEventIndex() {
            return eventIndex;
        }

        protected ContentValues values;
        public ContentValues getValues() {
            return values;
        }
    }

    /**
     * TemplatePreviewViewHolder
     */
    @Deprecated
    public static class TemplatePreviewViewHolder extends RecyclerView.ViewHolder
    {
        public TextView text_name;
        public TextView text_location;
        public TextView text_description;

        public static int getSuggestedLayoutID() {
            return R.layout.layout_item_calendar_event;
        }

        public TemplatePreviewViewHolder(Context context, View itemView)
        {
            super(itemView);
            text_name = (TextView) itemView.findViewById(R.id.text_name);
            text_location = (TextView) itemView.findViewById(R.id.text_location);
            text_description = (TextView) itemView.findViewById(R.id.text_description);
        }

        public void bindDataToPosition(Context context, int position, TemplatePreviewItem item)
        {
            CalendarEventTemplate template = item.getTemplate();
            if (template != null)
            {
                text_name.setText(template.getTitle(item.getValues()));
                text_location.setText(template.getLocation(item.getValues()));
                text_description.setText(template.getDesc(item.getValues()));
                text_location.setVisibility(text_location.getText().toString().isEmpty() ? View.GONE : View.VISIBLE);

            } else {
                text_name.setText("");
                text_location.setText("");
                text_description.setText("");
            }
        }
    }

    protected ContentValues createMockDataValues(SuntimesCalendar calendar, String[] location, String eventLabel)
    {
        ContentValues data = TemplatePatterns.createContentValues(null, calendar);
        data = TemplatePatterns.createContentValues(data, location);
        data.put(TemplatePatterns.pattern_em.getPattern(), Calendar.getInstance().getTimeInMillis());
        data.put(TemplatePatterns.pattern_event.getPattern(), eventLabel);
        data.put(TemplatePatterns.pattern_phase.getPattern(), "Full Moon");

        double direction = 263.78;
        double elevation = -6.0;
        double rightAscension = 10;
        double declination = 23.5;
        double distance = 252086.52;
        double illum = .51;

        data.put(TemplatePatterns.pattern_eZ.getPattern(), Utils.formatAsDirection(direction, 2));
        data.put(TemplatePatterns.pattern_eA.getPattern(), Utils.formatAsElevation(elevation, 2));
        data.put(TemplatePatterns.pattern_eR.getPattern(), Utils.formatAsRightAscension(rightAscension, 1));
        data.put(TemplatePatterns.pattern_eD.getPattern(), Utils.formatAsDeclination(declination, 1));
        data.put(TemplatePatterns.pattern_dist.getPattern(), Utils.formatAsDistanceKm(distance, 1));
        data.put(TemplatePatterns.pattern_illum.getPattern(), Utils.formatAsPercent(illum, 1));

        return data;
    }

}


