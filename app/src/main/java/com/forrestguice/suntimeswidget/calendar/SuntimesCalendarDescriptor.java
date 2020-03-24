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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("Convert2Diamond")
public class SuntimesCalendarDescriptor implements Comparable
{
    public SuntimesCalendarDescriptor(String name, String title, String summary, int color, String classRef)
    {
        calendarName = name;
        calendarTitle = title;
        calendarSummary = summary;
        calendarColor = color;
        calendarRef = classRef;
    }

    protected String calendarRef;
    public String calendarRef() {
        return calendarRef;
    }

    protected String calendarName;
    public String calendarName() {
        return calendarName;
    }

    protected String calendarTitle;
    public String calendarTitle() {
        return calendarTitle;
    }

    protected String calendarSummary;
    public String calendarSummary() {
        return calendarSummary;
    }

    protected int calendarColor;
    public int calendarColor() {
        return calendarColor;
    }

    protected boolean isPlugin = false;
    public boolean isPlugin()
    {
        return isPlugin;
    }
    public void setIsPlugin( boolean value )
    {
        isPlugin = value;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof SuntimesCalendarDescriptor)) {
            return false;
        } else {
            SuntimesCalendarDescriptor otherDescriptor = (SuntimesCalendarDescriptor) other;
            return this.calendarName().equals(otherDescriptor.calendarName());
        }
    }

    @Override
    public int compareTo(@NonNull Object other) {
        SuntimesCalendarDescriptor otherDescriptor = (SuntimesCalendarDescriptor)other;
        return this.calendarName().compareTo(otherDescriptor.calendarName());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String CALENDAR_TWILIGHT_CIVIL = "civilTwilightCalendar";
    public static final String CALENDAR_SOLSTICE = "solsticeCalendar";
    public static final String CALENDAR_TWILIGHT_NAUTICAL = "nauticalTwilightCalendar";
    public static final String CALENDAR_TWILIGHT_ASTRO = "astroTwilightCalendar";
    public static final String CALENDAR_MOONRISE = "moonriseCalendar";
    public static final String CALENDAR_MOONPHASE = "moonPhaseCalendar";
    public static final String CALENDAR_MOONAPSIS = "moonApsisCalendar";

    public static final String CATEGORY_SUNTIMES_CALENDAR = "com.forrestguice.suntimeswidget.SUNTIMES_CALENDAR";

    public static final String KEY_NAME = "CalendarName";
    public static final String KEY_TITLE = "CalendarTitle";
    public static final String KEY_SUMMARY = "CalendarSummary";
    public static final String KEY_COLOR = "CalendarColor";
    public static final String KEY_REFERENCE = "CalendarReference";

    public static final String REQUIRED_PERMISSION = "suntimes.permission.READ_CALCULATOR";

    protected static HashMap<String, SuntimesCalendarDescriptor> calendars = new HashMap<>();
    protected static boolean initialized = false;

    public static void initDescriptors(Context context)
    {
        //SolsticeCalendar solsticeCalendar = new SolsticeCalendar();
        //solsticeCalendar.init(context);
        //SuntimesCalendarDescriptor.addValue(solsticeCalendar.getDescriptor());

        PackageManager packageManager = context.getPackageManager();
        Intent packageQuery = new Intent(Intent.ACTION_RUN);    // get a list of installed plugins
        packageQuery.addCategory(CATEGORY_SUNTIMES_CALENDAR);
        List<ResolveInfo> packages = packageManager.queryIntentActivities(packageQuery, PackageManager.GET_META_DATA);
        Log.i("initDescriptors", "Scanning for calendar plugins... found " + packages.size());

        for (ResolveInfo resolveInfo : packages)
        {
            if (resolveInfo.activityInfo != null && resolveInfo.activityInfo.metaData != null)
            {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo, resolveInfo.activityInfo))
                    {
                        String calendarName = resolveInfo.activityInfo.metaData.getString(KEY_NAME);
                        String calendarTitle = resolveInfo.activityInfo.metaData.getString(KEY_TITLE);
                        String calendarSummary = resolveInfo.activityInfo.metaData.getString(KEY_SUMMARY);
                        int calendarColor = resolveInfo.activityInfo.metaData.getInt(KEY_COLOR);
                        String calendarReference = resolveInfo.activityInfo.metaData.getString(KEY_REFERENCE);

                        SuntimesCalendarDescriptor descriptor = new SuntimesCalendarDescriptor(calendarName, calendarTitle, calendarSummary, calendarColor, calendarReference);
                        descriptor.setIsPlugin(true);
                        SuntimesCalendarDescriptor.addValue(descriptor);
                        Log.i("initDescriptors", "..initialized plugin: " + descriptor.toString());

                    } else {
                        Log.w("initDescriptors", "Permission denied! " + packageInfo.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("initDescriptors", "Package not found! " + e);
                }
            }
        }
        initialized = true;
    }

    private static boolean hasPermission(@NonNull PackageInfo packageInfo, @NonNull ActivityInfo activityInfo)
    {
        boolean hasPermission = false;
        if (packageInfo.requestedPermissions != null && activityInfo.permission != null &&     // the package should request permission
                activityInfo.permission.equals(REQUIRED_PERMISSION))                           // and activity should require permission
        {
            for (String permission : packageInfo.requestedPermissions) {
                if (permission != null && permission.equals(REQUIRED_PERMISSION)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }

    public static void addValue( SuntimesCalendarDescriptor calendar )
    {
        if (!calendars.containsKey(calendar.calendarName())) {
            calendars.put(calendar.calendarName(), calendar);
        }
    }

    public static void removeValue( SuntimesCalendarDescriptor calendar ) {
        calendars.remove(calendar.calendarName);
    }

    public static HashMap<String, SuntimesCalendarDescriptor> values(Context context)
    {
        if (!initialized) {
            initDescriptors(context);
        }
        return new HashMap<>(calendars);
    }

    public static SuntimesCalendarDescriptor getDescriptor(Context context, String calendarName)
    {
        if (!initialized) {
            initDescriptors(context);
        }
        return calendars.get(calendarName);
    }

    public static String[] getCalendars(Context context)
    {
        if (!initialized) {
            initDescriptors(context);
        }
        return calendars.keySet().toArray(new String[0]);
    }
}
