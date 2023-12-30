/*
    Copyright (C) 2022 Forrest Guice
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

/**
 * CalendarEventTemplateContract
 * A content-provider that allows addon calendars to access user-defined templates and strings.
 *
 * content://[AUTHORITY]/config                    .. get provider config info (one row)
 * content://[AUTHORITY]/templates                 .. get list of all calendars with templates (multiple rows (string-array))
 * content://[AUTHORITY]/template/[calendarName]   .. get template for given calendar (one row of template elements)
 * content://[AUTHORITY]/strings/[calendarName]    .. get template strings for given calendar (multiple rows (string-array))
 *
 * The `template` and `strings` queries return an empty result if values are still undefined (defaults).
 */
public interface CalendarEventTemplateContract
{
    String AUTHORITY = "com.forrestguice.suntimescalendars.template.provider";
    String READ_PERMISSION = "suntimes.permission.READ_CALCULATOR";
    String VERSION_NAME = "v0.0.0";
    int VERSION_CODE = 0;

    /**
     * CONFIG
     */
    String COLUMN_CONFIG_PROVIDER_VERSION = "provider_version";             // String (provider version string)
    String COLUMN_CONFIG_PROVIDER_VERSION_CODE = "provider_version_code";   // int (provider version code)

    String QUERY_CONFIG = "config";
    String[] QUERY_CONFIG_PROJECTION = new String[] {
            COLUMN_CONFIG_PROVIDER_VERSION, COLUMN_CONFIG_PROVIDER_VERSION_CODE
    };

    /**
     * TEMPLATES
     */

    String COLUMN_TEMPLATE_CALENDAR = "calendar_name";            // String (calendar name)
    String COLUMN_TEMPLATE_TITLE = "template_title";              // String (title element)
    String COLUMN_TEMPLATE_DESCRIPTION = "template_description";  // String (description element)
    String COLUMN_TEMPLATE_LOCATION = "template_location";        // String (location element)
    String COLUMN_TEMPLATE_STRINGS = "template_strings";          // String (template strings)

    /**
     * content://[AUTHORITY]/templates                 .. get list of all calendars with templates
     */
    String QUERY_TEMPLATES = "templates";
    String[] QUERY_TEMPLATES_PROJECTION = new String[] { COLUMN_TEMPLATE_CALENDAR };

    /**
     * content://[AUTHORITY]/template/[calendarName]   .. get template for given calendar
     */
    String QUERY_TEMPLATE = "template";
    String[] QUERY_TEMPLATE_PROJECTION = new String[] { COLUMN_TEMPLATE_CALENDAR,
            COLUMN_TEMPLATE_TITLE, COLUMN_TEMPLATE_DESCRIPTION, COLUMN_TEMPLATE_LOCATION
    };

    /**
     * content://[AUTHORITY]/strings/[calendarName]   .. get strings for given calendar
     */
    String QUERY_STRINGS = "strings";
    String[] QUERY_STRINGS_PROJECTION = new String[] { COLUMN_TEMPLATE_CALENDAR,
            COLUMN_TEMPLATE_STRINGS
    };
}
