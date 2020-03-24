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

import android.support.annotation.NonNull;

import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskProgressInterface;

public class SuntimesCalendarTaskProgress implements SuntimesCalendarTaskProgressInterface
{
    public SuntimesCalendarTaskProgress(int i, int n, String message)
    {
        setProgress(i, n, message);
    }
    public SuntimesCalendarTaskProgress( @NonNull SuntimesCalendarTaskProgressInterface other) {
        setProgress(other.itemNum(), other.getCount(), other.getMessage());
    }

    public void setProgress(int i, int n, String message)
    {
        this.i = i;
        this.n = n;
        this.message = message;
    }

    private int i;
    public int itemNum() {
        return i;
    }

    private int n;
    public int getCount() {
        return n;
    }

    private String message;
    public String getMessage() {
        return message;
    }

    public boolean isIndeterminate()
    {
        return (i == 0 || n == 0);
    }

    public String toString() {
        return message + ": " + i + "/" + n + " (" + (isIndeterminate() ? "true" : "false") + ")";
    }
}
