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
import java.util.Calendar;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_RISE_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_RISE_ILLUM;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_SET_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_SET_ILLUM;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_NOON;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_ALT;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_AZ;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_DEC;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract._POSITION_RA;

@SuppressWarnings("Convert2Diamond")
public class MoonriseCalendar extends MoonCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_MOONRISE;
    private static final int resID_calendarTitle = R.string.calendar_moonrise_displayName;
    private static final int resID_calendarSummary = R.string.calendar_moonrise_summary;

    private String[] moonStrings = new String[2];      // {moonrise, moonset}

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public CalendarEventTemplate defaultTemplate() {
        return new CalendarEventTemplate("%M", "%M @ %loc\n%eZ, %illum", "%loc");
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(moonStrings);
    }

    @Override
    public CalendarEventFlags defaultFlags()
    {
        boolean[] values = new boolean[moonStrings.length];
        Arrays.fill(values, true);
        return new CalendarEventFlags(values);
    }

    @Override
    public String flagLabel(int i) {
        if (i >=0 && i < moonStrings.length) {
            return moonStrings[i];
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

        moonStrings[0] = context.getString(R.string.moonrise);
        moonStrings[1] = context.getString(R.string.moonset);
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
                boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendarName, defaultFlags()).getValues();
                String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendarName, defaultStrings()).getValues();
                CalendarEventTemplate template = SuntimesCalendarSettings.loadPrefCalendarTemplate(context, calendarName, defaultTemplate());

                int i_eZ = -1, i_eA = -1, i_eR = -1, i_eD = -1, i_illum = -1, i_dist = -1;
                boolean containsPattern_eZ, containsPattern_eA, containsPattern_eR, containsPattern_eD, containsPattern_dist, containsPattern_illum;

                int j = 2;
                ArrayList<String> projection0 = new ArrayList<>(Arrays.asList(COLUMN_MOON_RISE, COLUMN_MOON_SET));
                if (containsPattern_eZ = template.containsPattern(TemplatePatterns.pattern_eZ)) {
                    i_eZ  = j;
                    projection0.add(COLUMN_MOON_RISE + _POSITION_AZ);
                    projection0.add(COLUMN_MOON_SET + _POSITION_AZ);
                    j += 2;
                }
                if (containsPattern_eA = template.containsPattern(TemplatePatterns.pattern_eA))
                {
                    i_eA  = j;
                    projection0.add(COLUMN_MOON_RISE + _POSITION_ALT);
                    projection0.add(COLUMN_MOON_SET + _POSITION_ALT);
                    j += 2;
                }
                if (containsPattern_eR = template.containsPattern(TemplatePatterns.pattern_eR))
                {
                    i_eR  = j;
                    projection0.add(COLUMN_MOON_RISE + _POSITION_RA);
                    projection0.add(COLUMN_MOON_SET + _POSITION_RA);
                    j += 2;
                }
                if (containsPattern_eD = template.containsPattern(TemplatePatterns.pattern_eD))
                {
                    i_eD  = j;
                    projection0.add(COLUMN_MOON_RISE + _POSITION_DEC);
                    projection0.add(COLUMN_MOON_SET + _POSITION_DEC);
                    j += 2;
                }
                if (containsPattern_dist = template.containsPattern(TemplatePatterns.pattern_dist))
                {
                    i_dist  = j;
                    projection0.add(COLUMN_MOON_RISE_DISTANCE);
                    projection0.add(COLUMN_MOON_SET_DISTANCE);
                    j += 2;
                }
                if (containsPattern_illum = template.containsPattern(TemplatePatterns.pattern_illum))
                {
                    i_illum  = j;
                    projection0.add(COLUMN_MOON_RISE_ILLUM);
                    projection0.add(COLUMN_MOON_SET_ILLUM);
                    j += 2;
                }
                String[] projection = projection0.toArray(new String[0]);

                Uri moonUri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON + "/" + window[0] + "-" + window[1]);
                Cursor cursor = resolver.query(moonUri, projection, null, null, null);
                if (cursor != null)
                {
                    String[] location = task.getLocation();
                    settings.saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, location[0]);

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
                        for (int i=0; i<2; i++)
                        {
                            if (flags[i] && !cursor.isNull(i))
                            {
                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(cursor.getLong(i));
                                data.put(TemplatePatterns.pattern_event.getPattern(), strings[i]);

                                if (containsPattern_eZ) {
                                    data.put(TemplatePatterns.pattern_eZ.getPattern(), Utils.formatAsDirection(cursor.getDouble(i + i_eZ), 2));
                                }
                                if (containsPattern_eA) {
                                    data.put(TemplatePatterns.pattern_eA.getPattern(), Utils.formatAsElevation(cursor.getDouble(i + i_eA), 2));
                                }
                                if (containsPattern_eR) {
                                    data.put(TemplatePatterns.pattern_eR.getPattern(), Utils.formatAsRightAscension(cursor.getDouble(i + i_eR), 1));
                                }
                                if (containsPattern_eD) {
                                    data.put(TemplatePatterns.pattern_eD.getPattern(), Utils.formatAsDeclination(cursor.getDouble(i + i_eD), 1));
                                }
                                if (containsPattern_dist) {
                                    data.put(TemplatePatterns.pattern_dist.getPattern(), Utils.formatAsDistanceKm(cursor.getDouble(i + i_dist), 1));   // TODO: length units
                                }
                                if (containsPattern_illum) {
                                    data.put(TemplatePatterns.pattern_illum.getPattern(), Utils.formatAsPercent(cursor.getDouble(i + i_illum), 1));
                                }
                                //desc = context.getString(R.string.event_at_format, moonStrings[i], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                                //desc = context.getString(R.string.event_at_format, moonStrings[i], location[0]);
                                eventValues.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventTime));
                                //Log.d("DEBUG", "create event: " + moonStrings[i] + " at " + eventTime.toString());
                            }
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
                    lastError = "Failed to resolve URI! " + moonUri;
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
