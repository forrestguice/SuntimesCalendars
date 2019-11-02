/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimescalendars.R;

public class SuntimesCalendarSettings
{
    public static final String PREF_KEY_CALENDARS_ENABLED = "app_calendars_enabled";
    public static final boolean PREF_DEF_CALENDARS_ENABLED = false;

    public static final String PREF_KEY_CALENDAR_WINDOW0 = "app_calendars_window0";
    public static final String PREF_DEF_CALENDAR_WINDOW0 = "31536000000";  // 1 year

    public static final String PREF_KEY_CALENDAR_WINDOW1 = "app_calendars_window1";
    public static final String PREF_DEF_CALENDAR_WINDOW1 = "63072000000";  // 2 years

    public static final String PREF_KEY_CALENDARS_CALENDAR = "app_calendars_calendar_";
    public static final String PREF_KEY_CALENDARS_COLOR = "app_calendars_color_";

    public static final String PREF_KEY_CALENDARS_FIRSTLAUNCH = "app_calendars_firstlaunch";
    public static final String PREF_KEY_CALENDARS_PERMISSIONS = "app_calendars_permissions";

    /**
     * @param context
     * @return
     */
    public static boolean isFirstLaunch(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_CALENDARS_FIRSTLAUNCH, true);
    }

    /**
     * @param context
     */
    public static void saveFirstLaunch(Context context)
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_CALENDARS_FIRSTLAUNCH, false);
        pref.apply();
    }

    /**
     * @param context
     * @param enabled
     */
    public static void saveCalendarsEnabledPref( Context context, boolean enabled )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_CALENDARS_ENABLED, enabled);
        pref.apply();
    }

    /**
     * @param context
     * @return
     */
    public static boolean loadCalendarsEnabledPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_CALENDARS_ENABLED, PREF_DEF_CALENDARS_ENABLED);
    }

    /**
     * @param context context used to access preferences
     * @return calendarWindow pref (ms value) [past]
     */
    public static long loadPrefCalendarWindow0(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Long.parseLong(prefs.getString(PREF_KEY_CALENDAR_WINDOW0, PREF_DEF_CALENDAR_WINDOW0));
    }

    /**
     * @param context context used to access preferences
     * @return calendarWindow pref (ms value) [future]
     */
    public static long loadPrefCalendarWindow1(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Long.parseLong(prefs.getString(PREF_KEY_CALENDAR_WINDOW1, PREF_DEF_CALENDAR_WINDOW1));
    }

    /**
     * @param context context used to access preferences
     * @return true calendar is enabled, false otherwise
     */
    public static boolean loadPrefCalendarEnabled(Context context, String calendar)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_CALENDARS_CALENDAR + calendar, false);
    }

    public static int loadPrefCalendarColor(Context context, String calendar)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_CALENDARS_COLOR + calendar, defaultCalendarColor(context, calendar));
    }

    public static int defaultCalendarColor(Context context, String calendar)
    {
        switch (calendar)
        {
            case SuntimesCalendarAdapter.CALENDAR_SOLSTICE:
                return ContextCompat.getColor(context, R.color.colorSolsticeCalendar);

            case SuntimesCalendarAdapter.CALENDAR_MOONPHASE:
                return ContextCompat.getColor(context, R.color.colorMoonCalendar);

            case SuntimesCalendarAdapter.CALENDAR_MOONRISE:
                return ContextCompat.getColor(context, R.color.colorMoonriseCalendar);

            case SuntimesCalendarAdapter.CALENDAR_TWILIGHT_ASTRO:
                return ContextCompat.getColor(context, R.color.colorAstroTwilightCalendar);

            case SuntimesCalendarAdapter.CALENDAR_TWILIGHT_NAUTICAL:
                return ContextCompat.getColor(context, R.color.colorNauticalTwilightCalendar);

            case SuntimesCalendarAdapter.CALENDAR_TWILIGHT_CIVIL:
            default:
                return ContextCompat.getColor(context, R.color.colorCivilTwilightCalendar);
        }
    }
}
