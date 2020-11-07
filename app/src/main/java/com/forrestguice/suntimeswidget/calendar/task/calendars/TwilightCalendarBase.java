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
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTask;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public abstract class TwilightCalendarBase extends SuntimesCalendarBase implements SuntimesCalendar
{
    protected String s_SUNRISE, s_SUNSET, s_DAWN_TWILIGHT, s_DUSK_TWILIGHT;
    protected String s_CIVIL_TWILIGHT, s_NAUTICAL_TWILIGHT, s_ASTRO_TWILIGHT;
    protected String s_POLAR_TWILIGHT, s_CIVIL_NIGHT, s_NAUTICAL_NIGHT, s_WHITE_NIGHT;

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettings settings)
    {
        super.init(context, settings);
        s_SUNRISE = context.getString(R.string.sunrise);
        s_SUNSET = context.getString(R.string.sunset);
        s_CIVIL_TWILIGHT = context.getString(R.string.timeMode_civil);
        s_NAUTICAL_TWILIGHT = context.getString(R.string.timeMode_nautical);
        s_ASTRO_TWILIGHT = context.getString(R.string.timeMode_astronomical);
        s_POLAR_TWILIGHT = context.getString(R.string.polar_twilight);
        s_CIVIL_NIGHT = context.getString(R.string.civil_night);
        s_NAUTICAL_NIGHT = context.getString(R.string.nautical_night);
        s_DAWN_TWILIGHT = context.getString(R.string.dawn);
        s_DUSK_TWILIGHT = context.getString(R.string.dusk);
        s_WHITE_NIGHT = context.getString(R.string.white_night);
    }

    /**
     *
     * @param context context
     * @param calendarID calender identifier
     * @param cursor a cursor containing columns [rise-start, rise-end, set-start, set-end]
     * @param i index into cursor columns (expects i = 0 (rising), or i = 2 (setting))
     * @param title event title (e.g. Civil Twilight)
     * @param desc0 avg case description (e.g. ending in sunrise, starting at sunset)
     * @param desc1 edge case description (e.g. polar twilight)
     */
    protected void createSunCalendarEvent(Context context, @NonNull SuntimesCalendarAdapter adapter, @NonNull SuntimesCalendarTask task,
                                          ArrayList<ContentValues> values, long calendarID, Cursor cursor, int i, String title, String desc0, String desc1, String desc_fallback)
    {
        int j = i + 1;             // [rise-start, rise-end, set-start, set-end]
        int k = (i == 0) ? 2 : 0;  // rising [i, j, k, l] .. setting [k, l, i, j]
        int l = k + 1;
        String eventDesc;
        Calendar eventStart = Calendar.getInstance();
        Calendar eventEnd = Calendar.getInstance();
        String[] location = task.getLocation();

        if (!cursor.isNull(i) && !cursor.isNull(j))                // avg case [i, j]
        {
            eventStart.setTimeInMillis(cursor.getLong(i));
            eventEnd.setTimeInMillis(cursor.getLong(j));
            //eventDesc = context.getString(R.string.event_at_format, desc0, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
            eventDesc = context.getString(R.string.event_at_format, desc0, location[0]);
            values.add(adapter.createEventContentValues(calendarID, title, eventDesc, location[0], eventStart, eventEnd));

        } else if (!cursor.isNull(i)) {
            eventStart.setTimeInMillis(cursor.getLong(i));
            if (i == 0)
            {
                if (!cursor.isNull(l)) {                          // edge [i, l] of [i, j, k, l]
                    eventEnd.setTimeInMillis(cursor.getLong(l));
                    //eventDesc = context.getString(R.string.event_at_format, desc1, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                    eventDesc = context.getString(R.string.event_at_format, desc1, location[0]);
                    values.add(adapter.createEventContentValues(calendarID, title, eventDesc, location[0], eventStart, eventEnd));
                }

            } else {
                if (cursor.moveToNext())
                {                                // peek forward
                    if (!cursor.isNull(l))
                    {
                        eventEnd.setTimeInMillis(cursor.getLong(l));      // edge [i, +l] of [+k, +l, i, j]
                        //eventDesc = context.getString(R.string.event_at_format, desc1, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        eventDesc = context.getString(R.string.event_at_format, desc1, location[0]);
                        values.add(adapter.createEventContentValues(calendarID, title, eventDesc, location[0], eventStart, eventEnd));

                    } else {                                              // fallback (start-only; end-only events are ignored)
                        //eventDesc = context.getString(R.string.event_at_format, desc_fallback, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        eventDesc = context.getString(R.string.event_at_format, desc_fallback, location[0]);
                        values.add(adapter.createEventContentValues(calendarID, title, eventDesc, location[0], eventStart));
                    }
                    cursor.moveToPrevious();
                }
            }
        }
    }

}
