/**
    Copyright (C) 2018-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar.task.calendars;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;

import com.forrestguice.suntimeswidget.calendar.CalendarEventFlags;
import com.forrestguice.suntimeswidget.calendar.CalendarEventStrings;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.CalendarGroups;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class SolsticeCalendar extends SuntimesCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_SOLSTICE;
    private static final int resID_calendarTitle = R.string.calendar_solstice_displayName;
    private static final int resID_calendarSummary = R.string.calendar_solstice_summary;

    private final String[] displayStrings = new String[8];  // {spring, cross-spring, summer, cross-summer, fall, cross-fall, winter, cross-winter}
    private final String[] projection = new String[]
    {
            CalculatorProviderContract.COLUMN_SEASON_VERNAL,          // 0
            CalculatorProviderContract.COLUMN_SEASON_CROSS_SPRING,    // 1

            CalculatorProviderContract.COLUMN_SEASON_SUMMER,          // 2
            CalculatorProviderContract.COLUMN_SEASON_CROSS_SUMMER,    // 3

            CalculatorProviderContract.COLUMN_SEASON_AUTUMN,          // 4
            CalculatorProviderContract.COLUMN_SEASON_CROSS_AUTUMN,    // 5

            CalculatorProviderContract.COLUMN_SEASON_WINTER,          // 6
            CalculatorProviderContract.COLUMN_SEASON_CROSS_WINTER,    // 7
    };

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%M", "%M", null);
    }

    @Override
    public TemplatePatterns[] supportedPatterns()
    {
        return new TemplatePatterns[] {
                TemplatePatterns.pattern_event, null, //TemplatePatterns.pattern_eZ, TemplatePatterns.pattern_eA, TemplatePatterns.pattern_eD, TemplatePatterns.pattern_eR, null,
                TemplatePatterns.pattern_loc, TemplatePatterns.pattern_lat, TemplatePatterns.pattern_lon, TemplatePatterns.pattern_lel, null,
                TemplatePatterns.pattern_cal, TemplatePatterns.pattern_summary, TemplatePatterns.pattern_color, TemplatePatterns.pattern_percent
        };
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(displayStrings);
    }

    @Override
    public CalendarEventFlags defaultFlags()
    {
        boolean[] values = new boolean[displayStrings.length];
        Arrays.fill(values, true);
        return new CalendarEventFlags(values);
    }

    @Override
    public String flagLabel(int i) {
        if (i >=0 && i < displayStrings.length) {
            return displayStrings[i];
        } else return "";
    }

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);

        defaultCalendarTitle = context.getString(resID_calendarTitle);
        calendarTitle = settings.loadPrefCalendarTitle(context, calendarName(), defaultCalendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarDesc = null;
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());

        displayStrings[0] = context.getString(R.string.timeMode_equinox_vernal);
        displayStrings[1] = context.getString(R.string.timeMode_cross_spring);

        displayStrings[2] = context.getString(R.string.timeMode_solstice_summer);
        displayStrings[3] = context.getString(R.string.timeMode_cross_summer);

        displayStrings[4] = context.getString(R.string.timeMode_equinox_autumnal);
        displayStrings[5] = context.getString(R.string.timeMode_cross_autumnal);

        displayStrings[6] = context.getString(R.string.timeMode_solstice_winter);
        displayStrings[7] = context.getString(R.string.timeMode_cross_winter);
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettings settings, @NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgress progress0, @NonNull long[] window, @NonNull CalendarInitializer listener)
    {
        if (task.isCancelled()) {
            return false;
        }
        if (!listener.onStarted()) {
            return false;
        }

        long calendarID = listener.calendarID();
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                int versionCode = queryProviderVersionCode(resolver);

                Calendar startDate = Calendar.getInstance();
                startDate.setTimeInMillis(window[0]);

                Calendar endDate = Calendar.getInstance();
                endDate.setTimeInMillis(window[1]);

                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SEASONS + "/" + startDate.get(Calendar.YEAR) + "-" + endDate.get(Calendar.YEAR));
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();

                    int c = 0;
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = task.createProgressObj(c, totalProgress, calendarTitle);
                    task.publishProgress(progress0, progress);

                    boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName(), defaultFlags()).getValues();
                    String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName(), defaultStrings()).getValues();
                    CalendarEventTemplate template = settings.loadPrefCalendarTemplate(context, calendarName(), defaultTemplate());
                    ContentValues data = TemplatePatterns.createContentValues(null, this);
                    data = TemplatePatterns.createContentValues(data, task.getLocation());

                    Calendar eventTime;
                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        for (int i=0; i<projection.length; i++)
                        {
                            if (flags[i] && !cursor.isNull(i))
                            {
                                data.put(TemplatePatterns.pattern_event.getPattern(), strings[i]);
                                eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(cursor.getLong( toLegacyProjection(i, versionCode) ));
                                eventValues.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventTime));
                            }
                        }
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast())
                        {
                            listener.processEventValues( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        progress.setProgress(c, totalProgress, calendarTitle);
                        task.publishProgress(progress0, progress);
                    }
                    cursor.close();
                    listener.onFinished();
                    return !task.isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.e(getClass().getSimpleName(), lastError);
                    return false;
                }
            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(getClass().getSimpleName(), lastError);
                return false;
            }
        } else return false;
    }

    protected int queryProviderVersionCode(@NonNull ContentResolver resolver)
    {
        int versionCode = 0;
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG);
        Cursor cursor = resolver.query(uri, new String[] { CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE }, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            if (!cursor.isAfterLast() && !cursor.isNull(0)) {
                versionCode = cursor.getInt(0);
            }
            cursor.close();
        }
        return versionCode;
    }

    /**
     * necessary because v0.5.0 (5) and earlier mistakenly swaps the cross-quarter values.
     * @param providerVersionCode provider version int
     * @param i legacy index
     * @return remapped index
     */
    protected int toLegacyProjection(int i, int providerVersionCode)
    {
        if (providerVersionCode <= 5)
        {
            switch (i)
            {
                case 1: return 3;    // cross-spring <- cross-summer
                case 3: return 5;    // cross-summer <- cross-fall
                case 5: return 7;    // cross-autumn <- cross-winter
                case 7: return 1;    // cross-winter <- cross-spring
                case 0: case 2: case 4: case 6: default: return i;   // unchanged: spring, summer, autumn, winter, others
            }
        } else
            return i;
    }

    @Override
    public String[] getGroups() {
        return new String[] { CalendarGroups.GROUP_SOLSTICE };
    }

    @Override
    public int priority() {
        return 9;
    }

    @Override
    public long[] defaultWindow() {
        return new long[] {1000L * 60 * 60 * 24, 1000L * 60 * 60 * 24 * 365};    // 1 year
    }
}
