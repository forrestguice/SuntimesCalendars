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

import android.content.Context;
import android.support.annotation.NonNull;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;

@SuppressWarnings("Convert2Diamond")
public interface SuntimesCalendar
{
    void init(@NonNull Context context);
    boolean initCalendar(@NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTask task,
                         @NonNull SuntimesCalendarTaskProgress progress0,
                         @NonNull long[] window);

    /**
     * @return last error message encountered during processing (if any)
     */
    String lastError();

    /**
     * @return calendar name / identifier
     */
    String calendarName();

    /**
     * @return display string/title
     */
    String calendarTitle();

    /**
     * @return one-line display summary / subtitle
     */
    String calendarSummary();

    /**
     * @return a longer descriptive summary
     */
    String calendarDescription();

    /**
     * @return display color
     */
    int calendarColor();
}
