/**
    Copyright (C) 2024 Forrest Guice
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CalendarGroups
{
    public static final String GROUP_DEFAULT = "DEFAULT";
    public static final String GROUP_BASIC = "BASIC";
    public static final String GROUP_SOLSTICE = "SOLSTICE";
    public static final String GROUP_TWILIGHT = "TWILIGHT";
    public static final String GROUP_BLUEGOLD = "BLUEGOLD";
    public static final String GROUP_MOON = "MOON";
    public static final String GROUP_ADDON = "ADDON";

    public static final String[] ALL_GROUPS = new String[] {
            GROUP_DEFAULT, GROUP_BASIC, GROUP_SOLSTICE, GROUP_TWILIGHT, GROUP_BLUEGOLD, GROUP_MOON, GROUP_ADDON,
    };

    public static HashMap<String, ArrayList<SuntimesCalendarDescriptor>> createEmptyGroups()
    {
        HashMap<String, ArrayList<SuntimesCalendarDescriptor>> groups = new HashMap<>();
        for (String group : CalendarGroups.ALL_GROUPS) {
            groups.put(group, new ArrayList<SuntimesCalendarDescriptor>());
        }
        return groups;
    }

    public static HashMap<String, ArrayList<SuntimesCalendarDescriptor>> separateIntoGroups(SuntimesCalendarDescriptor... descriptors)
    {
        HashMap<String, ArrayList<SuntimesCalendarDescriptor>> groups = createEmptyGroups();
        for (final SuntimesCalendarDescriptor descriptor : descriptors)
        {
            if (descriptor != null)
            {
                String[] calendarGroups = descriptor.getGroups();
                String calendarGroup = ((groups != null) ? calendarGroups[0] : null);
                ArrayList<SuntimesCalendarDescriptor> group = groups.get(calendarGroup);
                if (group == null) {
                    group = new ArrayList<>();
                }
                group.add(descriptor);
            }
        }
        return groups;
    }

    public static ArrayList<String> sortGroups(List<String> groups)
    {
        ArrayList<String> sortedGroups = new ArrayList<>(groups);
        //sortedGroups.sort(new Comparator<String>() {
        //    public int compare(String s, String t1) {
        //        return Integer.compare(CalendarGroups.getGroupPriority(s), CalendarGroups.getGroupPriority(t1));
        //    }
        //});    // TODO
        return sortedGroups;
    }

    public static int getGroupPriority(@Nullable String group)
    {
        switch (group)
        {
            case GROUP_DEFAULT: return 0;
            case GROUP_BASIC: return 1;
            case GROUP_TWILIGHT: return 2;
            case GROUP_BLUEGOLD: return 3;
            case GROUP_MOON: return 4;
            case GROUP_SOLSTICE:
            case GROUP_ADDON:
            default: return 9;
        }
    }

    public static Integer getGroupColor(Context context, @Nullable String[] groups, boolean enabled) {
        return getGroupColor(context, ((groups != null && groups.length > 0) ? groups[0] : null), enabled);
    }

    @SuppressLint("ResourceType")
    @Nullable
    public static Integer getGroupColor(@NonNull Context context, @Nullable String group, boolean enabled)
    {
        if (group == null || !enabled) {
            return Color.TRANSPARENT;
        }

        int[] colorAttrs = { R.attr.dialogBackground, R.attr.dialogBackgroundAlt };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int bgColor0 = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.dialog_bg_dark));
        int bgColor1 = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.dialog_bg_alt_dark));
        typedArray.recycle();

        switch (group)
        {
            case GROUP_BASIC:
            case GROUP_DEFAULT: return bgColor0;

            case GROUP_SOLSTICE:
            case GROUP_TWILIGHT:
            case GROUP_MOON:
            case GROUP_BLUEGOLD:
            case GROUP_ADDON:
            default: return null;
        }
    }
}
