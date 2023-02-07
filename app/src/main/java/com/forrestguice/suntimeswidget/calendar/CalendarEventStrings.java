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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * CalendarEventStrings
 */
public class CalendarEventStrings implements Parcelable
{
    protected String[] values;

    public CalendarEventStrings() {
        values = new String[0];
    }

    public CalendarEventStrings(String... data) {
        setValues(data);
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public CalendarEventStrings(String[]... data)
    {
        int size = 0;
        for (String[] d : data) {
            size += d.length;
        }

        int c = 0;
        values = new String[size];
        outerLoop:
        for (int i=0; i<data.length; i++)
        {
            String[] d = data[i];
            for (int j=0; j<d.length; j++)
            {
                if (c < size)
                {
                    values[c] = d[j];
                    c++;

                } else {
                    Log.w("CalendarEventStrings", "Out of bounds! " + c + " >= " + size);
                    break outerLoop;
                }
            }
        }
    }

    public CalendarEventStrings(CalendarEventStrings other) {
        setValues(other.values);
    }

    public CalendarEventStrings(Parcel in) {
        values = in.createStringArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(values);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString()
    {
        StringBuilder s = new StringBuilder("[");
        for (int i=0; i<values.length; i++) {
            s.append(values[i]);
            s.append((i == (values.length-1)) ? "]" : ", ");
        }
        return s.toString();
    }

    public static final Creator<CalendarEventStrings> CREATOR = new Creator<CalendarEventStrings>()
    {
        public CalendarEventStrings createFromParcel(Parcel in) {
            return new CalendarEventStrings(in);
        }
        public CalendarEventStrings[] newArray(int size) {
            return new CalendarEventStrings[size];
        }
    };

    public String[] getValues() {
        return values;
    }
    public void setValues(String[] v)
    {
        values = new String[v.length];
        for (int i=0; i<values.length; i++) {
            values[i] = v[i];
        }
    }

    public int getCount() {
        return ((values != null) ? values.length : 0);
    }

    @Nullable
    public String getValue(int i) {
        if (i >= 0 && i < values.length) {
            return values[i];
        } else return null;
    }

    public void setValue(int i, String value) {
        if (i >= 0 && i < values.length) {
            values[i] = value;
        }
    }

}
