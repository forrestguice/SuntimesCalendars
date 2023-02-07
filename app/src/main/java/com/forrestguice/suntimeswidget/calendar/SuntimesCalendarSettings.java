/**
    Copyright (C) 2018-2022 Forrest Guice
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.ui.reminders.Reminder;

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

    public static final String PREF_KEY_CALENDARS_TEMPLATE_TITLE = "app_calendars_template_title_";
    public static final String PREF_KEY_CALENDARS_TEMPLATE_DESC = "app_calendars_template_desc_";
    public static final String PREF_KEY_CALENDARS_TEMPLATE_LOCATION = "app_calendars_template_location_";

    public static final String PREF_KEY_CALENDARS_REMINDER_METHOD = "app_calendars_reminder_method_";
    public static final String PREF_KEY_CALENDARS_REMINDER_MINUTES = "app_calendars_reminder_minutes_";
    public static final String PREF_KEY_CALENDARS_REMINDER_COUNT = "app_calendars_reminder_count_";
    public static final int MAX_REMINDERS = 10;

    public static final String PREF_KEY_CALENDARS_NOTES = "app_calendars_notes_";
    public static final String NOTE_LOCATION_NAME = "location_name";
    public static final String[] ALL_NOTES = new String[] { NOTE_LOCATION_NAME };

    public static final String PREF_KEY_CALENDAR_LASTSYNC = "lastCalendarSync";

    public static final String PREF_KEY_CALENDARS_FIRSTLAUNCH = "app_calendars_firstlaunch";
    public static final String PREF_KEY_CALENDARS_PERMISSIONS = "app_calendars_permissions";

    /**
     * @param context
     * @return timestamp
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
     * @return timestamp
     */
    public static long readLastSyncTime(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(PREF_KEY_CALENDAR_LASTSYNC, -1L);
    }

    /**
     * @param context
     */
    public static void writeLastSyncTime(Context context, long timeInMillis)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_CALENDAR_LASTSYNC, timeInMillis);
        prefs.apply();
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

    /**
     * loadPrefCalendarTemplate
     */
    @Nullable
    public static CalendarEventTemplate loadPrefCalendarTemplate(Context context, String calendar, @NonNull CalendarEventTemplate defaultTemplate)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String title = prefs.getString(PREF_KEY_CALENDARS_TEMPLATE_TITLE + calendar, defaultTemplate.getTitle());
        String desc = prefs.getString(PREF_KEY_CALENDARS_TEMPLATE_DESC + calendar, defaultTemplate.getDesc());
        String location = prefs.getString(PREF_KEY_CALENDARS_TEMPLATE_LOCATION + calendar, defaultTemplate.getLocation());
        return new CalendarEventTemplate(title, desc, location);
    }
    @Nullable
    public static String loadPrefCalendarTemplateTitle(Context context, String calendar)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_CALENDARS_TEMPLATE_TITLE + calendar, null);
    }
    @Nullable
    public static String loadPrefCalendarTemplateDesc(Context context, String calendar)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_CALENDARS_TEMPLATE_DESC + calendar, null);
    }
    @Nullable
    public static String loadPrefCalendarTemplateLocation(Context context, String calendar)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_CALENDARS_TEMPLATE_LOCATION + calendar, null);
    }

    /**
     * savePrefCalendarTemplate
     */
    public static void savePrefCalendarTemplate(Context context, String calendar, CalendarEventTemplate template)
    {
        savePrefCalendarTemplateTitle(context, calendar, template.getTitle());
        savePrefCalendarTemplateDesc(context, calendar, template.getDesc());
        savePrefCalendarTemplateLocation(context, calendar, template.getLocation());
    }
    public static void savePrefCalendarTemplateTitle(Context context, String calendar, @Nullable String title)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_KEY_CALENDARS_TEMPLATE_TITLE + calendar, title);
        prefs.apply();
    }
    public static void savePrefCalendarTemplateDesc(Context context, String calendar, @Nullable String desc) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_KEY_CALENDARS_TEMPLATE_DESC + calendar, desc);
        prefs.apply();
    }
    public static void savePrefCalendarTemplateLocation(Context context, String calendar, @Nullable String location) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_KEY_CALENDARS_TEMPLATE_LOCATION + calendar, location);
        prefs.apply();
    }

    /**
     * loadPrefCalendarReminder
     */
    public static int loadPrefCalendarReminderCount(Context context, String calendar)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_CALENDARS_REMINDER_COUNT + calendar, defaultCalendarReminderCount(context, calendar));
    }
    public static int loadPrefCalendarReminderMethod(Context context, String calendar, int reminderNum)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_CALENDARS_REMINDER_METHOD + reminderNum + "_" + calendar, defaultCalendarReminderMethod(context, calendar, reminderNum));
    }
    public static int loadPrefCalendarReminderMinutes(Context context, String calendar, int reminderNum)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_CALENDARS_REMINDER_MINUTES + reminderNum + "_" + calendar, defaultCalendarReminderMinutes(context, calendar, reminderNum));
    }
    public static Reminder loadPrefCalendarReminder(Context context, String calendar, int reminderNum) {
        return new Reminder(loadPrefCalendarReminderMinutes(context, calendar, reminderNum), loadPrefCalendarReminderMethod(context, calendar, reminderNum));
    }

    /**
     * savePrefCalendarReminder
     */
    public static void savePrefCalendarReminderCount(Context context, String calendar, int count)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KEY_CALENDARS_REMINDER_COUNT + calendar, count);
        prefs.apply();
    }
    public static void savePrefCalendarReminder(Context context, String calendar, int reminderNum, int minutes, int method)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KEY_CALENDARS_REMINDER_MINUTES + reminderNum + "_" + calendar, minutes);
        prefs.putInt(PREF_KEY_CALENDARS_REMINDER_METHOD + reminderNum + "_" + calendar, method);
        prefs.apply();
    }

    public static boolean addCalendarReminder(Context context, String calendar, int minute, int method)
    {
        int n = loadPrefCalendarReminderCount(context, calendar);
        if (n < MAX_REMINDERS)
        {
            savePrefCalendarReminder(context, calendar, n, minute, method);
            savePrefCalendarReminderCount(context, calendar, n+1);
            return true;
        } else return false;
    }

    /**
     * removeCalendarReminders
     */
    public static void removeCalendarReminders(Context context, String calendar)
    {
        int n = loadPrefCalendarReminderCount(context, calendar);
        while (n > 0)
        {
            removeLastCalendarReminder(context, calendar);
            n = loadPrefCalendarReminderCount(context, calendar);
        }
    }

    public static boolean removeLastCalendarReminder(Context context, String calendar)
    {
        int n = loadPrefCalendarReminderCount(context, calendar);
        int reminderNum = n - 1;

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(PREF_KEY_CALENDARS_REMINDER_MINUTES + reminderNum + "_" + calendar);
        prefs.remove(PREF_KEY_CALENDARS_REMINDER_METHOD + reminderNum + "_" + calendar);
        prefs.putInt(PREF_KEY_CALENDARS_REMINDER_COUNT + calendar, (Math.max(reminderNum, 0)));
        prefs.apply();
        return true;
    }

    public static boolean removeCalendarReminder(Context context, String calendar, int reminderNum)
    {
        int n = loadPrefCalendarReminderCount(context, calendar);
        if (reminderNum >= 0 && reminderNum < n)
        {
            for (int i=reminderNum; i<n-1; i++)    // shift entries left (overwrite reminderNum)
            {
                int minutes = loadPrefCalendarReminderMinutes(context, calendar, i+1);
                int method = loadPrefCalendarReminderMethod(context, calendar, i+1);
                savePrefCalendarReminder(context, calendar, i, minutes, method);
            }

            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefs.remove(PREF_KEY_CALENDARS_REMINDER_MINUTES + (n-1) + "_" + calendar);    // remove final entry (now a duplicate)
            prefs.remove(PREF_KEY_CALENDARS_REMINDER_METHOD + (n-1) + "_" + calendar);
            prefs.putInt(PREF_KEY_CALENDARS_REMINDER_COUNT + calendar, (Math.max(n-1, 0)));
            prefs.apply();
            return true;
        }
        return false;
    }

    /**
     * defaultCalendarReminder
     */
    public static int defaultCalendarReminderCount(Context context, String calendar)
    {
        return 0;
    }
    public static int defaultCalendarReminderMethod(Context context, String calendar, int reminderNum)
    {
        switch (reminderNum)
        {
            case 1: case 0: return 0;    // 0; CalendarContract.Reminders.METHOD_DEFAULT
            default: return -1;  // -1; disabled
        }
    }
    public static int defaultCalendarReminderMinutes(Context context, String calendar, int reminderNum)
    {
        switch (reminderNum)
        {
            case 2: return -5;     // 5m after
            case 1: return 5;      // 5m before
            case 0: default: return 0;
        }
    }

    /**
     * loadPrefCalendarColor
     */
    public int loadPrefCalendarColor(Context context, String calendar)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_CALENDARS_COLOR + calendar, defaultCalendarColor(context, calendar));
    }
    public void savePrefCalendarColor(Context context, String calendar, int color)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KEY_CALENDARS_COLOR + calendar, color);
        prefs.apply();
    }

    public static int defaultCalendarColor(Context context, String calendar)
    {
        switch (calendar)
        {
            case SuntimesCalendarAdapter.CALENDAR_SOLSTICE:
                return ContextCompat.getColor(context, R.color.colorSolsticeCalendar);

            case SuntimesCalendarAdapter.CALENDAR_MOONPHASE:
                return ContextCompat.getColor(context, R.color.colorMoonCalendar);

            case SuntimesCalendarAdapter.CALENDAR_MOONAPSIS:
                return ContextCompat.getColor(context, R.color.colorMoonApsisCalendar);

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

    /**
     * @param context context
     * @param calendar calendar name
     * @param key note key (e.g. NOTE_LOCATION)
     * @return the requested note (or null if dne)
     */
    @Nullable
    public String loadCalendarNote(Context context, String calendar, String key)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_CALENDARS_NOTES + calendar + "_" + key, null);
    }
    public void saveCalendarNote(Context context, String calendar, String key, String note)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_KEY_CALENDARS_NOTES + calendar + "_" + key, note);
        prefs.apply();
    }
    public static void clearNotes(Context context, String calendar)
    {
        if (context == null) {
            return;
        }
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (String key : ALL_NOTES) {
            prefs.remove(PREF_KEY_CALENDARS_NOTES + calendar + "_" + key);
            prefs.apply();
        }
    }
}
