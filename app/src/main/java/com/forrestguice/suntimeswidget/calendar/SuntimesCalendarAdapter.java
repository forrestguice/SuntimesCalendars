/**
    Copyright (C) 2018-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

@TargetApi(14)
public class SuntimesCalendarAdapter
{
    public static final String TAG = "SuntimesCalendarAdapter";

    public static final String CALENDAR_TWILIGHT_CIVIL = "civilTwilightCalendar";
    public static final String CALENDAR_TWILIGHT_GOLD = "goldHourCalendar";
    public static final String CALENDAR_TWILIGHT_BLUE = "blueHourCalendar";
    public static final String CALENDAR_SOLSTICE = "solsticeCalendar";
    public static final String CALENDAR_TWILIGHT_NAUTICAL = "nauticalTwilightCalendar";
    public static final String CALENDAR_TWILIGHT_ASTRO = "astroTwilightCalendar";
    public static final String CALENDAR_MOONRISE = "moonriseCalendar";
    public static final String CALENDAR_MOONPHASE = "moonPhaseCalendar";
    public static final String CALENDAR_MOONAPSIS = "moonApsisCalendar";


    private ContentResolver contentResolver;
    private String[] calendars = new String[0];

    public SuntimesCalendarAdapter(ContentResolver contentResolver, String[] calendars)
    {
        this.contentResolver = contentResolver;
        this.calendars = calendars;
    }

    /**
     * Creates a new calender managed by the "Suntimes" local account.
     * @param calendarName the calendar's name
     * @param calendarDisplayName the calendar's display string
     * @param calendarColor the calendar's color (an index into calendar color table)
     */
    public void createCalendar(String calendarName, String calendarDisplayName, int calendarColor)
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Calendars.CONTENT_URI);
        ContentValues contentValues = createCalendarContentValues(calendarName, calendarDisplayName, calendarColor);
        contentResolver.insert(uri, contentValues);
    }

    public boolean updateCalendarColor(String calendarName, int calendarColor)
    {
        Cursor cursor = queryCalendar(calendarName);
        if (cursor != null && cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, values);
            cursor.close();

            values.put(CalendarContract.Calendars.CALENDAR_COLOR, calendarColor);

            Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Calendars.CONTENT_URI);
            String[] args = new String[] { SuntimesCalendarSyncAdapter.ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, calendarName, SuntimesCalendarSyncAdapter.ACCOUNT_NAME };
            String select = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                    + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                    + CalendarContract.Calendars.NAME + " = ?) AND ("
                    + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
            return (contentResolver.update(uri, values, select, args) > 0);
        }
        return false;
    }

    /**
     * Removes all calendars managed by the "Suntimes" local account.
     */
    public boolean removeCalendars()
    {
        Cursor cursor = queryCalendars();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                long calendarID = cursor.getLong(PROJECTION_ID_INDEX);
                Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarID);
                contentResolver.delete(deleteUri, null, null);
                //Log.d(TAG, "removeCalendars: removed calendar " + calendarID);
            }
            cursor.close();
            return true;
        } else return false;
    }

    /**
     * Removes individual calendars by name.
     * @param calendar calendar name
     * @return true calendar was removed, false otherwise
     */
    public boolean removeCalendar(String calendar) {
        return removeCalendar(queryCalendarID(calendar));
    }
    public boolean removeCalendar(long calendarID)
    {
        if (calendarID != -1)
        {
            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarID);
            contentResolver.delete(deleteUri, null, null);
            //Log.d(TAG, "removeCalendar: removed calendar " + calendarID);
            return true;
        } else return false;
    }

    /**
     * @param calendarID the calendar's ID
     * @param title the event title
     * @param description the event description
     * @param time the startTime of the event (endTime is the same)
     */
    public void createCalendarEvent(long calendarID, String title, String description, @Nullable String location, Calendar... time) throws SecurityException
    {
        ContentValues contentValues = createEventContentValues(calendarID, title, description, location, time);
        contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues);
    }
    public void createCalendarEvent(long calendarID, String title, String description, Calendar... time) throws SecurityException {
        createCalendarEvent(calendarID, title, description, null, time);
    }
    public void createCalendarEvents(@NonNull ContentValues[] values) throws SecurityException
    {
        contentResolver.bulkInsert(CalendarContract.Events.CONTENT_URI, values);
    }

    public boolean createCalendarReminders(@NonNull ContentValues[] values) throws SecurityException {
        contentResolver.bulkInsert(CalendarContract.Reminders.CONTENT_URI, values);
        return true;
    }

    /**
     * removeCalendarEventsBefore
     * @param calendarID calendar ID
     * @param timestamp remove all events occurring before timestamp
     * @return the number of events removed
     */
    public int removeCalendarEventsBefore( long calendarID, long timestamp )
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Events.CONTENT_URI);
        String[] args = new String[] { Long.toString(calendarID), Long.toString(timestamp) };
        String select = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND (" + CalendarContract.Events.DTSTART + " < ?))";
        return contentResolver.delete(uri, select, args);
    }

    /**
     * removeCalendarEventsAt
     * @param calendarID calendar ID
     * @param timestamp remove all events occurring on timestamp
     * @return the number of events removed
     */
    public int removeCalendarEventsAt( long calendarID, long timestamp )
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Events.CONTENT_URI);
        String[] args = new String[] { Long.toString(calendarID), Long.toString(timestamp) };
        String select = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND (" + CalendarContract.Events.DTSTART + " = ?))";
        return contentResolver.delete(uri, select, args);
    }

    /**
     * removeCalendarEventsAfter
     * @param calendarID calendar ID
     * @param timestamp remove all events occurring after timestamp
     * @return the number of events removed
     */
    public int removeCalendarEventsAfter( long calendarID, long timestamp )
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Events.CONTENT_URI);
        String[] args = new String[] { Long.toString(calendarID), Long.toString(timestamp) };
        String select = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND (" + CalendarContract.Events.DTSTART + " > ?))";
        return contentResolver.delete(uri, select, args);
    }

    /**
     * queryCalendarEvents
     * @param calendarID calendar ID
     * @param timestamp query events with start or end matching timestamp
     * @return cursor
     */
    public Cursor queryCalendarEventsAt( long calendarID, long timestamp )
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Events.CONTENT_URI);
        String[] args = new String[] { Long.toString(calendarID), Long.toString(timestamp) };
        String select = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND (" + CalendarContract.Events.DTSTART + " = ?))";
        return contentResolver.query(uri, EVENT_PROJECTION, select, args, null);
    }

    public boolean hasCalendarEvents( long calendarID, long timestamp ) {
        return (queryCalendarEventsAt(calendarID, timestamp).getCount() > 0);
    }

    public Cursor queryCalendarEvents( long calendarID )
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Events.CONTENT_URI);
        String[] args = new String[] { Long.toString(calendarID) };
        String select = "((" + CalendarContract.Events.CALENDAR_ID + " = ?))";
        return contentResolver.query(uri, EVENT_PROJECTION, select, args, null);
    }

    /**
     * @return a Cursor to all calendars managed by the "Suntimes" local account
     */
    public Cursor queryCalendars()
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Calendars.CONTENT_URI);
        String[] args = new String[] { SuntimesCalendarSyncAdapter.ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, SuntimesCalendarSyncAdapter.ACCOUNT_NAME };
        String select = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND (" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND (" + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        return contentResolver.query(uri, EVENT_PROJECTION, select, args, null);
    }

    /**
     * @param calendarName the calendar's name
     * @return a Cursor to the calendar w/ the given name managed by the "Suntimes" local account.
     */
    public Cursor queryCalendar(String calendarName)
    {
        Uri uri = SuntimesCalendarSyncAdapter.asSyncAdapter(CalendarContract.Calendars.CONTENT_URI);
        String[] args = new String[] { SuntimesCalendarSyncAdapter.ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, calendarName, SuntimesCalendarSyncAdapter.ACCOUNT_NAME };
        String select = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.NAME + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        return contentResolver.query(uri, EVENT_PROJECTION, select, args, null);
    }

    /**
     * @param calendarName
     * @return
     */
    public long queryCalendarID(String calendarName)
    {
        long calendarID = -1;
        Cursor cursor = queryCalendar(calendarName);
        if (cursor != null)
        {
            while (cursor.moveToNext()) {
                calendarID = cursor.getLong(PROJECTION_ID_INDEX);
            }
            cursor.close();
        } else {
            Log.w(TAG, "initCalendars: Calendar not found! (null cursor) " + calendarName);
            calendarID = -1;
        }
        return calendarID;
    }

    /**
     * @param calendarName the calendar's name
     * @return true if a calendar w/ given name is already managed by the "Suntimes" local account, false otherwise.
     */
    public boolean hasCalendar(String calendarName)
    {
        boolean retValue = false;
        Cursor cursor = queryCalendar(calendarName);
        if (cursor != null) {
            retValue = (cursor.getCount() > 0);
            cursor.close();
        }
        return retValue;
    }

    /**
     * @return true if any calendars are being managed by the "Suntimes" local account, false no calendars exist.
     */
    public boolean hasCalendars(Context context)
    {
        try {
            for (String calendar : calendars)
            {
                Cursor cursor = queryCalendar(calendar);
                if (cursor != null)
                {
                    boolean hasCalendar = (cursor.getCount() > 0);
                    cursor.close();
                    if (hasCalendar) {
                        return true;
                    }
                }
            }
        } catch (SecurityException e) {
            return false;
        }
        return false;
    }

    /**
     * @param calendar calendar name
     * @return the calendar's position within ALL_CALENDARS (or -1 if calendar dne)
     */
    public int calendarOrdinal(String calendar)
    {
        for (int i=0; i<calendars.length; i++)
        {
            if (calendars[i].equals(calendar)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param calendarOrdinal calendar number
     * @return the calendar's name (or null if calendar dne)
     */
    public String calendarName( int calendarOrdinal )
    {
        if (calendarOrdinal >= 0 && calendarOrdinal < calendars.length)
            return calendars[calendarOrdinal];
        else return null;
    }

    public String[] getCalendarList() {
        return calendars;
    }

    /**
     * @param calendarName
     * @param displayName
     * @param calendarColor
     * @return
     */
    public ContentValues createCalendarContentValues(String calendarName, String displayName, int calendarColor)
    {
        ContentValues v = new ContentValues();
        v.put(CalendarContract.Calendars.ACCOUNT_NAME, SuntimesCalendarSyncAdapter.ACCOUNT_NAME);
        v.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        v.put(CalendarContract.Calendars.OWNER_ACCOUNT, SuntimesCalendarSyncAdapter.ACCOUNT_NAME);
        v.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);

        v.put(CalendarContract.Calendars.NAME, calendarName);
        v.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, displayName);
        v.put(CalendarContract.Calendars.CALENDAR_COLOR, calendarColor);
        v.put(CalendarContract.Calendars.VISIBLE, 1);
        v.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        v.put(CalendarContract.Calendars.ALLOWED_REMINDERS, "METHOD_ALERT, METHOD_EMAIL, METHOD_ALARM");

        if (Build.VERSION.SDK_INT >= 15)
        {
            v.put(CalendarContract.Calendars.ALLOWED_AVAILABILITY, "AVAILABILITY_BUSY, AVAILABILITY_FREE, AVAILABILITY_TENTATIVE");
            //v.put(CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES, "TYPE_OPTIONAL, TYPE_REQUIRED, TYPE_RESOURCE");
        }

        return v;
    }

    /**
     * @param calendarID
     * @param title
     * @param description
     * @param time
     * @return
     */
    public ContentValues createEventContentValues(long calendarID, String title, String description, @Nullable String location, Calendar... time)
    {
        ContentValues v = new ContentValues();
        v.put(CalendarContract.Events.CALENDAR_ID, calendarID);
        v.put(CalendarContract.Events.TITLE, title);
        v.put(CalendarContract.Events.DESCRIPTION, description);

        if (time.length > 0)
        {
            v.put(CalendarContract.Events.EVENT_TIMEZONE, time[0].getTimeZone().getID());
            if (time.length >= 2)
            {
                v.put(CalendarContract.Events.DTSTART, time[0].getTimeInMillis());
                v.put(CalendarContract.Events.DTEND, time[1].getTimeInMillis());
            } else {
                v.put(CalendarContract.Events.DTSTART, time[0].getTimeInMillis());
                v.put(CalendarContract.Events.DTEND, time[0].getTimeInMillis());
            }
        } else {
            Log.w(TAG, "createEventContentValues: missing time arg (empty array); creating event without start or end time.");
        }

        if (location != null) {
            v.put(CalendarContract.Events.EVENT_LOCATION, location);
        }

        v.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        v.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_MODIFY, "0");
        return v;
    }

    /**
     * @param calendarID
     * @param eventID
     * @param minutesBeforeEvent
     * @param method e.g. CalendarContract.Reminders.METHOD_DEFAULT
     * @return
     */
    public ContentValues createReminderContentValues(long calendarID, long eventID, int minutesBeforeEvent, int method)
    {
        ContentValues v = new ContentValues();
        v.put(CalendarContract.Reminders.EVENT_ID, eventID);
        v.put(CalendarContract.Reminders.MINUTES, minutesBeforeEvent);
        v.put(CalendarContract.Reminders.METHOD, method);
        return v;
    }

    /**
     * EVENT_PROJECTION
     */
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT,                 // 3
            CalendarContract.Calendars.CALENDAR_COLOR                 // 4
    };
    public static final int PROJECTION_ID_INDEX = 0;
    public static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    public static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    public static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    public static final int PROJECTION_CALENDAR_COLOR_INDEX = 4;


    /**
     * REMINDERS_PROJECTION
     */
    public static final String[] REMINDERS_PROJECTION = new String[] {
            CalendarContract.Reminders.MINUTES,
            CalendarContract.Reminders.METHOD,
    };

}
