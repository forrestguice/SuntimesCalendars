/**
    Copyright (C) 2018-2020 Forrest Guice
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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarAdapterInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarSettingsInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskProgressInterface;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class MoonriseCalendar extends MoonCalendarBase implements SuntimesCalendar
{
    private static final String CALENDAR_NAME = SuntimesCalendarAdapterInterface.CALENDAR_MOONRISE;
    private static final int resID_calendarTitle = R.string.calendar_moonrise_displayName;
    private static final int resID_calendarSummary = R.string.calendar_moonrise_summary;

    public static SuntimesCalendarDescriptor getDescriptor(Context context, @NonNull SuntimesCalendarSettingsInterface settings)
    {
        return new SuntimesCalendarDescriptor( CALENDAR_NAME, context.getString(resID_calendarTitle), context.getString(resID_calendarSummary),
                settings.loadPrefCalendarColor(context, CALENDAR_NAME), MoonriseCalendar.class.getName() );
    }

    private String[] moonStrings = new String[2];      // {moonrise, moonset}

    @Override
    public String calendarName() {
        return CALENDAR_NAME;
    }

    @Override
    public void init(@NonNull Context context, SuntimesCalendarSettingsInterface settings)
    {
        super.init(context, settings);

        calendarTitle = context.getString(resID_calendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarDesc = null;
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());

        moonStrings[0] = context.getString(R.string.moonrise);
        moonStrings[1] = context.getString(R.string.moonset);
    }

    @Override
    public boolean initCalendar(@NonNull SuntimesCalendarSettingsInterface settings, @NonNull SuntimesCalendarAdapterInterface adapter, @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgressInterface progress0, @NonNull long[] window)
    {
        if (task.isCancelled()) {
            return false;
        }

        String calendarName = calendarName();
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColor);
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri moonUri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON + "/" + window[0] + "-" + window[1]);
                String[] moonProjection = new String[] { CalculatorProviderContract.COLUMN_MOON_RISE, CalculatorProviderContract.COLUMN_MOON_SET };
                Cursor moonCursor = resolver.query(moonUri, moonProjection, null, null, null);
                if (moonCursor != null)
                {
                    String[] location = task.getLocation();
                    settings.saveCalendarNote(context, calendarName, SuntimesCalendarSettingsInterface.NOTE_LOCATION_NAME, location[0]);

                    int c = 0;
                    int totalProgress = moonCursor.getCount();
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, location[0]);
                    SuntimesCalendarTaskProgressInterface progress = task.createProgressObj(c, totalProgress, progressTitle);
                    task.publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    String title, desc;
                    moonCursor.moveToFirst();
                    while (!moonCursor.isAfterLast() && !task.isCancelled())
                    {
                        for (int i=0; i<moonProjection.length; i++)
                        {
                            if (!moonCursor.isNull(i))
                            {
                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(moonCursor.getLong(i));
                                title = moonStrings[i];
                                //desc = context.getString(R.string.event_at_format, moonStrings[i], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                                desc = context.getString(R.string.event_at_format, moonStrings[i], location[0]);
                                eventValues.add(adapter.createEventContentValues(calendarID, title, desc, location[0], eventTime));
                                //Log.d("DEBUG", "create event: " + moonStrings[i] + " at " + eventTime.toString());
                            }
                        }
                        moonCursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || moonCursor.isLast()) {
                            adapter.createCalendarEvents( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || moonCursor.isLast()) {
                            progress.setProgress(c, totalProgress, progressTitle);
                            task.publishProgress(progress0, progress);
                        }
                    }
                    moonCursor.close();
                    return !task.isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + moonUri;
                    Log.e(getClass().getSimpleName(), lastError);
                    return false;
                }

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(getClass().getSimpleName(), lastError);
                return false;
            }
        } else return false;
    }



}
