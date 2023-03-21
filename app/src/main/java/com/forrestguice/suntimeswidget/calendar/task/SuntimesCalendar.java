/**
    Copyright (C) 2020-2023 Forrest Guice
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

import com.forrestguice.suntimeswidget.calendar.CalendarEventStrings;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;

/**
 * @version 0.2.0
 *
 * v0.1.0 initial version
 * v0.2.0 adds columns for default template;
 *        TEMPLATE_TITLE, TEMPLATE_DESCRIPTION, TEMPLATE_LOCATION, TEMPLATE_STRINGS
 */
@SuppressWarnings("Convert2Diamond")
public interface SuntimesCalendar
{
    String COLUMN_CALENDAR_NAME = "calendar_name";             // String (calendar ID)
    String COLUMN_CALENDAR_TITLE = "calendar_title";           // String (display string)
    String COLUMN_CALENDAR_SUMMARY = "calendar_summary";       // String (display string)
    String COLUMN_CALENDAR_COLOR = "calendar_color";           // int (color)

    String COLUMN_CALENDAR_TEMPLATE_TITLE = "template_title";               // String (template title element)
    String COLUMN_CALENDAR_TEMPLATE_DESCRIPTION = "template_description";   // String (template description element)
    String COLUMN_CALENDAR_TEMPLATE_LOCATION = "template_location";         // String (template location element)
    String COLUMN_CALENDAR_TEMPLATE_STRINGS = "template_strings";           // String (template strings)

    String QUERY_CALENDAR_INFO = "calendarInfo";
    String[] QUERY_CALENDAR_INFO_PROJECTION = new String[] {
            COLUMN_CALENDAR_NAME, COLUMN_CALENDAR_TITLE, COLUMN_CALENDAR_SUMMARY, COLUMN_CALENDAR_COLOR,
            COLUMN_CALENDAR_TEMPLATE_TITLE, COLUMN_CALENDAR_TEMPLATE_DESCRIPTION, COLUMN_CALENDAR_TEMPLATE_LOCATION
    };

    String QUERY_CALENDAR_CONTENT = "calendarContent";

    void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings);
    boolean initCalendar(@NonNull SuntimesCalendarSettings settings,
                         @NonNull SuntimesCalendarAdapter adapter,
                         @NonNull SuntimesCalendarTask task,
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
     * @return display color
     */
    int calendarColor();

    /**
     * @return default template
     */
    CalendarEventTemplate defaultTemplate();


    /**
     * @return default strings
     */
    CalendarEventStrings defaultStrings();
}
