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
package com.forrestguice.suntimeswidget.calendar;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Template
 */
public class CalendarEventTemplate implements Parcelable
{
    protected String title;
    protected String body;

    public CalendarEventTemplate(String title, String body)
    {
        this.title = title;
        this.body = body;
    }

    public CalendarEventTemplate(Parcel in)
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

    public static final Creator<CalendarEventTemplate> CREATOR = new Creator<CalendarEventTemplate>()
    {
        public CalendarEventTemplate createFromParcel(Parcel in) {
            return new CalendarEventTemplate(in);
        }
        public CalendarEventTemplate[] newArray(int size) {
            return new CalendarEventTemplate[size];
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
        return TemplatePatterns.replaceSubstitutions(title, data);
    }
    public String getBody(ContentValues data) {
        return TemplatePatterns.replaceSubstitutions(body, data);
    }

}
