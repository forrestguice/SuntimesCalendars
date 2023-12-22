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
 * CalendarEvents
 */
public class CalendarEventFlags implements Parcelable
{
    protected boolean[] values;

    public CalendarEventFlags() {
        values = new boolean[0];
    }

    public CalendarEventFlags(String... data)
    {
        boolean[] v = new boolean[data.length];
        for (int i=0; i<data.length; i++) {
            v[i] = Boolean.parseBoolean(data[i]);
        }
        setValues(v);
    }

    public CalendarEventFlags(boolean... data) {
        setValues(data);
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public CalendarEventFlags(boolean[]... data)
    {
        int size = 0;
        for (boolean[] d : data) {
            size += d.length;
        }

        int c = 0;
        values = new boolean[size];
        outerLoop:
        for (int i=0; i<data.length; i++)
        {
            boolean[] d = data[i];
            for (int j=0; j<d.length; j++)
            {
                if (c < size)
                {
                    values[c] = d[j];
                    c++;

                } else {
                    Log.w("CalendarEvents", "Out of bounds! " + c + " >= " + size);
                    break outerLoop;
                }
            }
        }
    }

    public CalendarEventFlags(CalendarEventFlags other) {
        setValues(other.values);
    }

    public CalendarEventFlags(Parcel in) {
        values = in.createBooleanArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(values);
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

    public static final Creator<CalendarEventFlags> CREATOR = new Creator<CalendarEventFlags>()
    {
        public CalendarEventFlags createFromParcel(Parcel in) {
            return new CalendarEventFlags(in);
        }
        public CalendarEventFlags[] newArray(int size) {
            return new CalendarEventFlags[size];
        }
    };

    public boolean[] getValues() {
        return values;
    }
    public void setValues(boolean[] v)
    {
        values = new boolean[v.length];
        System.arraycopy(v, 0, values, 0, values.length);
    }

    public int getCount() {
        return ((values != null) ? values.length : 0);
    }

    @Nullable
    public Boolean getValue(int i) {
        if (i >= 0 && i < values.length) {
            return values[i];
        } else return null;
    }

    public void setValue(int i, boolean value) {
        if (i >= 0 && i < values.length) {
            values[i] = value;
        }
    }

    public boolean isEmpty() {
        return (values == null || values.length == 0);
    }

}
