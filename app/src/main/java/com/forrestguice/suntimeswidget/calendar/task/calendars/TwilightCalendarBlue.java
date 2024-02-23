/**
    Copyright (C) 2023 Forrest Guice
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
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.task.CalendarGroups;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_BLUE8_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_BLUE8_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_GOLDEN_EVENING;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_GOLDEN_MORNING;

@SuppressWarnings("Convert2Diamond")
public class TwilightCalendarBlue extends TwilightCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_BLUE;
    private static final int resID_calendarTitle = R.string.calendar_blue_twilight_displayName;
    private static final int resID_calendarSummary = R.string.calendar_blue_twilight_summary;

    protected String s_BLUE_HOUR, s_BLUE_HOUR_MORNING, s_BLUE_HOUR_EVENING;

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%cal", "%M @ %loc\n%eZ", "%loc");
    }

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);

        calendarTitle = context.getString(resID_calendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarDesc = null;
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());

        s_BLUE_HOUR_MORNING = context.getString(R.string.timeMode_blue_morning);
        s_BLUE_HOUR_EVENING = context.getString(R.string.timeMode_blue_evening);
        s_BLUE_HOUR = context.getString(R.string.calendar_blue_twilight_displayName);
    }

    @Override
    public CalendarEventFlags defaultFlags()
    {
        boolean[] values = new boolean[2];
        Arrays.fill(values, true);
        return new CalendarEventFlags(values);
    }

    @Override
    public String flagLabel(int i)
    {
        switch (i) {
            case 0: return s_BLUE_HOUR_MORNING;
            case 1: return s_BLUE_HOUR_EVENING;
            default: return "";
        }
    }

    @Override
    public String[] getGroups() {
        return new String[] { CalendarGroups.GROUP_BLUEGOLD };
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(s_BLUE_HOUR_MORNING, s_BLUE_HOUR_EVENING, s_BLUE_HOUR);
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettings settings, @NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgress progress0, @NonNull long[] window)
    {
        if (task.isCancelled()) {
            return false;
        }

        String calendarName = calendarName();
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColor);
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                ArrayList<String> projection0 = new ArrayList<>(Arrays.asList(
                        CalculatorProviderContract.COLUMN_SUN_BLUE8_RISE, CalculatorProviderContract.COLUMN_SUN_BLUE4_RISE,  // 0, 1
                        CalculatorProviderContract.COLUMN_SUN_BLUE4_SET, CalculatorProviderContract.COLUMN_SUN_BLUE8_SET )); // 2, 3 .. expected order: blue8 (morning), blue4 (morning), blue4 (evening), blue8 (evening)

                CalendarEventTemplate template = SuntimesCalendarSettings.loadPrefCalendarTemplate(context, calendarName, defaultTemplate());
                boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName, defaultFlags()).getValues();    // TODO
                String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName, defaultStrings()).getValues();    // 0:s_BLUE_HOUR_MORNING, 1:s_BLUE_HOUR_EVENING, 2:s_BLUE_HOUR

                Map<TemplatePatterns, Boolean> containsPattern = new HashMap<>();
                Map<TemplatePatterns, Integer> i_pattern = new HashMap<>();
                buildContainsPattern(template, containsPattern);
                buildExtProjectionFromPatterns(containsPattern, i_pattern, projection0.size(), projection0,
                        COLUMN_SUN_BLUE8_RISE, COLUMN_SUN_BLUE8_SET);    // 4, 5  .. rising position, setting position, etc

                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + window[0] + "-" + window[1]);
                String[] projection = projection0.toArray(new String[0]);
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    String[] location = task.getLocation();
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, location[0]);

                    int c = 0;
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, location[0]);
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, progressTitle);
                    task.publishProgress(progress0, progress);

                    ContentValues data = TemplatePatterns.createContentValues(null, this);
                    data = TemplatePatterns.createContentValues(data, task.getLocation());

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        if (flags[0]) {
                            populatePatternData(containsPattern, i_pattern, cursor, 0, data);
                            createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 0, template, data, strings[0], strings[0], strings[2]);    // blue8 (morning), blue4 (morning)
                        }
                        if (flags[1]) {
                            populatePatternData(containsPattern, i_pattern, cursor, 1, data);
                            createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 2, template, data, strings[1], strings[1], strings[2]);    // blue4 (evening), blue8 (evening)
                        }
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast()) {
                            adapter.createCalendarEvents( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || cursor.isLast()) {
                            progress.setProgress(c, totalProgress, progressTitle);
                            task.publishProgress(progress0, progress);
                        }
                    }
                    cursor.close();
                    createCalendarReminders(context, task, progress0);
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

}
