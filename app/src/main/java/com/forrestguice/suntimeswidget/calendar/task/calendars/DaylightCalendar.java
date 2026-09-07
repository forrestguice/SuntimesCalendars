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
import androidx.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.CalendarEventFlags;
import com.forrestguice.suntimeswidget.calendar.CalendarEventStrings;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_MIDNIGHT;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_NOON;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_ALT;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_AZ;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_DEC;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_RA;

@SuppressWarnings("Convert2Diamond")
public class DaylightCalendar extends SuntimesCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_DAYLIGHT;
    private static final int resID_calendarTitle = R.string.calendar_daylight_displayName;
    private static final int resID_calendarSummary = R.string.calendar_daylight_summary;

    private final String[] daylightStrings = new String[6];      // {0:sunrise, 1:solar noon, 2:sunset, 3:midnight, 4:morning, 5:afternoon}

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%M", "%M @ %loc\n%eZ, %eA", "%loc");
    }

    @Override
    public TemplatePatterns[] supportedPatterns()
    {
        return new TemplatePatterns[] {
                TemplatePatterns.pattern_event, TemplatePatterns.pattern_eZ, TemplatePatterns.pattern_eA, TemplatePatterns.pattern_eD, TemplatePatterns.pattern_eR, null,
                TemplatePatterns.pattern_loc, TemplatePatterns.pattern_lat, TemplatePatterns.pattern_lon, TemplatePatterns.pattern_lel, null,
                TemplatePatterns.pattern_cal, TemplatePatterns.pattern_summary, TemplatePatterns.pattern_color, TemplatePatterns.pattern_percent
        };
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(daylightStrings);
    }

    @Override
    public CalendarEventFlags defaultFlags()
    {
        boolean[] values = new boolean[daylightStrings.length];
        Arrays.fill(values, true);
        values[3] = false;    // morning and afternoon default false
        values[4] = false;
        return new CalendarEventFlags(values);
    }

    @Override
    public String flagLabel(int i) {
        if (i >=0 && i < daylightStrings.length) {
            return daylightStrings[i];
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

        daylightStrings[0] = context.getString(R.string.sunrise);
        daylightStrings[1] = context.getString(R.string.timeMode_noon);
        daylightStrings[2] = context.getString(R.string.sunset);
        daylightStrings[3] = context.getString(R.string.midnight);
        daylightStrings[4] = context.getString(R.string.morning);
        daylightStrings[5] = context.getString(R.string.afternoon);
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
                CalendarEventTemplate template = settings.loadPrefCalendarTemplate(context, calendarName(), defaultTemplate());
                boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName(), defaultFlags()).getValues();
                String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName(), defaultStrings()).getValues();

                int i_eZ = -1, i_eA = -1, i_eR = -1, i_eD = -1;
                boolean containsPattern_eZ, containsPattern_eA, containsPattern_eR, containsPattern_eD;
                boolean containsPattern_em = template.containsPattern(TemplatePatterns.pattern_em);

                int j = 4;
                ArrayList<String> projection0 = new ArrayList<>(Arrays.asList(COLUMN_SUN_ACTUAL_RISE, COLUMN_SUN_NOON, COLUMN_SUN_ACTUAL_SET, COLUMN_SUN_MIDNIGHT));
                if (containsPattern_eZ = template.containsPattern(TemplatePatterns.pattern_eZ)) {
                    i_eZ  = j;
                    projection0.add(COLUMN_SUN_ACTUAL_RISE + _POSITION_AZ);
                    projection0.add(COLUMN_SUN_NOON + _POSITION_AZ);
                    projection0.add(COLUMN_SUN_ACTUAL_SET + _POSITION_AZ);
                    projection0.add(COLUMN_SUN_MIDNIGHT + _POSITION_AZ);
                    j += 4;
                }
                if (containsPattern_eA = template.containsPattern(TemplatePatterns.pattern_eA))
                {
                    i_eA  = j;
                    projection0.add(COLUMN_SUN_ACTUAL_RISE + _POSITION_ALT);
                    projection0.add(COLUMN_SUN_NOON + _POSITION_ALT);
                    projection0.add(COLUMN_SUN_ACTUAL_SET + _POSITION_ALT);
                    projection0.add(COLUMN_SUN_MIDNIGHT + _POSITION_ALT);
                    j += 4;
                }
                if (containsPattern_eR = template.containsPattern(TemplatePatterns.pattern_eR))
                {
                    i_eR  = j;
                    projection0.add(COLUMN_SUN_ACTUAL_RISE + _POSITION_RA);
                    projection0.add(COLUMN_SUN_NOON + _POSITION_RA);
                    projection0.add(COLUMN_SUN_ACTUAL_SET + _POSITION_RA);
                    projection0.add(COLUMN_SUN_MIDNIGHT + _POSITION_RA);
                    j += 4;
                }
                if (containsPattern_eD = template.containsPattern(TemplatePatterns.pattern_eD))
                {
                    i_eD  = j;
                    projection0.add(COLUMN_SUN_ACTUAL_RISE + _POSITION_DEC);
                    projection0.add(COLUMN_SUN_NOON + _POSITION_DEC);
                    projection0.add(COLUMN_SUN_ACTUAL_SET + _POSITION_DEC);
                    projection0.add(COLUMN_SUN_MIDNIGHT + _POSITION_DEC);
                    j += 4;
                }
                String[] projection = projection0.toArray(new String[0]);

                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + window[0] + "-" + window[1]);
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    String[] location = task.getLocation();
                    settings.saveCalendarNote(context, calendarName(), SuntimesCalendarSettings.NOTE_LOCATION_NAME, location[0]);

                    int c = 0;
                    int totalProgress = cursor.getCount();
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, location[0]);
                    SuntimesCalendarTaskProgress progress = task.createProgressObj(c, totalProgress, progressTitle);
                    task.publishProgress(progress0, progress);

                    ContentValues data = TemplatePatterns.createContentValues(null, this);
                    data = TemplatePatterns.createContentValues(data, task.getLocation());

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        Calendar[] events = new Calendar[4];    // 0:sunrise, 1:noon, 2:sunset, 3:midnight
                        for (int i=0; i<4; i++)
                        {
                            if (!cursor.isNull(i))
                            {
                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(cursor.getLong(i));
                                events[i] = eventTime;

                                if (flags[i])
                                {
                                    data.put(TemplatePatterns.pattern_event.getPattern(), strings[i]);
                                    putTemplatePatterns_eA(data, cursor, i, containsPattern_eA, i_eA);
                                    putTemplatePatterns_eZ(data, cursor, i, containsPattern_eZ, i_eZ);
                                    putTemplatePatterns(data, cursor, i, containsPattern_eD, i_eD, containsPattern_eR, i_eR, containsPattern_em, eventTime);
                                    eventValues.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventTime));
                                    //Log.d("DEBUG", "create event: " + strings[i] + " at " + eventTime.toString());
                                }
                            }
                        }

                        if (flags[4] && (events[0] != null) && (!cursor.isNull(0)))
                        {   // 4:morning
                            data.put(TemplatePatterns.pattern_event.getPattern(), strings[4]);
                            putTemplatePatterns_eA(data, cursor, 1, containsPattern_eA, i_eA);    // altitude at noon
                            putTemplatePatterns_eZ(data, cursor, 0, containsPattern_eZ, i_eZ);    // direction at sunrise
                            putTemplatePatterns(data, cursor, 0, containsPattern_eD, i_eD, containsPattern_eR, i_eR, containsPattern_em, events[0]);
                            eventValues.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), events[0], events[1]));
                        }

                        if (flags[5] && (events[2] != null) && (!cursor.isNull(2)))
                        {   // 5:afternoon
                            data.put(TemplatePatterns.pattern_event.getPattern(), strings[5]);
                            putTemplatePatterns_eA(data, cursor, 1, containsPattern_eA, i_eA);    // altitude at noon
                            putTemplatePatterns_eZ(data, cursor, 2, containsPattern_eZ, i_eZ);    // direction at sunset
                            putTemplatePatterns(data, cursor, 2, containsPattern_eD, i_eD, containsPattern_eR, i_eR, containsPattern_em, events[2]);
                            eventValues.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), events[1], events[2]));
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

    private void putTemplatePatterns(ContentValues data, Cursor cursor, int i,
                                     boolean containsPattern_eR, int i_eR,
                                     boolean containsPattern_eD, int i_eD,
                                     boolean containsPattern_em, Calendar eventTime)
    {
        if (containsPattern_eR) {
            data.put(TemplatePatterns.pattern_eR.getPattern(), Utils.formatAsRightAscension(cursor.getDouble(i + i_eR), 1));
        }
        if (containsPattern_eD) {
            data.put(TemplatePatterns.pattern_eD.getPattern(), Utils.formatAsDeclination(cursor.getDouble(i + i_eD), 1));
        }
        if (containsPattern_em) {
            data.put(TemplatePatterns.pattern_em.getPattern(), eventTime.getTimeInMillis());
        }
    }
    private void putTemplatePatterns_eZ(ContentValues data, Cursor cursor, int i,
                                        boolean containsPattern_eZ, int i_eZ) {
        if (containsPattern_eZ) {
            data.put(TemplatePatterns.pattern_eZ.getPattern(), Utils.formatAsDirection(cursor.getDouble(i + i_eZ), 2));
        }
    }
    private void putTemplatePatterns_eA(ContentValues data, Cursor cursor, int i,
                                        boolean containsPattern_eA, int i_eA) {
        if (containsPattern_eA) {
            data.put(TemplatePatterns.pattern_eA.getPattern(), Utils.formatAsElevation(cursor.getDouble(i + i_eA), 2));
        }
    }

    @Override
    public String[] getGroups() {
        return new String[] {};
    }

    @Override
    public int priority() {
        return 0;
    }

}
