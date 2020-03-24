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

package com.forrestguice.suntimeswidget.calendar;

import android.content.Context;
import android.util.Log;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendar;

@SuppressWarnings("Convert2Diamond")
public class SuntimesCalendarFactory
{
    public SuntimesCalendar createCalendar(Context context, SuntimesCalendarDescriptor descriptor)
    {
        SuntimesCalendar calendar = null;
        Class calendarClass = null;
        try {
            calendarClass = Class.forName(descriptor.calendarRef());
            calendar = (SuntimesCalendar) calendarClass.newInstance();
            calendar.init(context);

        } catch (ClassNotFoundException e) {
            Log.e(getClass().getSimpleName(), "Failed to createCalendar! " + e);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getSimpleName(), "Failed to createCalendar! " + e);
        } catch (InstantiationException e) {
            Log.e(getClass().getSimpleName(), "Failed to createCalendar! " + e);
        }
        return calendar;
    }
}