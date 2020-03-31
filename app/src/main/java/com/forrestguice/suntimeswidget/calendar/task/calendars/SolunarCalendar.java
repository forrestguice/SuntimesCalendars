// SPDX-License-Identifier: GPL-3.0-or-later
/*
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

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarAdapterInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarSettingsInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskProgressInterface;

import java.lang.ref.WeakReference;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class SolunarCalendar implements SuntimesCalendar
{
    private static final String CALENDAR_SOLUNAR = "solunarCalendar";
    private static final int resID_calendarTitle = R.string.calendar_solunar_displayName;
    private static final int resID_calendarSummary = R.string.calendar_solunar_summary;

    private WeakReference<Context> contextRef;

    @Override
    public String calendarName() {
        return CALENDAR_SOLUNAR;
    }

    @Override
    public String calendarTitle() {
        return calendarTitle;
    }
    protected String calendarTitle;

    @Override
    public String calendarSummary() {
        return calendarSummary;
    }
    protected String calendarSummary;

    @Override
    public int calendarColor() {
        return calendarColor;
    }
    protected int calendarColor = Color.MAGENTA;

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettingsInterface settings)
    {
        contextRef = new WeakReference<>(context);
        calendarTitle = context.getString(resID_calendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettingsInterface settings, @NonNull SuntimesCalendarAdapterInterface adapter, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgressInterface progress0, @NonNull long[] window)
    {
        if (task.isCancelled()) {
            return false;
        }

        String calendarName = calendarName();
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColor);
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Calendar startDate = Calendar.getInstance();
                startDate.setTimeInMillis(window[0]);

                Calendar endDate = Calendar.getInstance();
                endDate.setTimeInMillis(window[1]);

                return false;
            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(getClass().getSimpleName(), lastError);
                return false;
            }
        } else return false;
    }

    @Override
    public String lastError() {
        return lastError;
    }
    private String lastError;

}
