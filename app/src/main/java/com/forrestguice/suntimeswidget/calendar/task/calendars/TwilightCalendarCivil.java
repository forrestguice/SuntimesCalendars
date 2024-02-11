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
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTask;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_CIVIL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_CIVIL_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_ALT;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_AZ;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_DEC;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_RA;

@SuppressWarnings("Convert2Diamond")
public class TwilightCalendarCivil extends TwilightCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_CIVIL;
    private static final int resID_calendarTitle = R.string.calendar_civil_twilight_displayName;
    private static final int resID_calendarSummary = R.string.calendar_civil_twilight_summary;

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%cal", "%M @ %loc\n%eZ", "%loc");
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(s_CIVIL_TWILIGHT, s_CIVIL_TWILIGHT_MORNING, s_CIVIL_TWILIGHT_EVENING, s_SUNRISE, s_SUNSET, s_POLAR_TWILIGHT, s_WHITE_NIGHT);
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
            case 0: return s_CIVIL_TWILIGHT_MORNING;
            case 1: return s_CIVIL_TWILIGHT_EVENING;
            default: return "";
        }
    }

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);
        calendarTitle = context.getString(resID_calendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarDesc = null;
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettings settings, @NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTask task, @NonNull SuntimesCalendarTaskProgress progress0, @NonNull long[] window)
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
                        COLUMN_SUN_CIVIL_RISE, COLUMN_SUN_ACTUAL_RISE,   // 0, 1 .. civil rise, sunrise,
                        COLUMN_SUN_ACTUAL_SET, COLUMN_SUN_CIVIL_SET));   // 2, 3 .. sunset, civil set

                CalendarEventTemplate template = SuntimesCalendarSettings.loadPrefCalendarTemplate(context, calendarName, defaultTemplate());
                boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName, defaultFlags()).getValues();
                String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName, defaultStrings()).getValues();
                // 0:s_CIVIL_TWILIGHT, 1:s_CIVIL_TWILIGHT_MORNING, 2:s_CIVIL_TWILIGHT_EVENING, 3:s_SUNRISE, 4:s_SUNSET, 5:s_POLAR_TWILIGHT, 6:s_WHITE_NIGHT

                Map<TemplatePatterns, Boolean> containsPattern = new HashMap<>();
                Map<TemplatePatterns, Integer> i_pattern = new HashMap<>();
                buildContainsPattern(template, containsPattern);
                buildExtProjectionFromPatterns(containsPattern, i_pattern, projection0.size(), projection0,
                        COLUMN_SUN_CIVIL_RISE, COLUMN_SUN_CIVIL_SET);    // 4, 5  .. civil rise position, civil set position, etc

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
                        if (flags[0])
                        {
                            populatePatternData(containsPattern, i_pattern, cursor, 0, data);
                            createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 0, template, data, strings[1], strings[5], strings[0]);    // civil twilight (morning), polar twilight, civil twilight
                        }
                        if (flags[1]) {
                            populatePatternData(containsPattern, i_pattern, cursor, 1, data);
                            createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 2, template, data, strings[2], strings[6], strings[0]);   // civil twilight (evening), white night, civil twilight
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
