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

package com.forrestguice.suntimeswidget.calendar.intf;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @version 0.1.0
 */
@SuppressWarnings("Convert2Diamond")
public interface SuntimesCalendar
{
    void init( @NonNull Context context, @NonNull SuntimesCalendarSettingsInterface settings );
    boolean initCalendar( @NonNull SuntimesCalendarSettingsInterface settings,
                          @NonNull SuntimesCalendarAdapterInterface adapter,
                          @NonNull SuntimesCalendarTaskInterface task,
                          @NonNull SuntimesCalendarTaskProgressInterface progress0,
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
     * @return display color
     */
    int calendarColor();
}
