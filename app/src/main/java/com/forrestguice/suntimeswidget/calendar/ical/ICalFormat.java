/**
    Copyright (C) 2024 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.calendar.ical;

import android.content.ContentValues;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * ICalendarFormat
 * https://www.rfc-editor.org/info/rfc5545/
 */
public class ICalFormat
{
    public static final String ICS_MIMETYPE = "text/calendar";
    public static final String ICS_FILEEXT = ".ics";

    public static final String ICS_VERSION = "2.0";
    public static final String ICS_PRODID = "-//SuntimesCalendars//NONSGML v1.0//EN";

    public static final Charset ENCODING;
    static {
        if (Build.VERSION.SDK_INT >= 19) {
            ENCODING = StandardCharsets.UTF_8;
        } else {
            //noinspection CharsetObjectCanBeUsed
            ENCODING = Charset.forName("UTF-8");
        }
    }

    /**
     * icsCalendar
     * @return ics calendar; BEGIN:VCALENDAR ... END:VCALENDAR
     */
    public String icsCalendar(String calendar, String calendarLabel, ContentValues[] events) {
        return icsCalendarStart(calendar, calendarLabel) + icsEvents(events) + icsCalendarEnd();
    }

    public String icsCalendarStart(String calendar, String calendarLabel)
    {
        return "BEGIN:VCALENDAR" + CRLF +
                "VERSION:" + ICS_VERSION + CRLF +
                foldContentLine("PRODID:" + ICS_PRODID) + CRLF +
                foldContentLine("X-WR-RELCALID:" + encodeContentLine(calendar)) + CRLF +
                foldContentLine("X-WR-CALNAME:" + encodeContentLine(calendarLabel)) + CRLF;
    }

    public String icsCalendarEnd() {
        return "END:VCALENDAR" + CRLF;
    }

    public String icsEvents(ContentValues... events)
    {
        StringBuilder r = new StringBuilder();
        for (ContentValues event : events) {
            r.append(icsEvent(event));
        }
        return r.toString();
    }

    /**
     * icsEvent
     * @return ics event; BEGIN:VEVENT ... END:VEVENT
     */
    public String icsEvent(ContentValues event)
    {
        StringBuilder r = new StringBuilder();
        r.append("BEGIN:VEVENT").append(CRLF);

        //int uid = event.hashCode();   // TODO: UID must be well formed or calendar apps will reject it
        //r.append("UID:").append(uid).append(CRLF);

        r.append("CREATED:").append(toTimestamp(Calendar.getInstance())).append(CRLF);
        r.append("DTSTAMP:").append(toTimestamp(Calendar.getInstance())).append(CRLF);

        r.append(foldContentLine("SUMMARY:" + encodeContentLine(event.getAsString(CalendarContract.Events.TITLE)))).append(CRLF);
        r.append(foldContentLine("DESCRIPTION:" + encodeContentLine(event.getAsString(CalendarContract.Events.DESCRIPTION)))).append(CRLF);

        if (event.containsKey(CalendarContract.Events.EVENT_LOCATION)) {
            r.append(foldContentLine("LOCATION:" + encodeContentLine(event.getAsString(CalendarContract.Events.EVENT_LOCATION)))).append(CRLF);
        }

        if (event.containsKey(CalendarContract.Events.CALENDAR_DISPLAY_NAME)) {
            r.append(foldContentLine("CATEGORIES:" + encodeContentLine(event.getAsString(CalendarContract.Events.CALENDAR_DISPLAY_NAME)))).append(CRLF);
        }

        Calendar eventStart = Calendar.getInstance();
        eventStart.setTimeInMillis(event.getAsLong(CalendarContract.Events.DTSTART));

        Calendar eventEnd = Calendar.getInstance();
        eventEnd.setTimeInMillis(event.getAsLong(CalendarContract.Events.DTEND));

        Boolean allDay = event.getAsBoolean(CalendarContract.Events.ALL_DAY);
        if (allDay != null && allDay) {
            r.append("DTSTART;VALUE=DATE:").append(toTimestamp(eventStart, false)).append(CRLF);
            r.append("DTEND;VALUE=DATE:").append(toTimestamp(eventEnd, false)).append(CRLF);

        } else {
            r.append("DTSTART:").append(toTimestamp(eventStart, true)).append(CRLF);
            r.append("DTEND:").append(toTimestamp(eventEnd, true)).append(CRLF);
        }

        if (event.containsKey(CalendarContract.Events.AVAILABILITY))
        {
            r.append("TRANSP:");
            switch (event.getAsInteger(CalendarContract.Events.AVAILABILITY)) {
                case CalendarContract.Events.AVAILABILITY_BUSY: r.append("OPAQUE"); break;
                case CalendarContract.Events.AVAILABILITY_FREE: default: r.append("TRANSPARENT"); break;
            }
            r.append(CRLF);
        }

        r.append("CLASS:");
        int accessLevel = event.containsKey(CalendarContract.Events.ACCESS_LEVEL)
                ? event.getAsInteger(CalendarContract.Events.ACCESS_LEVEL)
                : CalendarContract.Events.ACCESS_DEFAULT;
        switch (accessLevel)
        {
            case CalendarContract.Events.ACCESS_PRIVATE: r.append("PRIVATE"); break;
            case CalendarContract.Events.ACCESS_CONFIDENTIAL: r.append("CONFIDENTIAL"); break;
            case CalendarContract.Events.ACCESS_PUBLIC:
            case CalendarContract.Events.ACCESS_DEFAULT: r.append("PUBLIC"); break;
        }
        r.append(CRLF);

        // TODO
        //CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "0"
        //CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "0"
        //CalendarContract.Events.GUESTS_CAN_MODIFY, "0"

        r.append("END:VEVENT").append(CRLF);
        return r.toString();
    }

    private int numBytes(String line) {
        return line.getBytes(ICalFormat.ENCODING).length;
    }

    protected String foldContentLine(@NonNull String line)
    {
        int byteLimit = 75;
        if (numBytes(line) < byteLimit) {
            return line;
        }

        StringBuilder r = new StringBuilder();
        int i = 0, n = line.length();
        while (i < n)
        {
            int j = i + 1;
            while (numBytes(line.substring(i, j) + CRLF_) < byteLimit
                    && j < n) {
                j++;
            }
            r.append(line.substring(i, j));
            if (j < n) {
                r.append(CRLF_);
            }
            i = j;
        }
        return r.toString();
    }

    protected String encodeContentLine(String value) {
        return value.replaceAll("\n", "\\\\n")
                .replaceAll("\r", "\\\\n")
                .replaceAll(",", "\\,")
                .replaceAll(";", "\\;");
                //.replaceAll("\\\\", "\\");    // TODO: character to be escaped is missing
    }

    protected String toTimestamp(Calendar calendar) {
        return toTimestamp(calendar, true);
    }
    protected String toTimestamp(Calendar calendar, boolean withTime)
    {
        if (format_date == null || format_time == null) {
            format_date = new SimpleDateFormat("yyyyMMdd", Locale.US);
            format_time = new SimpleDateFormat("HHmmss", Locale.US);
        }
        return format_date.format(calendar.getTimeInMillis())
                + (withTime ? "T" + format_time.format(calendar.getTimeInMillis()) + "Z" : "");
    }
    private SimpleDateFormat format_date, format_time;

    public final static char CR = (char) 0x0D;
    public final static char LF = (char) 0x0A;
    public final static String CRLF = "" + CR + LF;
    public final static String CRLF_ = CRLF + " ";

}
