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

/**
 * @version 0.1.0
 */
public interface SuntimesCalendarSettingsInterface
{
    public static final String PREF_KEY_CALENDARS_ENABLED = "app_calendars_enabled";
    public static final boolean PREF_DEF_CALENDARS_ENABLED = false;

    public static final String PREF_KEY_CALENDAR_WINDOW0 = "app_calendars_window0";
    public static final String PREF_DEF_CALENDAR_WINDOW0 = "31536000000";  // 1 year

    public static final String PREF_KEY_CALENDAR_WINDOW1 = "app_calendars_window1";
    public static final String PREF_DEF_CALENDAR_WINDOW1 = "63072000000";  // 2 years

    public static final String PREF_KEY_CALENDARS_CALENDAR = "app_calendars_calendar_";
    public static final String PREF_KEY_CALENDARS_COLOR = "app_calendars_color_";

    public static final String PREF_KEY_CALENDARS_NOTES = "app_calendars_notes_";
    public static final String NOTE_LOCATION_NAME = "location_name";
    public static final String[] ALL_NOTES = new String[] { NOTE_LOCATION_NAME };

    public static final String PREF_KEY_CALENDAR_LASTSYNC = "lastCalendarSync";

    public static final String PREF_KEY_CALENDARS_FIRSTLAUNCH = "app_calendars_firstlaunch";
    public static final String PREF_KEY_CALENDARS_PERMISSIONS = "app_calendars_permissions";

    int loadPrefCalendarColor(Context context, String calendar);
    public void savePrefCalendarColor(Context context, String calendar, int color);

    public String loadCalendarNote(Context context, String calendar, String key);
    void saveCalendarNote(Context context, String calendar, String key, String value);
}
