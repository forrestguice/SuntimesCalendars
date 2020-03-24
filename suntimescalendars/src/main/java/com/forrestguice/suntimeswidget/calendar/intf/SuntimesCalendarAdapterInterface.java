/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar.intf;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

@TargetApi(14)
public interface SuntimesCalendarAdapterInterface
{
    public static final String CALENDAR_TWILIGHT_CIVIL = "civilTwilightCalendar";
    public static final String CALENDAR_SOLSTICE = "solsticeCalendar";
    public static final String CALENDAR_TWILIGHT_NAUTICAL = "nauticalTwilightCalendar";
    public static final String CALENDAR_TWILIGHT_ASTRO = "astroTwilightCalendar";
    public static final String CALENDAR_MOONRISE = "moonriseCalendar";
    public static final String CALENDAR_MOONPHASE = "moonPhaseCalendar";
    public static final String CALENDAR_MOONAPSIS = "moonApsisCalendar";

    ContentValues createCalendarContentValues(String calendarName, String displayName, int calendarColor);
    ContentValues createEventContentValues(long calendarID, String title, String description, @Nullable String location, Calendar... time);

    void createCalendar(String calendarName, String calendarDisplayName, int calendarColor);
    boolean updateCalendarColor(String calendarName, int calendarColor);
    boolean removeCalendars();
    boolean removeCalendar(String calendar);

    void createCalendarEvent(long calendarID, String title, String description, @Nullable String location, Calendar... time) throws SecurityException;
    void createCalendarEvent(long calendarID, String title, String description, Calendar... time) throws SecurityException;
    void createCalendarEvents(@NonNull ContentValues[] values) throws SecurityException;

    int removeCalendarEventsBefore( long calendarID, long timestamp );
    int removeCalendarEventsAt( long calendarID, long timestamp );
    int removeCalendarEventsAfter( long calendarID, long timestamp );

    Cursor queryCalendarEventsAt( long calendarID, long timestamp );
    boolean hasCalendarEvents( long calendarID, long timestamp );
    Cursor queryCalendars();
    Cursor queryCalendar(String calendarName);

    long queryCalendarID(String calendarName);
    boolean hasCalendar(String calendarName);
    boolean hasCalendars(Context context);

    /**
     * EVENT_PROJECTION
     */
    public static final String[] EVENT_PROJECTION = new String[]{
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

}
