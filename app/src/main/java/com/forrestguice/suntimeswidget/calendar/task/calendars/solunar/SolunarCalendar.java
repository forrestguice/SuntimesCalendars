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
import android.support.v4.content.ContextCompat;
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
import java.util.HashMap;

@SuppressWarnings("Convert2Diamond")
public class SolunarCalendar implements SuntimesCalendar
{
    private static final String CALENDAR_SOLUNAR = "solunarCalendar";
    private static final int resID_calendarTitle = R.string.calendar_solunar_displayName;
    private static final int resID_calendarSummary = R.string.calendar_solunar_summary;

    private WeakReference<Context> contextRef;

    private String majorTitle;
    private String minorTitle;
    private String descPattern;
    private String[] overlapDisplay;

    private String[] minorTitles;
    private String[] minorDesc;
    private String[] majorTitles;
    private String[] majorDesc;

    private String[] ratingLabels;
    private int[] ratingBrackets;
    private HashMap<String, String> moonPhaseDisplay;

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

        majorTitle = context.getString(R.string.solunarevent_title_major);
        majorTitles = new String[] {majorTitle, majorTitle};
        minorTitle = context.getString(R.string.solunarevent_title_minor);
        minorTitles = new String[] {minorTitle, minorTitle};

        overlapDisplay = context.getResources().getStringArray(R.array.solunarevent_overlap);
        ratingLabels = context.getResources().getStringArray(R.array.solunarevent_ratings_labels);
        ratingBrackets = context.getResources().getIntArray(R.array.solunarevent_ratings_brackets);

        String lunarRise = context.getString(R.string.solunarevent_lunar_rise);
        String lunarSet = context.getString(R.string.solunarevent_lunar_set);
        String lunarNoon = context.getString(R.string.solunarevent_lunar_noon);
        String lunarNight = context.getString(R.string.solunarevent_lunar_night);
        descPattern = context.getString(R.string.solunarevent_desc_pattern1);
        minorDesc = new String[] {context.getString(R.string.solunarevent_desc_pattern0, lunarRise, descPattern), context.getString(R.string.solunarevent_desc_pattern0, lunarSet, descPattern)};
        majorDesc = new String[] {context.getString(R.string.solunarevent_desc_pattern0, lunarNoon, descPattern), context.getString(R.string.solunarevent_desc_pattern0, lunarNight, descPattern)};

        moonPhaseDisplay = new HashMap<>();
        moonPhaseDisplay.put("NEW", context.getString(R.string.timeMode_moon_new));
        moonPhaseDisplay.put("WAXING_CRESCENT", context.getString(R.string.timeMode_moon_waxingcrescent));
        moonPhaseDisplay.put("FIRST_QUARTER", context.getString(R.string.timeMode_moon_firstquarter));
        moonPhaseDisplay.put("WAXING_GIBBOUS", context.getString(R.string.timeMode_moon_waxinggibbous));
        moonPhaseDisplay.put("FULL", context.getString(R.string.timeMode_moon_full));
        moonPhaseDisplay.put("WANING_GIBBOUS", context.getString(R.string.timeMode_moon_waninggibbous));
        moonPhaseDisplay.put("THIRD_QUARTER", context.getString(R.string.timeMode_moon_thirdquarter));
        moonPhaseDisplay.put("WANING_CRESCENT", context.getString(R.string.timeMode_moon_waningcrescent));
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
                int c = 0;
                int totalProgress = (int)(((window[1] - window[0]) / CHUNK_MILLIS) * CHUNK_DAYS);
                task.publishProgress(progress0, task.createProgressObj(0, totalProgress, calendarTitle));
                SuntimesCalendarTaskParams params = new SuntimesCalendarTaskParams(settings, adapter, task, progress0, calendarID, window);

