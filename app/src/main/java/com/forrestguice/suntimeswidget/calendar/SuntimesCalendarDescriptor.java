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
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@SuppressWarnings("Convert2Diamond")
public class SuntimesCalendarDescriptor implements Comparable
{
    public SuntimesCalendarDescriptor(String name, String title, String summary, int color, int priority, String classRef)
    {
        calendarName = name;
        calendarTitle = title;
        calendarSummary = summary;
        calendarColor = color;
        calendarRef = classRef;
        this.priority = priority;
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

    protected int priority;
    public int getPriority() {
        return priority;
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
    public int compareTo(@NonNull Object other)
    {
        SuntimesCalendarDescriptor otherDescriptor = (SuntimesCalendarDescriptor) other;
        //noinspection UseCompareMethod
        return Integer.valueOf(this.getPriority()).compareTo(otherDescriptor.getPriority());
    }

    public boolean isAddon() {
        return calendarRef != null && calendarRef.startsWith("content:");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ACTION_SUNTIMES_CALENDAR = "suntimes.action.ADD_CALENDAR";
    public static final String CATEGORY_SUNTIMES_CALENDAR = "suntimes.SUNTIMES_CALENDAR";
    public static final String KEY_REFERENCE = "SuntimesCalendarReference";
    public static final String REQUIRED_PERMISSION = "suntimes.permission.READ_CALCULATOR";

    protected static HashMap<String, SuntimesCalendarDescriptor> calendars = new HashMap<>();
    protected static boolean initialized = false;

    public static void initDescriptors(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        Intent packageQuery = new Intent(ACTION_SUNTIMES_CALENDAR);
        packageQuery.addCategory(CATEGORY_SUNTIMES_CALENDAR);
        ArrayList<ResolveInfo> packages = new ArrayList<>(packageManager.queryIntentActivities(packageQuery, PackageManager.GET_META_DATA));
        Log.i("initDescriptors", "Scanning for SuntimesCalendar references... found " + packages.size());

        for (ResolveInfo p : packages)    // display our package first
        {
            if (context.getPackageName().equals(p.activityInfo.packageName))
            {
                packages.remove(p);
                packages.add(0, p);
                Log.i("initDescriptors", "Moved SuntimesCalendar package to front.");
                break;
            }
        }

        int c = 0;
        SuntimesCalendarFactory factory = new SuntimesCalendarFactory();
        for (ResolveInfo resolveInfo : packages)
        {
            if (resolveInfo.activityInfo != null && resolveInfo.activityInfo.metaData != null)
            {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo, resolveInfo.activityInfo))
                    {
                        String metaData = resolveInfo.activityInfo.metaData.getString(KEY_REFERENCE);
                        String[] references = ((metaData != null) ? metaData.replace(" ","").split("\\|") : new String[0]);
                        for (int i=0; i<references.length; i++)
                        {
                            try {
                                SuntimesCalendar calendar = factory.createCalendar(context, references[i]);
                                if (calendar != null)
                                {
                                    SuntimesCalendarDescriptor descriptor = new SuntimesCalendarDescriptor(calendar.calendarName(), calendar.calendarTitle(), calendar.calendarSummary(), calendar.calendarColor(), c, references[i]);
                                    SuntimesCalendarDescriptor.addValue(descriptor);
                                    c++;
                                    Log.i("initDescriptors", "..added " + descriptor.toString());
                                }

                            } catch (SecurityException e) {
                                Log.e("initDescriptors", "Permission denied! " + e);
                            }
                        }

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
        if (packageInfo.requestedPermissions != null)
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

    @Nullable
    public static SuntimesCalendarDescriptor getDescriptor(Context context, String calendarName)
    {
        if (!initialized) {
            initDescriptors(context);
        }
        return calendars.get(calendarName);
    }

    public static SuntimesCalendarDescriptor[] getDescriptors(Context context)
    {
        if (!initialized) {
            initDescriptors(context);
        }
        ArrayList<SuntimesCalendarDescriptor> descriptors = new ArrayList<>(calendars.values());
        Collections.sort(descriptors, null);
        return descriptors.toArray(new SuntimesCalendarDescriptor[0]);
    }

    public static String[] getCalendars(Context context)
    {
        if (!initialized) {
            initDescriptors(context);
        }
        return calendars.keySet().toArray(new String[0]);
    }
}
