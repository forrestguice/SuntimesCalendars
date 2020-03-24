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
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTask;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.WeakHashMap;

@SuppressWarnings("Convert2Diamond")
public class SolsticeCalendar implements SuntimesCalendar
{
    public static final String CALENDAR_SOLSTICE = "solsticeCalendar";

    private WeakReference<Context> contextRef = null;

    private String calendarName = CALENDAR_SOLSTICE;
    private String calendarTitle, calendarSummary, calendarDesc;
    private int calendarColor = Color.BLUE;
    private String lastError;

    private String[] solsticeStrings = new String[4];  // {spring, summer, fall, winter}

    @Override
    public void init(@NonNull Context context)
    {
        contextRef = new WeakReference<>(context);

        calendarTitle = context.getString(R.string.calendar_solstice_displayName);
        calendarSummary = null;
        calendarDesc = null;
        calendarColor = SuntimesCalendarSettings.loadPrefCalendarColor(context, CALENDAR_SOLSTICE);

        solsticeStrings[0] = context.getString(R.string.timeMode_equinox_vernal);
        solsticeStrings[1] = context.getString(R.string.timeMode_solstice_summer);
        solsticeStrings[2] = context.getString(R.string.timeMode_equinox_autumnal);
        solsticeStrings[3] = context.getString(R.string.timeMode_solstice_winter);
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTask task, @NonNull SuntimesCalendarTaskProgress progress0, @NonNull long[] window)
    {
        if (task.isCancelled()) {
            return false;
        }

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
                Calendar startDate = Calendar.getInstance();
                startDate.setTimeInMillis(window[0]);

                Calendar endDate = Calendar.getInstance();
                endDate.setTimeInMillis(window[1]);

                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SEASONS + "/" + startDate.get(Calendar.YEAR) + "-" + endDate.get(Calendar.YEAR));
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SEASON_VERNAL, CalculatorProviderContract.COLUMN_SEASON_SUMMER, CalculatorProviderContract.COLUMN_SEASON_AUTUMN, CalculatorProviderContract.COLUMN_SEASON_WINTER };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();

                    int c = 0;
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, calendarTitle);
                    task.publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        for (int i=0; i<projection.length; i++)
                        {
                            if (!cursor.isNull(i))
                            {
                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(cursor.getLong(i));
                                eventValues.add(SuntimesCalendarAdapter.createEventContentValues(calendarID, solsticeStrings[i], solsticeStrings[i], null, eventTime));
                            }
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
    public String lastError() {
        return lastError;
    }

    @Override
    public String calendarName() {
        return calendarName;
    }

    @Override
    public String calendarTitle() {
        return calendarTitle;
    }

    @Override
    public String calendarSummary() {
        return calendarSummary;
    }

    @Override
    public String calendarDescription() {
        return calendarDesc;
    }

    @Override
    public int calendarColor() {
        return calendarColor;
    }

}
