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

package com.forrestguice.suntimeswidget.calendar.task;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * SuntimesCalendarTaskItem
 */
public class SuntimesCalendarTaskItem implements Parcelable
{
    public static final int ACTION_UPDATE = 0;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_REMINDERS_UPDATE = 10;
    public static final int ACTION_REMINDERS_DELETE = 12;

    private String calendar;
    private int action;

    public SuntimesCalendarTaskItem(String calendar, int action )
    {
        this.calendar = calendar;
        this.action = action;
    }

    private SuntimesCalendarTaskItem(Parcel in)
    {
        this.calendar = in.readString();
        this.action = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(calendar);
        dest.writeInt(action);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public String getCalendar()
    {
        return calendar;
    }

    public int getAction()
    {
        return action;
    }

    public static final Creator<SuntimesCalendarTaskItem> CREATOR = new Creator<SuntimesCalendarTaskItem>()
    {
        public SuntimesCalendarTaskItem createFromParcel(Parcel in)
        {
            return new SuntimesCalendarTaskItem(in);
        }

        public SuntimesCalendarTaskItem[] newArray(int size)
        {
            return new SuntimesCalendarTaskItem[size];
        }
    };
}
