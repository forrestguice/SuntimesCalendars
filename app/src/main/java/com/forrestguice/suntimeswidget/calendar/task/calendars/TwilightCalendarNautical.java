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
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTask;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.ui.templates.Template;

import java.util.ArrayList;

@SuppressWarnings("Convert2Diamond")
public class TwilightCalendarNautical extends TwilightCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_NAUTICAL;
    private static final int resID_calendarTitle = R.string.calendar_nautical_twilight_displayName;
    private static final int resID_calendarSummary = R.string.calendar_nautical_twilight_summary;

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public Template defaultTemplate() {
        return new Template("%cal", "%M @ %loc");
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
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + window[0] + "-" + window[1]);
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SUN_NAUTICAL_RISE, CalculatorProviderContract.COLUMN_SUN_CIVIL_RISE,
                        CalculatorProviderContract.COLUMN_SUN_CIVIL_SET, CalculatorProviderContract.COLUMN_SUN_NAUTICAL_SET };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    String[] location = task.getLocation();
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, location[0]);

                    int c = 0;
                    int numRows = cursor.getCount();
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, location[0]);
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, numRows, progressTitle);
                    task.publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    String title = calendarTitle;
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 0, title, s_DAWN_TWILIGHT, s_CIVIL_NIGHT, s_NAUTICAL_TWILIGHT);
                        createSunCalendarEvent(context, adapter, task, eventValues, calendarID, cursor, 2, title, s_DUSK_TWILIGHT, s_NAUTICAL_TWILIGHT, s_NAUTICAL_TWILIGHT);
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast()) {
                            adapter.createCalendarEvents( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || cursor.isLast()) {
                            progress.setProgress(c, numRows, progressTitle);
                            task.publishProgress(progress0, progress);
                        }
                    }
                    cursor.close();
                    createCalendarReminders(context, adapter);
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
