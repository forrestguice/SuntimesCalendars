/*
    Copyright (C) 2023-2024 Forrest Guice
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

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.Nullable;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;

/**
 * TemplatePatterns
 */
public enum TemplatePatterns
{
    pattern_cal("%cal", R.string.help_pattern_cal),
    pattern_summary("%summary", R.string.help_pattern_summary),
    pattern_color("%color", R.string.help_pattern_color),

    pattern_loc("%loc", R.string.help_pattern_loc),
    pattern_lat("%lat", R.string.help_pattern_lat),
    pattern_lon("%lon", R.string.help_pattern_lon),
    pattern_lel("%lel", R.string.help_pattern_lel),

    pattern_event("%M", R.string.help_pattern_event),
    pattern_em("%em", R.string.help_pattern_altitude),

    pattern_eA("%eA", R.string.help_pattern_altitude),
    pattern_eZ("%eZ", R.string.help_pattern_azimuth),
    pattern_eD("%eD", R.string.help_pattern_declination),
    pattern_eR("%eR", R.string.help_pattern_rightascension),

    pattern_dist("%dist", R.string.help_pattern_dist),
    pattern_illum("%illum", R.string.help_pattern_illum),
    pattern_phase("%phase", R.string.help_pattern_phase),

    pattern_percent("%%", R.string.help_pattern_percent);

    private final String pattern;
    private final int helpResource;

    private TemplatePatterns(String pattern, int helpResource)
    {
        this.pattern = pattern;
        this.helpResource = helpResource;
    }

    public String getPattern() {
        return pattern;
    }

    public String toString() {
        return pattern;
    }

    public int getHelpResource() {
        return helpResource;
    }
    public String getHelpText(Context context) {
        return context.getString(helpResource);
    }

    public static String getAllHelpText(Context context) {
        return getPatternHelpText(context, TemplatePatterns.values());
    }
    public static String getPatternHelpText(Context context, TemplatePatterns... patterns)
    {
        int c = 8;
        StringBuilder substitutionHelp = new StringBuilder();

        //substitutionHelp.append("<font face='monospace'>");
        for (int i=0; i<patterns.length; i++)
        {
            TemplatePatterns p = patterns[i];
            String pattern = ((p != null) ? p.getPattern() : null);
            if (pattern == null)
            {
                substitutionHelp.append("<br/>");
                continue;
            }

            substitutionHelp.append("<b>").append(pattern).append("</b>").append("&nbsp;");
            for (int j=0; j<(c-pattern.length()); j++) {
                substitutionHelp.append("&nbsp;");
            }

            String patternHelp = p.getHelpText(context);
            substitutionHelp.append(patternHelp)
                    .append("<br/>");
        }
        //substitutionHelp.append("</font");
        return substitutionHelp.toString();
    }

    public static ContentValues createContentValues(@Nullable ContentValues values, SuntimesCalendar calendar)
    {
        if (values == null) {
            values = new ContentValues();
        }
        values.put(pattern_cal.getPattern(), calendar.calendarTitle());
        values.put(pattern_summary.getPattern(), calendar.calendarSummary());
        values.put(pattern_color.getPattern(), calendar.calendarColor());
        return values;
    }

    public static ContentValues createContentValues(@Nullable ContentValues values, SuntimesCalendarDescriptor calendar)
    {
        if (values == null) {
            values = new ContentValues();
        }
        values.put(pattern_cal.getPattern(), calendar.calendarTitle());
        values.put(pattern_summary.getPattern(), calendar.calendarSummary());
        values.put(pattern_color.getPattern(), calendar.calendarColor());
        return values;
    }

    public static ContentValues createContentValues(@Nullable ContentValues values, String[] location)
    {
        if (values == null) {
            values = new ContentValues();
        }
        if (location != null && location.length > 0) {
            values.put(pattern_loc.getPattern(), location[0]);
            if (location.length > 1) {
                values.put(pattern_lat.getPattern(), location[1]);
                if (location.length > 2) {
                    values.put(pattern_lon.getPattern(), location[2]);
                    if (location.length > 3) {
                        values.put(pattern_lel.getPattern(), location[3]);
                    }
                }
            }
        }
        return values;
    }

    public static String replaceSubstitutions(@Nullable String pattern, ContentValues values)
    {
        String displayString = pattern;
        if (pattern != null)
        {
            //noinspection ForLoopReplaceableByForEach
            TemplatePatterns[] patterns = TemplatePatterns.values();
            for (int i=0; i<patterns.length; i++)
            {
                String p = patterns[i].getPattern();
                String v = values.getAsString(p);
                displayString = displayString.replaceAll(p, ((v != null) ? v : ""));
            }
        }
        return displayString;
    }

}
