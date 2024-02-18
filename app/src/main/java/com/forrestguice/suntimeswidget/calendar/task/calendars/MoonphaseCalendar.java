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
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FIRST;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FIRST_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FULL;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FULL_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_NEW;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_NEW_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_THIRD;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_THIRD_DISTANCE;

@SuppressWarnings("Convert2Diamond")
public class MoonphaseCalendar extends MoonCalendarBase
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_MOONPHASE;
    private static final int resID_calendarTitle = R.string.calendar_moonPhase_displayName;
    private static final int resID_calendarSummary = R.string.calendar_moonPhase_summary;

    public static final double THRESHHOLD_SUPERMOON = 360000;    // km
    public static final double THRESHHOLD_MICROMOON = 405000;    // km

    private final String[] phaseStrings = new String[4];     // {major phases}
    private final String[] phaseStrings1 = new String[4];    // {major phases; supermoon}
    private final String[] phaseStrings2 = new String[4];    // {major phases; micromoon}

    @Override
    public String calendarName() {
        return SuntimesCalendarAdapter.CALENDAR_MOONPHASE;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%M", "%M\n%dist", null);
    }

    @Override
    public TemplatePatterns[] supportedPatterns()
    {
        return new TemplatePatterns[] {
                TemplatePatterns.pattern_event, null, // TemplatePatterns.pattern_eZ, TemplatePatterns.pattern_eA, TemplatePatterns.pattern_eD, TemplatePatterns.pattern_eR, null,   // TODO: position patterns
                //TemplatePatterns.pattern_illum,  // TODO: illum pattern
                TemplatePatterns.pattern_dist, null,
                TemplatePatterns.pattern_loc, TemplatePatterns.pattern_lat, TemplatePatterns.pattern_lon, TemplatePatterns.pattern_lel, null,
                TemplatePatterns.pattern_cal, TemplatePatterns.pattern_summary, TemplatePatterns.pattern_color, TemplatePatterns.pattern_percent
        };
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(phaseStrings[0], phaseStrings[1], phaseStrings[2], phaseStrings[3],   // 0-3 normal phases
                phaseStrings1[0], phaseStrings1[2],                                                           // 4,5 super moon
                phaseStrings2[0], phaseStrings2[2]                                                            // 6,7 micro moon
        );
    }

    @Override
    public CalendarEventFlags defaultFlags()
    {
        boolean[] values = new boolean[4];
        Arrays.fill(values, true);
        return new CalendarEventFlags(values);
    }

    @Override
    public String flagLabel(int i) {
        if (i >=0 && i < 4) {
            return phaseStrings[i];
        } else return "";
    }

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);

        calendarTitle = context.getString(resID_calendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarDesc = null;
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());

        phaseStrings[0] = context.getString(R.string.timeMode_moon_new);
        phaseStrings[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings[2] = context.getString(R.string.timeMode_moon_full);
        phaseStrings[3] = context.getString(R.string.timeMode_moon_thirdquarter);

        phaseStrings1[0] = context.getString(R.string.timeMode_moon_supernew);
        phaseStrings1[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings1[2] = context.getString(R.string.timeMode_moon_superfull);
        phaseStrings1[3] = context.getString(R.string.timeMode_moon_thirdquarter);

        phaseStrings2[0] = context.getString(R.string.timeMode_moon_micronew);
        phaseStrings2[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings2[2] = context.getString(R.string.timeMode_moon_microfull);
        phaseStrings2[3] = context.getString(R.string.timeMode_moon_thirdquarter);
    }

    protected String[] getPhaseStrings(int i, double distance, String[] strings)
    {
        String[] result = Arrays.copyOfRange(strings, 0, 4);
        if (i == 0 || i == 2)  // new moon || full moon
        {
            if (distance < THRESHHOLD_SUPERMOON) {
                result[i] = strings[4 + Math.max(0, i-1)];
            } else if (distance > THRESHHOLD_MICROMOON) {
                result[i] = strings[6 + Math.max(0, i-1)];
            }
        }
        return result;
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
                CalendarEventTemplate template = SuntimesCalendarSettings.loadPrefCalendarTemplate(context, calendarName, defaultTemplate());
                boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName, defaultFlags()).getValues();
                String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName, defaultStrings()).getValues();

                ArrayList<String> projection0 = new ArrayList<>(Arrays.asList(
                        COLUMN_MOON_NEW, COLUMN_MOON_FIRST,      // 0, 1
                        COLUMN_MOON_FULL, COLUMN_MOON_THIRD));   // 2, 3

                int i_dist = -1;
                boolean containsPattern_dist = template.containsPattern(TemplatePatterns.pattern_dist);

                int j = 4;
                if (containsPattern_dist)
                {
                    i_dist = j;
                    projection0.add(COLUMN_MOON_NEW_DISTANCE);
                    projection0.add(COLUMN_MOON_FIRST_DISTANCE);
                    projection0.add(COLUMN_MOON_FULL_DISTANCE);
                    projection0.add(COLUMN_MOON_THIRD_DISTANCE);
                    j += 4;
                }

                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPHASE + "/" + window[0] + "-" + window[1]);
                String[] projection = projection0.toArray(new String[0]);
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    int c = 0;
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = task.createProgressObj(c, totalProgress, calendarTitle);
                    task.publishProgress(progress0, progress);

                    ContentValues data = TemplatePatterns.createContentValues(null, this);
                    data = TemplatePatterns.createContentValues(data, task.getLocation());

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        for (int i=0; i<4; i++)
                        {
                            if (!flags[i]) {
                                continue;
                            }

                            double distance = cursor.getDouble(i + i_dist);
                            String[] eventStrings = getPhaseStrings(i, distance, strings);
                            data.put(TemplatePatterns.pattern_event.getPattern(), eventStrings[i]);
                            data.put(TemplatePatterns.pattern_dist.getPattern(), ((distance > 0) ? Utils.formatAsDistance(task.getLengthUnits(), distance, 2) : ""));

                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(cursor.getLong(i));
                            eventValues.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventTime));
                        }
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast())
                        {
                            adapter.createCalendarEvents(eventValues.toArray(new ContentValues[0]));
                            eventValues.clear();
                        }
                        progress.setProgress(c, totalProgress, calendarTitle);
                        task.publishProgress(progress0, progress);
                    }
                    cursor.close();
                    createCalendarReminders(context, task, progress0);
                    return !task.isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.w(getClass().getSimpleName(), lastError);
                    return false;
                }
            } else {
                lastError = "Unable to getContentResolver!";
                Log.e("initMoonPhaseCalendar", lastError);
                return false;
            }
        } else return false;
    }

}
