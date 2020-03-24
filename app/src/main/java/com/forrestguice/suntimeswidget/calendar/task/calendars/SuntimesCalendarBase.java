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

import android.content.Context;
import android.support.annotation.NonNull;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarAdapterInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskProgressInterface;

import java.lang.ref.WeakReference;

@SuppressWarnings("Convert2Diamond")
public abstract class SuntimesCalendarBase implements SuntimesCalendar
{
    protected WeakReference<Context> contextRef = null;
    protected String calendarTitle, calendarSummary, calendarDesc;
    protected int calendarColor;
    protected String lastError;

    @Override
    public void init(@NonNull Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarAdapterInterface adapter, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgressInterface progress0, @NonNull long[] window) {
        return false;
    }

    @Override
    public String lastError() {
        return lastError;
    }

    @Override
    public abstract String calendarName();

    @Override
    public String calendarTitle() {
        return calendarTitle;
    }

    @Override
    public String calendarSummary() {
        return calendarSummary;
    }

    @Override
    public String calendarDescription() {
        return calendarDesc;
    }

    @Override
    public int calendarColor() {
        return calendarColor;
    }

    public SuntimesCalendarDescriptor getDescriptor() {
        return new SuntimesCalendarDescriptor(calendarName(), calendarTitle, calendarSummary, calendarColor, getClass().getName());
    }
}
