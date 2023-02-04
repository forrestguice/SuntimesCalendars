/*
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
package com.forrestguice.suntimeswidget.calendar.ui.templates;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;

/**
 * Template
 */
public class Template implements Parcelable
{
    protected String title;
    protected String body;

    public Template(String title, String body)
    {
        this.title = title;
        this.body = body;
    }

    public Template(Parcel in)
    {
        this.title = in.readString();
        this.body = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(title);
        dest.writeString(body);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Template> CREATOR = new Creator<Template>()
    {
        public Template createFromParcel(Parcel in) {
            return new Template(in);
        }
        public Template[] newArray(int size) {
            return new Template[size];
        }
    };

    public String getTitle() {
        return title;
    }
    public void setTitle(String value) {
        title = value;
    }

    public String getBody() {
        return body;
    }
    public void setBody(String value) {
        body = value;
    }

    public String getTitle(ContentValues data) {
        return replaceSubstitutions(title, data);
    }
    public String getBody(ContentValues data) {
        return replaceSubstitutions(body, data);
    }

    public static final String pattern_percent = "%%";
    public static final String pattern_cal = "%cal";
    public static final String pattern_summary = "%summary";
    public static final String pattern_color = "%color";
    public static final String pattern_loc = "%loc";
    public static final String pattern_lat = "%lat";
    public static final String pattern_lon = "%lon";
    public static final String pattern_lel = "%lel";
    public static final String pattern_event = "%M";
    public static final String pattern_dist = "%dist";
    public static final String pattern_illum = "%i";

    public static ContentValues createContentValues(@Nullable ContentValues values, SuntimesCalendar calendar)
    {
        if (values == null) {
            values = new ContentValues();
        }
        values.put(pattern_cal, calendar.calendarTitle());
        values.put(pattern_summary, calendar.calendarSummary());
        values.put(pattern_color, calendar.calendarColor());
        return values;
    }

    public static ContentValues createContentValues(@Nullable ContentValues values, SuntimesCalendarDescriptor calendar)
    {
        if (values == null) {
            values = new ContentValues();
        }
        values.put(pattern_cal, calendar.calendarTitle());
        values.put(pattern_summary, calendar.calendarSummary());
        values.put(pattern_color, calendar.calendarColor());
        return values;
    }

    public static ContentValues createContentValues(@Nullable ContentValues values, String[] location)
    {
        if (values == null) {
            values = new ContentValues();
        }
        if (location != null && location.length > 0) {
            values.put(pattern_loc, location[0]);
            if (location.length > 1) {
                values.put(pattern_lat, location[1]);
                if (location.length > 2) {
                    values.put(pattern_lon, location[2]);
                    if (location.length > 3) {
                        values.put(pattern_lel, location[3]);
                    }
                }
            }
        }
        return values;
    }

    /**
     * Substitutions:
     *   %cal                ..    calendar name (e.g. "Civil Twilight", "Moon", "Moon Phases", "Moon Apsis", etc)
     *   %summary            ..    calendar summary (e.g. "Sunrise / Sunset")
     *   %loc                ..    location name (e.g. Phoenix)
     *   %lat                ..    location latitude
     *   %lon                ..    location longitude
     *   %M                  ..    event title (e.g. "Sunrise", "Sunset", "Dawn", "Dusk", "Summer Solstice", "Full Moon", "Moonrise", "Apogee", etc).
     *
     *   %dist               ..    moon distance (e.g. 405,829.51 km)
     *   %i                  ..    moon illumination
     *
     *   %%                  ..    % character
     */
    public static String replaceSubstitutions(String pattern, ContentValues values)
    {
        String displayString = pattern;


        String[] patterns = new String[] {    // in order of replacement
                pattern_cal, pattern_summary, pattern_color,
                pattern_loc, pattern_lat, pattern_lon,
                pattern_event, pattern_dist, pattern_illum, pattern_percent
        };

        //noinspection ForLoopReplaceableByForEach
        for (int i=0; i<patterns.length; i++)
        {
            String p = patterns[i];
            String v = values.getAsString(p);
            displayString = displayString.replaceAll(p, ((v != null) ? v : ""));
        }
        return displayString;
    }

}
