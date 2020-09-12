/**
    Copyright (C) 2020 Forrest Guice
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
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarAdapterInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarSettingsInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskProgressInterface;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class ContentProviderCalendar extends SuntimesCalendarBase implements SuntimesCalendar
{
    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
    public static final int CHUNK_DAYS = 7;
    public static final long CHUNK_MILLIS = CHUNK_DAYS * DAY_MILLIS;

    private String contentUri = null;
    public String getContentUriString() {
        return contentUri;
    }

    public ContentProviderCalendar(String uriString)
    {
        contentUri = uriString;
        if (!contentUri.endsWith("/")) {
            contentUri += "/";
        }
    }

    @Override
    public String calendarName() {
        return calenderName;
    }
    private String calenderName = null;

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettingsInterface settings)
    {
        super.init(context, settings);
        queryCalendarInfo();
        calendarDesc = null;
        calendarColor = (calenderName != null ? settings.loadPrefCalendarColor(context, calendarName()) : calendarColor);
    }

    protected void queryCalendarInfo()
    {
        Context context = contextRef.get();
        ContentResolver resolver = (context == null ? null : context.getContentResolver());
        if (resolver != null)
        {
            Uri uri = Uri.parse(contentUri + SuntimesCalendar.QUERY_CALENDAR_INFO);
            Cursor cursor = resolver.query(uri, SuntimesCalendar.QUERY_CALENDAR_INFO_PROJECTION, null, null, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                calenderName = cursor.getString(cursor.getColumnIndex(COLUMN_CALENDAR_NAME));
                calendarTitle = cursor.getString(cursor.getColumnIndex(COLUMN_CALENDAR_TITLE));
                calendarSummary = cursor.getString(cursor.getColumnIndex(COLUMN_CALENDAR_SUMMARY));
                calendarColor = cursor.getInt(cursor.getColumnIndex(COLUMN_CALENDAR_COLOR));
                cursor.close();
            }
        }
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

                int c = 0;
                int totalProgress = (int)((window[1] - window[0]) / CHUNK_MILLIS);
                long start = window[0];
                for (long i = window[0]; i < window[1] && !task.isCancelled(); i += DAY_MILLIS)
                {
                    if ((i - start) > CHUNK_MILLIS)
                    {
                        ArrayList<ContentValues> values = readCursor(calendarID, queryCursor(resolver, new long[] {start, i}), task);
                        adapter.createCalendarEvents(values.toArray(new ContentValues[0]));
                        c++;
                        start = i;

                        SuntimesCalendarTaskProgressInterface progress = task.createProgressObj(c, totalProgress, calendarTitle);
                        progress.setProgress(c, totalProgress, calendarTitle);
                        task.publishProgress(progress0, progress);
                    }
                }
                return true;

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(getClass().getSimpleName(), lastError);
                return false;
            }
        } else return false;
    }

    private Cursor queryCursor(ContentResolver resolver, long[] window)
    {
        Uri uri = Uri.parse(contentUri + SuntimesCalendar.QUERY_CALENDAR_CONTENT + "/" + window[0] + "-" + window[1]);
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor == null) {
            lastError = "Failed to resolve URI! " + uri;
            Log.e(getClass().getSimpleName(), lastError);
        }
        return cursor;
    }

    private ArrayList<ContentValues> readCursor(long calendarID, Cursor cursor, @NonNull SuntimesCalendarTaskInterface task)
    {
        cursor.moveToFirst();

        ArrayList<ContentValues> eventValues = new ArrayList<>();
        while (!cursor.isAfterLast() && !task.isCancelled())
        {
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, values);

            boolean isValidEvent = (values.containsKey("title") && values.containsKey("description"));
            if (isValidEvent) {
                values.put("calendar_id", calendarID);
                eventValues.add(values);

            } else {
                Log.w(getClass().getSimpleName(), "Invalid event! result does not contain expected values; skipping..");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return eventValues;
    }

}
