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

import android.content.Context;
import android.graphics.Color;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CalendarGroups
{
    public static final String GROUP_DEFAULT = "DEFAULT";
    public static final String GROUP_SOLSTICE = "SOLSTICE";
    public static final String GROUP_TWILIGHT = "TWILIGHT";
    public static final String GROUP_BLUEGOLD = "BLUEGOLD";
    public static final String GROUP_MOON = "MOON";
    public static final String GROUP_ADDON = "ADDON";

    public static Integer getGroupColor(Context context, @Nullable String[] groups) {
        return getGroupColor(context, ((groups != null && groups.length > 0) ? groups[0] : null));
    }

    @Nullable
    public static Integer getGroupColor(@NonNull Context context, @Nullable String group)
    {
        if (group == null) {
            return Color.TRANSPARENT;
        }
        switch (group)
        {
            case GROUP_DEFAULT:
            case GROUP_SOLSTICE:
            case GROUP_TWILIGHT:
            case GROUP_BLUEGOLD:
            case GROUP_MOON:
            case GROUP_ADDON:
            default: return null;
        }
    }
}
