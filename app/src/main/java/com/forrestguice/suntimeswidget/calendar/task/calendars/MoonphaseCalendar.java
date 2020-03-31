/**
    Copyright (C) 2018-2020 Forrest Guice
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

import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarAdapterInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarSettingsInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskProgressInterface;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class MoonphaseCalendar extends MoonCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE;
    private static final int resID_calendarTitle = R.string.calendar_moonPhase_displayName;
    private static final int resID_calendarSummary = R.string.calendar_moonPhase_summary;

    public static final double THRESHHOLD_SUPERMOON = 360000;    // km
    public static final double THRESHHOLD_MICROMOON = 405000;    // km

    private String[] phaseStrings = new String[4];     // {major phases}
    private String[] phaseStrings1 = new String[4];    // {major phases; supermoon}
    private String[] phaseStrings2 = new String[4];    // {major phases; micromoon}

    @Override
    public String calendarName() {
        return SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE;
    }

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettingsInterface settings)
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

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettingsInterface settings, @NonNull SuntimesCalendarAdapterInterface adapter, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgressInterface progress0, @NonNull long[] window)
    {
        if (task.isCancelled()) {
            return false;
        }

        String calendarName = calendarName();
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColor);
        } else return false;

        String[] projection = new String[] {    // indices 0-3 should contain ordered phases!
                CalculatorProviderContract.COLUMN_MOON_NEW,
                CalculatorProviderContract.COLUMN_MOON_FIRST,
                CalculatorProviderContract.COLUMN_MOON_FULL,
                CalculatorProviderContract.COLUMN_MOON_THIRD,
                CalculatorProviderContract.COLUMN_MOON_NEW_DISTANCE,  // use indices 4+ for other data
                CalculatorProviderContract.COLUMN_MOON_FULL_DISTANCE
        };

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPHASE + "/" + window[0] + "-" + window[1]);
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    int c = 0;
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgressInterface progress = task.createProgressObj(c, totalProgress, calendarTitle);
                    task.publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        for (int i=0; i<4; i++)
                        {
                            double distance = -1;
                            String[] titleStrings;
                            if (i == 0 || i == 2)  // new moon || full moon
                            {
                                distance = cursor.getDouble(i == 0 ? 4 : 5);

                                if (distance < THRESHHOLD_SUPERMOON) {
                                    titleStrings = phaseStrings1;
                                } else if (distance > THRESHHOLD_MICROMOON) {
                                    titleStrings = phaseStrings2;
                                } else titleStrings = phaseStrings;

                            } else titleStrings = phaseStrings;

                            String desc = (distance > 0)
                                    ? context.getString(R.string.event_distance_format, titleStrings[i], formatDistanceString(distance))
                                    : titleStrings[i];
                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(cursor.getLong(i));
                            eventValues.add(adapter.createEventContentValues(calendarID, titleStrings[i], desc, null, eventTime));
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
