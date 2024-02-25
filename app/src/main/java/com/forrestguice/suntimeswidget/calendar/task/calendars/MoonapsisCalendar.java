/**
    Copyright (C) 2018-2024 Forrest Guice
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
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class MoonapsisCalendar extends MoonCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_MOONAPSIS;
    private static final int resID_calendarTitle = R.string.calendar_moonApsis_displayName;
    private static final int resID_calendarSummary = R.string.calendar_moonApsis_summary;

    private String[] apsisStrings = new String[2];    // {apogee, perigee}

    @Override
    public String calendarName() {
        return SuntimesCalendarAdapter.CALENDAR_MOONAPSIS;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%M", "%M\n%dist", null);
    }

    @Override
    public TemplatePatterns[] supportedPatterns()
    {
        return new TemplatePatterns[] {
                TemplatePatterns.pattern_event, null, //TemplatePatterns.pattern_eZ, TemplatePatterns.pattern_eA, TemplatePatterns.pattern_eD, TemplatePatterns.pattern_eR, null,  // TODO: position patterns
                //TemplatePatterns.pattern_illum,    // TODO: illum pattern
                TemplatePatterns.pattern_dist, null,
                TemplatePatterns.pattern_loc, TemplatePatterns.pattern_lat, TemplatePatterns.pattern_lon, TemplatePatterns.pattern_lel, null,
                TemplatePatterns.pattern_cal, TemplatePatterns.pattern_summary, TemplatePatterns.pattern_color, TemplatePatterns.pattern_percent
        };
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(apsisStrings);
    }

    @Override
    public CalendarEventFlags defaultFlags()
    {
        boolean[] values = new boolean[apsisStrings.length];
        Arrays.fill(values, true);
        return new CalendarEventFlags(values);
    }

    @Override
    public String flagLabel(int i) {
        if (i >= 0 && i < apsisStrings.length) {
            return apsisStrings[i];
        } else return "";
    }

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);

        calendarTitle = settings.loadPrefCalendarTitle(context, calendarName(), context.getString(resID_calendarTitle));
        calendarSummary = context.getString(resID_calendarSummary);
        calendarDesc = null;
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());

        apsisStrings[0] = context.getString(R.string.timeMode_moon_apogee);
        apsisStrings[1] = context.getString(R.string.timeMode_moon_perigee);
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettings settings, @NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgress progress0, @NonNull long[] window)
    {
        if (task.isCancelled()) {
            return false;
        }

        String calendarName = calendarName();

        //Log.d("DEBUG", "providerVersion: " + task.getProviderVersion());

        if (task.getProviderVersion() < 2)    // sanity check.. moonApsis needs provider v2:0.3.0 (Suntimes v0.12.0+))
        {
            Context context = contextRef.get();
            lastError = context != null ? context.getString(R.string.feature_not_supported_by_provider, calendarTitle, "Suntimes v0.12.0")
                    : calendarTitle + " is not supported by the current version; requires Suntimes v0.12.0 or greater";
            Log.e("initMoonApsisCalendar", lastError);
            return false;
        }

        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColor);
        } else return false;

        String[] projection = new String[] {
                CalculatorProviderContract.COLUMN_MOONPOS_APOGEE,
                CalculatorProviderContract.COLUMN_MOONPOS_PERIGEE
        };

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                int c = 0;
                int totalProgress = (int)Math.ceil(1.25 * (((window[1] - window[0]) / 1000d / 60d / 60d / 24d) / 27.554551d));
                SuntimesCalendarTaskProgress progress = task.createProgressObj(c, totalProgress, calendarTitle);
                task.publishProgress(progress0, progress);

                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(window[0]);

                Calendar endDate = Calendar.getInstance();
                endDate.setTimeInMillis(window[1]);

                while (date.before(endDate) && !task.isCancelled())
                {
                    Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPOS  + "/" + (date.getTimeInMillis()));
                    Cursor cursor = resolver.query(uri, projection, null, null, null);
                    if (cursor == null)
                    {
                        lastError = "Failed to resolve URI! " + uri;
                        Log.w(getClass().getSimpleName(), lastError);
                        return false;

                    } else {
                        progress = task.createProgressObj(c, totalProgress, calendarTitle);
                        task.publishProgress(progress0, progress);

                        boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName, defaultFlags()).getValues();
                        String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName, defaultStrings()).getValues();
                        CalendarEventTemplate template = SuntimesCalendarSettings.loadPrefCalendarTemplate(context, calendarName, defaultTemplate());
                        ContentValues data = TemplatePatterns.createContentValues(null, this);
                        data = TemplatePatterns.createContentValues(data, task.getLocation());

                        ArrayList<ContentValues> eventValues = new ArrayList<>();
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast() && !task.isCancelled())
                        {
                            if (cursor.getColumnCount() < 2 || cursor.getLong(0) <= 0)
                            {   // sanity check.. moonApsis needs provider v2:0.3.0 (Suntimes v0.12.0+))
                                cursor.close();
                                progress.setProgress(totalProgress, totalProgress, calendarTitle);
                                task.publishProgress(progress0, progress);
                                lastError = context.getString(R.string.feature_not_supported_by_provider, calendarTitle, "Suntimes v0.12.0");
                                Log.e("initMoonApsisCalendar", lastError);
                                return false;
                            }

                            for (int i=0; i<2; i++)
                            {
                                if (!flags[i]) {
                                    continue;
                                }

                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(cursor.getLong(i));
                                double distance = lookupMoonDistance(context, resolver, eventTime.getTimeInMillis());
                                String distanceString = ((distance > 0) ? Utils.formatAsDistance(task.getLengthUnits(), distance, 1) : "");

                                data.put(TemplatePatterns.pattern_event.getPattern(), strings[i]);
                                data.put(TemplatePatterns.pattern_dist.getPattern(), distanceString);
                                eventValues.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventTime));
                            }
                            date.setTimeInMillis(cursor.getLong(0) + (60 * 1000));  // advance to next cycle
                            cursor.moveToNext();
                            c++;

                            progress.setProgress(c, totalProgress, calendarTitle);
                            task.publishProgress(progress0, progress);
                        }
                        cursor.close();

                        int chunk = 128;
                        int n = eventValues.size();
                        for (int i = 0; i < n; i += chunk) {
                            int j = i + chunk;
                            adapter.createCalendarEvents(eventValues.subList(i, (j > n ? n : j)).toArray(new ContentValues[0]));
                        }
                    }
                }
                createCalendarReminders(context, task, progress0);
                return !task.isCancelled();

            } else {
                lastError = "Unable to getContentResolver!";
                Log.e("initMoonApsisCalendar", lastError);
                return false;
            }
        } else return false;
    }

    @Override
    public int priority() {
        return 8;
    }

}
