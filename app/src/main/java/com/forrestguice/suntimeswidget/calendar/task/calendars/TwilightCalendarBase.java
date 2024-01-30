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

package com.forrestguice.suntimeswidget.calendar.task.calendars;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.CalendarEventFlags;
import com.forrestguice.suntimeswidget.calendar.CalendarEventStrings;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTask;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public abstract class TwilightCalendarBase extends SuntimesCalendarBase implements SuntimesCalendar
{
    protected String s_SUNRISE, s_SUNSET, s_DAWN, s_DUSK;
    protected String s_CIVIL_TWILIGHT, s_CIVIL_TWILIGHT_MORNING, s_CIVIL_TWILIGHT_EVENING,
                     s_NAUTICAL_TWILIGHT, s_NAUTICAL_TWILIGHT_MORNING, s_NAUTICAL_TWILIGHT_EVENING,
                     s_ASTRO_TWILIGHT, s_ASTRO_TWILIGHT_MORNING, s_ASTRO_TWILIGHT_EVENING;
    protected String s_POLAR_TWILIGHT, s_CIVIL_NIGHT, s_NAUTICAL_NIGHT, s_WHITE_NIGHT;
    protected String s_CIVIL_DAWN, s_CIVIL_DUSK, s_NAUTICAL_DAWN, s_NAUTICAL_DUSK, s_ASTRO_DAWN, s_ASTRO_DUSK;

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);
        s_SUNRISE = context.getString(R.string.sunrise);
        s_DAWN = context.getString(R.string.dawn);
        s_SUNSET = context.getString(R.string.sunset);
        s_DUSK = context.getString(R.string.dusk);
        s_CIVIL_TWILIGHT = context.getString(R.string.timeMode_civil);
        s_CIVIL_TWILIGHT_MORNING = context.getString(R.string.timeMode_civil_morning);
        s_CIVIL_TWILIGHT_EVENING = context.getString(R.string.timeMode_civil_evening);
        s_CIVIL_DAWN = context.getString(R.string.dawn_civil);
        s_CIVIL_DUSK = context.getString(R.string.dusk_civil);
        s_CIVIL_NIGHT = context.getString(R.string.civil_night);
        s_NAUTICAL_TWILIGHT = context.getString(R.string.timeMode_nautical);
        s_NAUTICAL_TWILIGHT_MORNING = context.getString(R.string.timeMode_nautical_morning);
        s_NAUTICAL_TWILIGHT_EVENING = context.getString(R.string.timeMode_nautical_evening);
        s_NAUTICAL_DAWN = context.getString(R.string.dawn_nautical);
        s_NAUTICAL_DUSK = context.getString(R.string.dusk_nautical);
        s_NAUTICAL_NIGHT = context.getString(R.string.nautical_night);
        s_ASTRO_TWILIGHT = context.getString(R.string.timeMode_astronomical);
        s_ASTRO_TWILIGHT_MORNING = context.getString(R.string.timeMode_astronomical_morning);
        s_ASTRO_TWILIGHT_EVENING = context.getString(R.string.timeMode_astronomical_evening);
        s_ASTRO_DAWN = context.getString(R.string.dawn_astronomical);
        s_ASTRO_DUSK = context.getString(R.string.dusk_astronomical);
        s_POLAR_TWILIGHT = context.getString(R.string.polar_twilight);
        s_WHITE_NIGHT = context.getString(R.string.white_night);
    }

    @Override
    public CalendarEventStrings defaultStrings() {
        return new CalendarEventStrings(s_SUNRISE, s_SUNSET, s_CIVIL_TWILIGHT, s_NAUTICAL_TWILIGHT, s_ASTRO_TWILIGHT, s_POLAR_TWILIGHT, s_CIVIL_NIGHT, s_NAUTICAL_NIGHT, s_DAWN, s_DUSK, s_WHITE_NIGHT,
                s_ASTRO_DAWN, s_NAUTICAL_DAWN, s_CIVIL_DAWN,
                s_CIVIL_DUSK, s_NAUTICAL_DUSK, s_ASTRO_DUSK,
                s_CIVIL_TWILIGHT_MORNING, s_NAUTICAL_TWILIGHT_MORNING, s_ASTRO_TWILIGHT_MORNING,
                s_CIVIL_TWILIGHT_EVENING, s_NAUTICAL_TWILIGHT_EVENING, s_ASTRO_TWILIGHT_EVENING);
    }

    @Override
    public TemplatePatterns[] supportedPatterns()
    {
        return new TemplatePatterns[] {
                TemplatePatterns.pattern_event, null, //TemplatePatterns.pattern_eZ, TemplatePatterns.pattern_eA, TemplatePatterns.pattern_eD, TemplatePatterns.pattern_eR, null,   // TODO: position patterns
                TemplatePatterns.pattern_loc, TemplatePatterns.pattern_lat, TemplatePatterns.pattern_lon, TemplatePatterns.pattern_lel, null,
                TemplatePatterns.pattern_cal, TemplatePatterns.pattern_summary, TemplatePatterns.pattern_color, TemplatePatterns.pattern_percent
        };
    }

    @Override
    public CalendarEventFlags defaultFlags() {
        return new CalendarEventFlags();
    }

    @Override
    public String flagLabel(int i)
    {
        switch (i) {
            case 0: return s_DAWN;
            case 1: return s_DUSK;
            default: return "";
        }
    }

    /**
     *
     * @param context context
     * @param calendarID calender identifier
     * @param cursor a cursor containing columns [rise-start, rise-end, set-start, set-end]
     * @param i index into cursor columns (expects i = 0 (rising), or i = 2 (setting))
     * @param template template
     * @param data template data
     * @param desc0 avg case description (e.g. ending in sunrise, starting at sunset)
     * @param desc1 edge case description (e.g. polar twilight)
     */
    protected void createSunCalendarEvent(Context context, @NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTask task,
                                          ArrayList<ContentValues> values, long calendarID, Cursor cursor, int i, CalendarEventTemplate template, ContentValues data, String desc0, String desc1, String desc_fallback)
    {
        int j = i + 1;             // [rise-start, rise-end, set-start, set-end]
        int k = (i == 0) ? 2 : 0;  // rising [i, j, k, l] .. setting [k, l, i, j]
        int l = k + 1;
        Calendar eventStart = Calendar.getInstance();
        Calendar eventEnd = Calendar.getInstance();

        if (!cursor.isNull(i) && !cursor.isNull(j))                // avg case [i, j]
        {
            eventStart.setTimeInMillis(cursor.getLong(i));
            eventEnd.setTimeInMillis(cursor.getLong(j));
            data.put(TemplatePatterns.pattern_event.getPattern(), desc0);
            values.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventStart, eventEnd));

        } else if (!cursor.isNull(i)) {
            eventStart.setTimeInMillis(cursor.getLong(i));
            if (i == 0)
            {
                if (!cursor.isNull(l)) {                          // edge [i, l] of [i, j, k, l]
                    eventEnd.setTimeInMillis(cursor.getLong(l));
                    data.put(TemplatePatterns.pattern_event.getPattern(), desc1);
                    values.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventStart, eventEnd));
                }

            } else {
                if (cursor.moveToNext())
                {                                // peek forward
                    if (!cursor.isNull(l))
                    {
                        eventEnd.setTimeInMillis(cursor.getLong(l));      // edge [i, +l] of [+k, +l, i, j]
                        data.put(TemplatePatterns.pattern_event.getPattern(), desc1);
                        values.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventStart, eventEnd));

                    } else {                                              // fallback (start-only; end-only events are ignored)
                        data.put(TemplatePatterns.pattern_event.getPattern(), desc_fallback);
                        values.add(adapter.createEventContentValues(calendarID, template.getTitle(data), template.getDesc(data), template.getLocation(data), eventStart));
                    }
                    cursor.moveToPrevious();
                }
            }
        }
    }

}
