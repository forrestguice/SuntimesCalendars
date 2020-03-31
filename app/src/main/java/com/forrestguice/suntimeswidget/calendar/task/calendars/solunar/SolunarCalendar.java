// SPDX-License-Identifier: GPL-3.0-or-later
/*
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

package com.forrestguice.suntimeswidget.calendar.task.calendars.solunar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarAdapterInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarSettingsInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarTaskProgressInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class SolunarCalendar implements SuntimesCalendar
{
    private static final String CALENDAR_SOLUNAR = "solunarCalendar";
    private static final int resID_calendarTitle = R.string.calendar_solunar_displayName;
    private static final int resID_calendarSummary = R.string.calendar_solunar_summary;

    private WeakReference<Context> contextRef;

    @Override
    public String calendarName() {
        return CALENDAR_SOLUNAR;
    }

    @Override
    public String calendarTitle() {
        return calendarTitle;
    }
    protected String calendarTitle;

    @Override
    public String calendarSummary() {
        return calendarSummary;
    }
    protected String calendarSummary;

    @Override
    public int calendarColor() {
        return calendarColor;
    }
    protected int calendarColor = Color.MAGENTA;

    @Override
    public void init(@NonNull Context context, @NonNull SuntimesCalendarSettingsInterface settings)
    {
        contextRef = new WeakReference<>(context);
        calendarTitle = context.getString(resID_calendarTitle);
        calendarSummary = context.getString(resID_calendarSummary);
        calendarColor = settings.loadPrefCalendarColor(context, calendarName());
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
                Calendar startDate = Calendar.getInstance();
                startDate.setTimeInMillis(window[0]);

                Calendar endDate = Calendar.getInstance();
                endDate.setTimeInMillis(window[1]);

                /**
                 * COLUMN_SOLUNAR_DATE,
                 * COLUMN_SOLUNAR_RATING,
                        COLUMN_SOLUNAR_SUNRISE, COLUMN_SOLUNAR_SUNSET,
                        COLUMN_SOLUNAR_MOON_ILLUMINATION, COLUMN_SOLUNAR_MOON_PHASE,
                        COLUMN_SOLUNAR_PERIOD_MOONRISE, COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP,
                        COLUMN_SOLUNAR_PERIOD_MOONSET, COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP,
                        COLUMN_SOLUNAR_PERIOD_MOONNOON, COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP,
                        COLUMN_SOLUNAR_PERIOD_MOONNIGHT, COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP,
                        COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH, COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH,
                        COLUMN_SOLUNAR_LOCATION, COLUMN_SOLUNAR_LATITUDE, COLUMN_SOLUNAR_LONGITUDE, COLUMN_SOLUNAR_ALTITUDE, COLUMN_SOLUNAR_TIMEZONE
                 */

                Uri uri = Uri.parse("content://" + SolunarProviderContract.AUTHORITY + "/" + SolunarProviderContract.QUERY_SOLUNAR + "/" + window[0] + "-" + window[1]);
                String[] projection = SolunarProviderContract.QUERY_SOLUNAR_PROJECTION;
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();

                    int c = 0;
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgressInterface progress = task.createProgressObj(c, totalProgress, calendarTitle);
                    task.publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    while (!cursor.isAfterLast() && !task.isCancelled())
                    {
                        long minorLength = cursor.getLong(cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH));
                        long majorLength = cursor.getLong(cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH));
                        double dayRating = cursor.getDouble(cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_RATING));
                        String moonPhase = cursor.getString(cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_MOON_PHASE));
                        double moonIllum = cursor.getDouble(cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_MOON_ILLUMINATION));

                        int[] i_minor = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET) };
                        int[] i_minor_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP) };
                        String[] minorTitles = {"Moonrise", "Moonset"};  // TODO: i18n
                        for (int j=0; j<i_minor.length; j++)
                        {
                            int i = i_minor[j];
                            if (i != -1 && !cursor.isNull(i))
                            {
                                Calendar eventStart = Calendar.getInstance();
                                Calendar eventEnd = Calendar.getInstance();
                                eventStart.setTimeInMillis(cursor.getLong(i));
                                eventEnd.setTimeInMillis(eventStart.getTimeInMillis() + minorLength);

                                String title = minorTitles[j];
                                String desc = "Minor Period" + "\n" + moonPhase + " (" + dayRating + ")";  // TODO: i18n, formatting
                                int overlap = cursor.getInt(i_minor_overlap[j]);
                                if (overlap == 1) {
                                    desc += "\n" + "Close to Sunrise.";  // TODO
                                } else if (overlap == 2) {
                                    desc += "\n" + "Close to Sunset.";  // TODO
                                }
                                eventValues.add(adapter.createEventContentValues(calendarID, title, desc, task.getLocation()[0], eventStart, eventEnd));
                            }
                        }

                        int[] i_major = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT) };
                        int[] i_major_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP) };
                        String[] majorTitles = {"Lunar Noon", "Lunar Midnight"};  // TODO: i18n
                        for (int j=0; j<i_major.length; j++)
                        {
                            int i = i_major[j];
                            if (i != -1 && !cursor.isNull(i))
                            {
                                Calendar eventStart = Calendar.getInstance();
                                Calendar eventEnd = Calendar.getInstance();
                                eventStart.setTimeInMillis(cursor.getLong(i));
                                eventEnd.setTimeInMillis(eventStart.getTimeInMillis() + majorLength);

                                String title = majorTitles[j];
                                String desc = "Major Period" + "\n" + dayRating;  // TODO: i18n, formatting
                                int overlap = cursor.getInt(i_major_overlap[j]);
                                if (overlap == 1) {
                                    desc += "\n" + "Close to Sunrise.";  // TODO
                                } else if (overlap == 2) {
                                    desc += "\n" + "Close to Sunset.";  // TODO
                                }
                                eventValues.add(adapter.createEventContentValues(calendarID, title, desc, task.getLocation()[0], eventStart, eventEnd));
                            }
                        }

                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast())
                        {
                            adapter.createCalendarEvents(eventValues.toArray(new ContentValues[0]));
                            eventValues.clear();
                        }
                        progress.setProgress(c, totalProgress, calendarTitle);
                        task.publishProgress(progress0, progress);
                    }
                    cursor.close();
                    return !task.isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
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

    @Override
    public String lastError() {
        return lastError;
    }
    private String lastError;

}