                long start = window[0];
                for (long i = window[0]; i < window[1]; i += DAY_MILLIS)
                {
                    if ((i - start) > CHUNK_MILLIS)
                    {
                        if (!readCursor(params, queryCursor(resolver, new long[] {start, i}), c, totalProgress)) {
                            return false;
                        }
                        c += CHUNK_DAYS;
                        start = i;
                    }
                }
                return true;

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(getClass().getSimpleName(), lastError);
                return false;
            }
        } else return false;
    }

    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
    public static final int CHUNK_DAYS = 7;
    public static final long CHUNK_MILLIS = CHUNK_DAYS * DAY_MILLIS;

    /**
     * queryCursor
     */
    private Cursor queryCursor(ContentResolver resolver, long[] window)
    {
        Uri uri = Uri.parse("content://" + SolunarProviderContract.AUTHORITY + "/" + SolunarProviderContract.QUERY_SOLUNAR + "/" + window[0] + "-" + window[1]);
        String[] projection = SolunarProviderContract.QUERY_SOLUNAR_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        if (cursor == null) {
            lastError = "Failed to resolve URI! " + uri;
            Log.e(getClass().getSimpleName(), lastError);
        }
        return cursor;
    }

    /**
     * readCursor
     */
    private boolean readCursor(SuntimesCalendarTaskParams params, Cursor cursor, int c, int totalProgress)
    {
        if (cursor == null) {
            return false;
        }
        cursor.moveToFirst();

        //int c = 0;
        //int totalProgress = cursor.getCount();
        SuntimesCalendarTaskProgressInterface progress = params.task.createProgressObj(c, totalProgress, calendarTitle);
        params.task.publishProgress(params.progress, progress);

        int[] i_minor = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET) };
        int[] i_minor_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP) };

        int[] i_major = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT) };
        int[] i_major_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP) };

        int i_minor_length = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH);
        int i_major_length = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH);
        int i_dayRating = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_RATING);
        int i_moonPhase = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_MOON_PHASE);
        //int i_moonIllum = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_MOON_ILLUMINATION);

        ArrayList<ContentValues> eventValues = new ArrayList<>();
        while (!cursor.isAfterLast() && !params.task.isCancelled())
        {
            double dayRating = cursor.getDouble(i_dayRating);
            String dayRatingDisplay = formatRating(dayRating);

            String phase = cursor.getString(i_moonPhase);
            String phaseDisplay = moonPhaseDisplay.get(phase);
            //double moonIllum = cursor.getDouble(i_moonIllum);

            for (int i=0; i<2; i++)
            {
                String minor_overlap = cursor.getString(i_minor_overlap[i]);
                String major_overlap = cursor.getString(i_major_overlap[i]);
                minorDesc[i] = String.format(minorDesc[i], phaseDisplay, dayRatingDisplay, minor_overlap + "(" + i_minor_overlap[i] + ") boogers");
                majorDesc[i] = String.format(majorDesc[i], phaseDisplay, dayRatingDisplay, major_overlap + "(" + i_major_overlap[i] + ") boogers");
            }

            addPeriods(params, eventValues, cursor, i_minor, minorTitles, minorDesc, cursor.getLong(i_minor_length));
            addPeriods(params, eventValues, cursor, i_major, majorTitles, majorDesc, cursor.getLong(i_major_length));
            cursor.moveToNext();
            c++;

            if (c % 128 == 0 || cursor.isLast())
            {
                params.adapter.createCalendarEvents(eventValues.toArray(new ContentValues[0]));
                eventValues.clear();
            }
            progress.setProgress(c, totalProgress, calendarTitle);
            params.task.publishProgress(params.progress, progress);
        }
        cursor.close();
        return !params.task.isCancelled();
    }

    /**
     * addPeriods
     */
    private void addPeriods( @NonNull SuntimesCalendarTaskParams params, @NonNull ArrayList<ContentValues> eventValues, @NonNull Cursor cursor,
                             int[] index, String[] titles, String[] desc, long periodLength )
    {
        for (int j=0; j<index.length; j++)
        {
            int i = index[j];
            if (i != -1 && !cursor.isNull(i))
            {
                Calendar eventStart = Calendar.getInstance();
                Calendar eventEnd = Calendar.getInstance();
                eventStart.setTimeInMillis(cursor.getLong(i));
                eventEnd.setTimeInMillis(eventStart.getTimeInMillis() + periodLength);
                eventValues.add(params.adapter.createEventContentValues(params.calendarID, titles[j], desc[j], params.task.getLocation()[0], eventStart, eventEnd));
            }
        }
    }


    private String formatRating(double rating)
    {
        if (ratingBrackets.length != ratingLabels.length) {
            throw new ArrayIndexOutOfBoundsException("length of ratings_labels and ratings_brackets don't match");
        }

        int last = -1;
        for (int i=0; i<ratingBrackets.length; i++)
        {
            if (rating > (last * 0.01d)
                    && rating <= (ratingBrackets[i] * 0.01d)) {
                return ratingLabels[i];
            }
            last = ratingBrackets[i];
        }
        return "";
    }

    @Override
    public String lastError() {
        return lastError;
    }
    private String lastError;

    /**
     * SuntimesCalendarTaskParams
     */
    public static class SuntimesCalendarTaskParams
    {
        public SuntimesCalendarSettingsInterface settings;
        public SuntimesCalendarAdapterInterface adapter;
        public SuntimesCalendarTaskInterface task;
        public SuntimesCalendarTaskProgressInterface progress;
        public long calendarID;
        public long[] window;

        public SuntimesCalendarTaskParams(@NonNull SuntimesCalendarSettingsInterface settings, @NonNull SuntimesCalendarAdapterInterface adapter,
                                          @NonNull SuntimesCalendarTaskInterface task, @NonNull SuntimesCalendarTaskProgressInterface progress,
                                          long calendarID, @NonNull long[] window)
        {
            this.settings = settings;
            this.adapter = adapter;
            this.task = task;
            this.progress = progress;
            this.calendarID = calendarID;
            this.window = window;
        }
    }

}
