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
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ASTRO_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ASTRO_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_NAUTICAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_NAUTICAL_SET;

@SuppressWarnings("Convert2Diamond")
public class TwilightCalendarAstro extends TwilightCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_ASTRO;
    private static final int resID_calendarTitle = R.string.calendar_astronomical_twilight_displayName;
    private static final int resID_calendarSummary = R.string.calendar_astronomical_twilight_summary;

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
        return new CalendarEventStrings(s_ASTRO_TWILIGHT, s_ASTRO_TWILIGHT_MORNING, s_ASTRO_TWILIGHT_EVENING, s_ASTRO_DAWN, s_ASTRO_DUSK, s_NAUTICAL_NIGHT);
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
            case 0: return s_ASTRO_TWILIGHT_MORNING;
            case 1: return s_ASTRO_TWILIGHT_EVENING;
            default: return "";
        }
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
                ArrayList<String> projection0 = new ArrayList<>(Arrays.asList(
                        COLUMN_SUN_ASTRO_RISE, COLUMN_SUN_NAUTICAL_RISE,   // 0, 1
                        COLUMN_SUN_NAUTICAL_SET, COLUMN_SUN_ASTRO_SET ));  // 2, 3

                CalendarEventTemplate template = settings.loadPrefCalendarTemplate(context, calendarName(), defaultTemplate());
                boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName(), defaultFlags()).getValues();
                String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName(), defaultStrings()).getValues();
                // 0:s_ASTRO_TWILIGHT, 1:s_ASTRO_TWILIGHT_MORNING, 2:s_ASTRO_TWILIGHT_EVENING, 3:s_ASTRO_DAWN, 4:s_ASTRO_DUSK, 5:s_NAUTICAL_NIGHT

                Map<TemplatePatterns, Boolean> containsPattern = new HashMap<>();
                Map<TemplatePatterns, Integer> i_pattern = new HashMap<>();
                buildContainsPattern(template, containsPattern);
                buildExtProjectionFromPatterns(containsPattern, i_pattern, projection0.size(), projection0,
                        COLUMN_SUN_ASTRO_RISE, COLUMN_SUN_ASTRO_SET);    // 4, 5  .. rising position, setting position, etc

                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + window[0] + "-" + window[1]);
                String[] projection = projection0.toArray(new String[0]);
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    String[] location = task.getLocation();
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName(), SuntimesCalendarSettings.NOTE_LOCATION_NAME, location[0]);

                    int c = 0;
                    int totalProgress = cursor.getCount();
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, location[0]);
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
                            createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 0, template, data, strings[1], strings[5], strings[0]);   // dawn, nautical night, astro twilight
                        }
                        if (flags[1]) {
                            populatePatternData(containsPattern, i_pattern, cursor, 1, data);
                            createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 2, template, data, strings[2], strings[0], strings[0]);   // dusk, astro twilight, astro twilight
                        }
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast()) {
                            listener.processEventValues( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || cursor.isLast()) {
                            progress.setProgress(c, totalProgress, progressTitle);
                            task.publishProgress(progress0, progress);
                        }
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

    @Override
    public int priority() {
        return 3;
    }

}
