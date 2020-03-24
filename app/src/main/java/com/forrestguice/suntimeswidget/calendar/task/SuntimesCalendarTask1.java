/**
    Copyright (C) 2018-2019 Forrest Guice
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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.intf.SuntimesCalendarAdapterInterface;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * legacy task
 */
@SuppressWarnings("Convert2Diamond")
public class SuntimesCalendarTask1 extends SuntimesCalendarTaskBase
{
    public static final String TAG = "SuntimesCalendarTask";

    public static final String[] ALL_CALENDARS = new String[] {SuntimesCalendarAdapterInterface.CALENDAR_SOLSTICE, SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE, SuntimesCalendarAdapterInterface.CALENDAR_MOONAPSIS, SuntimesCalendarAdapterInterface.CALENDAR_MOONRISE, SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_CIVIL, SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_NAUTICAL, SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_ASTRO};

    public static final double THRESHHOLD_SUPERMOON = 360000;    // km
    public static final double THRESHHOLD_MICROMOON = 405000;    // km

    private HashMap<String, String> calendarDisplay = new HashMap<>();
    private HashMap<String, Integer> calendarColors = new HashMap<>();

    private String[] moonStrings = new String[2];      // {moonrise, moonset}
    private String[] phaseStrings = new String[4];     // {major phases}
    private String[] phaseStrings1 = new String[4];    // {major phases; supermoon}
    private String[] phaseStrings2 = new String[4];    // {major phases; micromoon}
    private String[] apsisStrings = new String[2];    // {apogee, perigee}
    private String[] solsticeStrings = new String[4];  // {spring, summer, fall, winter}
    //private int[] solsticeColors = new int[4];

    private String s_SUNRISE, s_SUNSET, s_DAWN_TWILIGHT, s_DUSK_TWILIGHT;
    private String s_CIVIL_TWILIGHT, s_NAUTICAL_TWILIGHT, s_ASTRO_TWILIGHT;
    private String s_POLAR_TWILIGHT, s_CIVIL_NIGHT, s_NAUTICAL_NIGHT, s_WHITE_NIGHT;

    public SuntimesCalendarTask1(Context context)
    {
        super(context);
        contextRef = new WeakReference<Context>(context);
        adapter = new SuntimesCalendarAdapter(context.getContentResolver(), SuntimesCalendarDescriptor.getCalendars(context));
        calendarWindow0 = SuntimesCalendarSettings.loadPrefCalendarWindow0(context);
        calendarWindow1 = SuntimesCalendarSettings.loadPrefCalendarWindow1(context);

        SuntimesCalendarSettings settings = new SuntimesCalendarSettings();

        // solstice calendar resources
        calendarDisplay.put(SuntimesCalendarAdapterInterface.CALENDAR_SOLSTICE, context.getString(R.string.calendar_solstice_displayName));
        calendarColors.put(SuntimesCalendarAdapterInterface.CALENDAR_SOLSTICE, settings.loadPrefCalendarColor(context, SuntimesCalendarAdapterInterface.CALENDAR_SOLSTICE));

        solsticeStrings[0] = context.getString(R.string.timeMode_equinox_vernal);
        solsticeStrings[1] = context.getString(R.string.timeMode_solstice_summer);
        solsticeStrings[2] = context.getString(R.string.timeMode_equinox_autumnal);
        solsticeStrings[3] = context.getString(R.string.timeMode_solstice_winter);

        phaseStrings[0] = context.getString(R.string.timeMode_moon_new);
        phaseStrings[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings[2] = context.getString(R.string.timeMode_moon_full);
        phaseStrings[3] = context.getString(R.string.timeMode_moon_thirdquarter);

        phaseStrings1[0] = context.getString(R.string.timeMode_moon_supernew);
        phaseStrings1[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings1[2] = context.getString(R.string.timeMode_moon_superfull);
        phaseStrings1[3] = context.getString(R.string.timeMode_moon_thirdquarter);

        phaseStrings2[0] = context.getString(R.string.timeMode_moon_micronew);
        phaseStrings2[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings2[2] = context.getString(R.string.timeMode_moon_microfull);
        phaseStrings2[3] = context.getString(R.string.timeMode_moon_thirdquarter);

        apsisStrings[0] = context.getString(R.string.timeMode_moon_apogee);
        apsisStrings[1] = context.getString(R.string.timeMode_moon_perigee);

        //solsticeColors[0] = ContextCompat.getColor(context, R.color.springColor_light);
        //solsticeColors[1] = ContextCompat.getColor(context, R.color.summerColor_light);
        //solsticeColors[2] = ContextCompat.getColor(context, R.color.fallColor_light);
        //solsticeColors[3] = ContextCompat.getColor(context, R.color.winterColor_light);

        // sunrise, sunset calendar resources
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

        calendarDisplay.put(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_CIVIL, context.getString(R.string.calendar_civil_twilight_displayName));
        calendarColors.put(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_CIVIL, settings.loadPrefCalendarColor(context, SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_CIVIL));

        calendarDisplay.put(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_NAUTICAL, context.getString(R.string.calendar_nautical_twilight_displayName));
        calendarColors.put(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_NAUTICAL, settings.loadPrefCalendarColor(context, SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_NAUTICAL));

        calendarDisplay.put(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_ASTRO, context.getString(R.string.calendar_astronomical_twilight_displayName));
        calendarColors.put(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_ASTRO, settings.loadPrefCalendarColor(context, SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_ASTRO));

        // moonrise, moonset calendar resources
        moonStrings[0] = context.getString(R.string.moonrise);
        moonStrings[1] = context.getString(R.string.moonset);

        calendarDisplay.put(SuntimesCalendarAdapterInterface.CALENDAR_MOONRISE, context.getString(R.string.calendar_moonrise_displayName));
        calendarColors.put(SuntimesCalendarAdapterInterface.CALENDAR_MOONRISE, settings.loadPrefCalendarColor(context, SuntimesCalendarAdapterInterface.CALENDAR_MOONRISE));

        // moon phase calendar resources
        calendarDisplay.put(SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE, context.getString(R.string.calendar_moonPhase_displayName));
        calendarColors.put(SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE, settings.loadPrefCalendarColor(context, SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE));

        calendarDisplay.put(SuntimesCalendarAdapterInterface.CALENDAR_MOONAPSIS, context.getString(R.string.calendar_moonApsis_displayName));
        calendarColors.put(SuntimesCalendarAdapterInterface.CALENDAR_MOONAPSIS, settings.loadPrefCalendarColor(context, SuntimesCalendarAdapterInterface.CALENDAR_MOONAPSIS));

        notificationMsgAdding = context.getString(R.string.calendars_notification_adding);
        notificationMsgAdded = context.getString(R.string.calendars_notification_added);
        notificationMsgClearing = context.getString(R.string.calendars_notification_clearing);
        notificationMsgCleared = context.getString(R.string.calendars_notification_cleared);
        notificationMsgAddFailed = context.getString(R.string.calendars_notification_adding_failed);
    }

    private Calendar[] getWindow()
    {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        startDate.setTimeInMillis(now.getTimeInMillis() - calendarWindow0);
        startDate.set(Calendar.MONTH, 0);            // round down to start of year
        startDate.set(Calendar.DAY_OF_MONTH, 0);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);

        endDate.setTimeInMillis(now.getTimeInMillis() + calendarWindow1);
        endDate.add(Calendar.YEAR, 1);       // round up to end of year
        endDate.set(Calendar.MONTH, 0);
        endDate.set(Calendar.DAY_OF_MONTH, 0);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);

        return new Calendar[] { startDate, endDate };
    }

    @Override
    protected Boolean doInBackground(SuntimesCalendarTaskItem... items)
    {
        if (Build.VERSION.SDK_INT < 14)
            return false;

        if (items.length > 0) {
            setItems(items);
        }

        if (flag_clear && !isCancelled()) {
            adapter.removeCalendars();
            for (String calendar : ALL_CALENDARS) {
                SuntimesCalendarSettings.clearNotes(contextRef.get(), calendar);
            }
        }

        Calendar[] window = getWindow();
        Log.d(TAG, "Adding... startWindow: " + calendarWindow0 + " (" + window[0].get(Calendar.YEAR) + "), "
                + "endWindow: " + calendarWindow1 + " (" + window[1].get(Calendar.YEAR) + ")");

        boolean retValue = initLocation();
        if (!retValue) {
            return false;
        }

        publishProgress(new SuntimesCalendarTaskProgress(1, 1000, notificationMsgAdding));
        try {
            int c = 0;
            int n = taskItems.size();
            TreeSet<String> calendarSet = new TreeSet<>(taskItems.keySet());
            for (String calendar : calendarSet)
            {
                SuntimesCalendarTaskItem item = taskItems.get(calendar);
                switch (item.getAction())
                {
                    case SuntimesCalendarTaskItem.ACTION_DELETE:
                        publishProgress(null, new SuntimesCalendarTaskProgress(0, 1, notificationMsgClearing));
                        retValue = retValue && adapter.removeCalendar(calendar);
                        SuntimesCalendarSettings.clearNotes(contextRef.get(), calendar);
                        break;

                    case SuntimesCalendarTaskItem.ACTION_UPDATE:
                    default:
                        retValue = retValue && initCalendar(calendar, window, new SuntimesCalendarTaskProgress(c, n, calendarDisplay.get(calendar)));
                        break;
                }
                c++;
            }

        } catch (SecurityException e) {
            lastError = "Unable to access provider! " + e;
            Log.e(TAG, lastError);
            return false;
        }

        return retValue;
    }

    /**
     * initCalendar
     */
    private boolean initCalendar(@NonNull String calendar, @NonNull Calendar[] window, @NonNull SuntimesCalendarTaskProgress progress) throws SecurityException
    {
        if (window.length != 2) {
            Log.e(TAG, "initCalendar: invalid window with length " + window.length);
            return false;

        } else if (window[0] == null || window[1] == null) {
            Log.e(TAG, "initCalendar: invalid window; null!");
            return false;
        }

        boolean retValue = true;
        long calendarID = adapter.queryCalendarID(calendar);
        if (calendarID != -1) {
            retValue = (adapter.removeCalendarEventsBefore(calendarID, window[0].getTimeInMillis()) > 0);
        }

        long bench_start = System.nanoTime();
        if (calendar.equals(SuntimesCalendarAdapterInterface.CALENDAR_SOLSTICE)) {
            retValue = retValue && initSolsticeCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_CIVIL)) {
            retValue = retValue && initCivilTwilightCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_NAUTICAL)) {
            retValue = retValue && initNauticalTwilightCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_ASTRO)) {
            retValue = retValue && initAstroTwilightCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapterInterface.CALENDAR_MOONRISE)) {
            retValue = retValue && initMoonriseCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE)) {
            retValue = retValue && initMoonPhaseCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapterInterface.CALENDAR_MOONAPSIS)) {
            retValue = retValue && initMoonApsisCalendar(progress, window[0], window[1]);

        } else {
            Log.w(TAG, "initCalendar: unrecognized calendar " + calendar);
            retValue = false;
        }
        long bench_end = System.nanoTime();
        Log.i(TAG, "initCalendar (" + calendar + ") in " + ((bench_end - bench_start) / 1000000.0) + " ms");

        return retValue;
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
    private void createSunCalendarEvent(Context context, ArrayList<ContentValues> values, long calendarID, Cursor cursor, int i, String title, String desc0, String desc1, String desc_fallback)
    {
        int j = i + 1;             // [rise-start, rise-end, set-start, set-end]
        int k = (i == 0) ? 2 : 0;  // rising [i, j, k, l] .. setting [k, l, i, j]
        int l = k + 1;
        String eventDesc;
        Calendar eventStart = Calendar.getInstance();
        Calendar eventEnd = Calendar.getInstance();

        if (!cursor.isNull(i) && !cursor.isNull(j))                // avg case [i, j]
        {
            eventStart.setTimeInMillis(cursor.getLong(i));
            eventEnd.setTimeInMillis(cursor.getLong(j));
            //eventDesc = context.getString(R.string.event_at_format, desc0, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
            eventDesc = context.getString(R.string.event_at_format, desc0, config_location_name);
            values.add(adapter.createEventContentValues(calendarID, title, eventDesc, config_location_name, eventStart, eventEnd));

        } else if (!cursor.isNull(i)) {
            eventStart.setTimeInMillis(cursor.getLong(i));
            if (i == 0)
            {
                if (!cursor.isNull(l)) {                          // edge [i, l] of [i, j, k, l]
                    eventEnd.setTimeInMillis(cursor.getLong(l));
                    //eventDesc = context.getString(R.string.event_at_format, desc1, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                    eventDesc = context.getString(R.string.event_at_format, desc1, config_location_name);
                    values.add(adapter.createEventContentValues(calendarID, title, eventDesc, config_location_name, eventStart, eventEnd));
                }

            } else {
                if (cursor.moveToNext())
                {                                // peek forward
                    if (!cursor.isNull(l))
                    {
                        eventEnd.setTimeInMillis(cursor.getLong(l));      // edge [i, +l] of [+k, +l, i, j]
                        //eventDesc = context.getString(R.string.event_at_format, desc1, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        eventDesc = context.getString(R.string.event_at_format, desc1, config_location_name);
                        values.add(adapter.createEventContentValues(calendarID, title, eventDesc, config_location_name, eventStart, eventEnd));

                    } else {                                              // fallback (start-only; end-only events are ignored)
                        //eventDesc = context.getString(R.string.event_at_format, desc_fallback, context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        eventDesc = context.getString(R.string.event_at_format, desc_fallback, config_location_name);
                        values.add(adapter.createEventContentValues(calendarID, title, eventDesc, config_location_name, eventStart));
                    }
                    cursor.moveToPrevious();
                }
            }
        }
    }

    /**
     * initCivilTwilightCalendar
     */
    private boolean initCivilTwilightCalendar(@NonNull SuntimesCalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_CIVIL;
        String calendarTitle = calendarDisplay.get(calendarName);
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SUN_CIVIL_RISE, CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE,
                                                     CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET, CalculatorProviderContract.COLUMN_SUN_CIVIL_SET };   // 0, 1, 2, 3 .. expected order: civil, sunrise, sunset, civil
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, config_location_name);

                    int c = 0;
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, config_location_name);
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, progressTitle);
                    publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    String title = calendarDisplay.get(calendarName);
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        createSunCalendarEvent(context, eventValues, calendarID, cursor, 0, title, s_SUNRISE, s_POLAR_TWILIGHT, s_CIVIL_TWILIGHT);
                        createSunCalendarEvent(context, eventValues, calendarID, cursor, 2, title, s_SUNSET, s_WHITE_NIGHT, s_CIVIL_TWILIGHT);
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast()) {
                            adapter.createCalendarEvents( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || cursor.isLast()) {
                            progress.setProgress(c, totalProgress, progressTitle);
                            publishProgress(progress0, progress);
                        }
                    }
                    cursor.close();
                    return !isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.e(TAG, lastError);
                    return false;
                }

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(TAG, lastError);
                return false;
            }
        } else return false;
    }

    /**
     * initNauticalTwilightCalendar
     */
    private boolean initNauticalTwilightCalendar(@NonNull SuntimesCalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_NAUTICAL;
        String calendarTitle = calendarDisplay.get(calendarName);
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SUN_NAUTICAL_RISE, CalculatorProviderContract.COLUMN_SUN_CIVIL_RISE,
                                                     CalculatorProviderContract.COLUMN_SUN_CIVIL_SET, CalculatorProviderContract.COLUMN_SUN_NAUTICAL_SET };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, config_location_name);

                    int c = 0;
                    int numRows = cursor.getCount();
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, config_location_name);
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, numRows, progressTitle);
                    publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    String title = calendarDisplay.get(calendarName);
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        createSunCalendarEvent(context, eventValues, calendarID, cursor, 0, title, s_DAWN_TWILIGHT, s_CIVIL_NIGHT, s_NAUTICAL_TWILIGHT);
                        createSunCalendarEvent(context, eventValues, calendarID, cursor, 2, title, s_DUSK_TWILIGHT, s_NAUTICAL_TWILIGHT, s_NAUTICAL_TWILIGHT);
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast()) {
                            adapter.createCalendarEvents( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || cursor.isLast()) {
                            progress.setProgress(c, numRows, progressTitle);
                            publishProgress(progress0, progress);
                        }
                    }
                    cursor.close();
                    return !isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.e(TAG, lastError);
                    return false;
                }

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(TAG, lastError);
                return false;
            }
        } else return false;
    }

    /**
     * initAstroTwilightCalendar
     */
    private boolean initAstroTwilightCalendar(@NonNull SuntimesCalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapterInterface.CALENDAR_TWILIGHT_ASTRO;
        String calendarTitle = calendarDisplay.get(calendarName);
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SUN_ASTRO_RISE, CalculatorProviderContract.COLUMN_SUN_NAUTICAL_RISE,
                                                     CalculatorProviderContract.COLUMN_SUN_NAUTICAL_SET, CalculatorProviderContract.COLUMN_SUN_ASTRO_SET };
                Cursor cursor = resolver.query(uri, projection, null, null, null);

                if (cursor != null)
                {
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, config_location_name);

                    int c = 0;
                    int totalProgress = cursor.getCount();
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, config_location_name);
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, progressTitle);
                    publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    String title = calendarDisplay.get(calendarName);
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        createSunCalendarEvent(context, eventValues, calendarID, cursor, 0, title, s_DAWN_TWILIGHT, s_NAUTICAL_NIGHT, s_ASTRO_TWILIGHT);
                        createSunCalendarEvent(context, eventValues, calendarID, cursor, 2, title, s_DUSK_TWILIGHT, s_ASTRO_TWILIGHT, s_ASTRO_TWILIGHT);
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast()) {
                            adapter.createCalendarEvents( eventValues.toArray(new ContentValues[0]) );
                            eventValues.clear();
                        }
                        if (c % 8 == 0 || cursor.isLast()) {
                            progress.setProgress(c, totalProgress, progressTitle);
                            publishProgress(progress0, progress);
                        }
                    }
                    cursor.close();
                    return !isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.e(TAG, lastError);
                    return false;
                }

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(TAG, lastError);
                return false;
            }
        } else return false;
    }


    /**
     * initMoonriseCalendar
     */
    private boolean initMoonriseCalendar(@NonNull SuntimesCalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapterInterface.CALENDAR_MOONRISE;
        String calendarTitle = calendarDisplay.get(calendarName);
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri moonUri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());
                String[] moonProjection = new String[] { CalculatorProviderContract.COLUMN_MOON_RISE, CalculatorProviderContract.COLUMN_MOON_SET };
                Cursor moonCursor = resolver.query(moonUri, moonProjection, null, null, null);
                if (moonCursor != null)
                {
                    new SuntimesCalendarSettings().saveCalendarNote(context, calendarName, SuntimesCalendarSettings.NOTE_LOCATION_NAME, config_location_name);

                    int c = 0;
                    int totalProgress = moonCursor.getCount();
                    String progressTitle = context.getString(R.string.summarylist_format, calendarTitle, config_location_name);
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, progressTitle);
                    publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    String title, desc;
                    moonCursor.moveToFirst();
                    while (!moonCursor.isAfterLast() && !isCancelled())
                    {
                        for (int i=0; i<moonProjection.length; i++)
                        {
                            if (!moonCursor.isNull(i))
                            {
                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(moonCursor.getLong(i));
                                title = moonStrings[i];
                                //desc = context.getString(R.string.event_at_format, moonStrings[i], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                                desc = context.getString(R.string.event_at_format, moonStrings[i], config_location_name);
                                eventValues.add(adapter.createEventContentValues(calendarID, title, desc, config_location_name, eventTime));
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
                            publishProgress(progress0, progress);
                        }
                    }
                    moonCursor.close();
                    return !isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + moonUri;
                    Log.e(TAG, lastError);
                    return false;
                }

            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(TAG, lastError);
                return false;
            }
        } else return false;
    }

    /**
     * initSolsticeCalendar
     */
    private boolean initSolsticeCalendar(@NonNull SuntimesCalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate ) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapterInterface.CALENDAR_SOLSTICE;
        String calendarTitle = calendarDisplay.get(calendarName);
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SEASONS + "/" + startDate.get(Calendar.YEAR) + "-" + endDate.get(Calendar.YEAR));
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_SEASON_VERNAL, CalculatorProviderContract.COLUMN_SEASON_SUMMER, CalculatorProviderContract.COLUMN_SEASON_AUTUMN, CalculatorProviderContract.COLUMN_SEASON_WINTER };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();

                    int c = 0;
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, calendarTitle);
                    publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        for (int i=0; i<projection.length; i++)
                        {
                            if (!cursor.isNull(i))
                            {
                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(cursor.getLong(i));
                                eventValues.add(adapter.createEventContentValues(calendarID, solsticeStrings[i], solsticeStrings[i], null, eventTime));
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
                        publishProgress(progress0, progress);
                    }
                    cursor.close();
                    return !isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.e(TAG, lastError);
                    return false;
                }
            } else {
                lastError = "Unable to getContentResolver! ";
                Log.e(TAG, lastError);
                return false;
            }
        } else return false;
    }

    /**
     * initMoonApsisCalendar
     */
    private boolean initMoonApsisCalendar(@NonNull SuntimesCalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate ) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapterInterface.CALENDAR_MOONAPSIS;
        String calendarTitle = calendarDisplay.get(calendarName);

        if (config_provider_version < 2)    // sanity check.. moonApsis needs provider v2:0.3.0 (Suntimes v0.12.0+))
        {
            Context context = contextRef.get();
            lastError = context != null ? context.getString(R.string.feature_not_supported_by_provider, calendarTitle, "Suntimes v0.12.0")
                                        : calendarTitle + " is not supported by the current version; requires Suntimes v0.12.0 or greater.";
            Log.e("initMoonApsisCalendar", lastError);
            return (taskItems.size() > 1);  // let other calendars finish
        }

        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        String[] projection = new String[] {
                CalculatorProviderContract.COLUMN_MOONPOS_APOGEE,
                CalculatorProviderContract.COLUMN_MOONPOS_PERIGEE
        };

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                int c = 0;
                int totalProgress = (int)Math.ceil(1.25 * (((endDate.getTimeInMillis() - startDate.getTimeInMillis()) / 1000d / 60d / 60d / 24d) / 27.554551d));
                SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, calendarTitle);
                publishProgress(progress0, progress);

                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(startDate.getTimeInMillis());

                while (date.before(endDate) && !isCancelled())
                {
                    Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPOS  + "/" + (date.getTimeInMillis()));
                    Cursor cursor = resolver.query(uri, projection, null, null, null);
                    if (cursor == null)
                    {
                        lastError = "Failed to resolve URI! " + uri;
                        Log.w(TAG, lastError);
                        return false;

                    } else {
                        progress = new SuntimesCalendarTaskProgress(c, totalProgress, calendarTitle);
                        publishProgress(progress0, progress);

                        ArrayList<ContentValues> eventValues = new ArrayList<>();
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast() && !isCancelled())
                        {
                            if (cursor.getColumnCount() <= 2 || cursor.getLong(0) <= 0)
                            {   // sanity check.. moonApsis needs provider v2:0.3.0 (Suntimes v0.12.0+))
                                cursor.close();
                                progress.setProgress(totalProgress, totalProgress, calendarTitle);
                                publishProgress(progress0, progress);
                                lastError = context.getString(R.string.feature_not_supported_by_provider, calendarTitle, "Suntimes v0.12.0");
                                Log.e("initMoonApsisCalendar", lastError);
                                return false;
                            }

                            for (int i=0; i<2; i++)
                            {
                                Calendar eventTime = Calendar.getInstance();
                                eventTime.setTimeInMillis(cursor.getLong(i));
                                double distance = lookupMoonDistance(context, resolver, eventTime.getTimeInMillis());
                                String desc = ((distance != -1) ? context.getString(R.string.event_distance_format, apsisStrings[i], formatDistanceString(distance)) : apsisStrings[i]);
                                eventValues.add(adapter.createEventContentValues(calendarID, apsisStrings[i], desc, null, eventTime));
                            }
                            date.setTimeInMillis(cursor.getLong(0) + (60 * 1000));  // advance to next cycle
                            cursor.moveToNext();
                            c++;

                            progress.setProgress(c, totalProgress, calendarTitle);
                            publishProgress(progress0, progress);
                        }
                        cursor.close();

                        int chunk = 128;
                        int n = eventValues.size();
                        for (int i = 0; i < n; i += chunk) {
                            int j = i + chunk;
                            adapter.createCalendarEvents(eventValues.subList(i, (j > n ? n : j)).toArray(new ContentValues[0]));
                        }
                    }
                }
                return !isCancelled();

            } else {
                lastError = "Unable to getContentResolver!";
                Log.e("initMoonApsisCalendar", lastError);
                return false;
            }
        } else return false;
    }

    private double lookupMoonDistance( @NonNull Context context, @NonNull ContentResolver resolver, long dateMillis )
    {
        double retValue = -1;
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPOS  + "/" + dateMillis);
        Cursor cursor = resolver.query(uri, new String[] { CalculatorProviderContract.COLUMN_MOONPOS_DISTANCE }, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            retValue = cursor.getDouble(0);
            cursor.close();
        }
        return retValue;
    }

    /**
     * initMoonPhaseCalendar
     */
    private boolean initMoonPhaseCalendar(@NonNull SuntimesCalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate ) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapterInterface.CALENDAR_MOONPHASE;
        String calendarTitle = calendarDisplay.get(calendarName);
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        String[] projection = new String[] {    // indices 0-3 should contain ordered phases!
                CalculatorProviderContract.COLUMN_MOON_NEW,
                CalculatorProviderContract.COLUMN_MOON_FIRST,
                CalculatorProviderContract.COLUMN_MOON_FULL,
                CalculatorProviderContract.COLUMN_MOON_THIRD,
                CalculatorProviderContract.COLUMN_MOON_NEW_DISTANCE,  // use indices 4+ for other data
                CalculatorProviderContract.COLUMN_MOON_FULL_DISTANCE
        };

        long calendarID = adapter.queryCalendarID(calendarName);
        if (calendarID != -1)
        {
            Context context = contextRef.get();
            ContentResolver resolver = (context == null ? null : context.getContentResolver());
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPHASE + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    int c = 0;
                    int totalProgress = cursor.getCount();
                    SuntimesCalendarTaskProgress progress = new SuntimesCalendarTaskProgress(c, totalProgress, calendarTitle);
                    publishProgress(progress0, progress);

                    ArrayList<ContentValues> eventValues = new ArrayList<>();
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        for (int i=0; i<4; i++)
                        {
                            double distance = -1;
                            String[] titleStrings;
                            if (i == 0 || i == 2)  // new moon || full moon
                            {
                                distance = cursor.getDouble(i == 0 ? 4 : 5);

                                if (distance < THRESHHOLD_SUPERMOON) {
                                    titleStrings = phaseStrings1;
                                } else if (distance > THRESHHOLD_MICROMOON) {
                                    titleStrings = phaseStrings2;
                                } else titleStrings = phaseStrings;

                            } else titleStrings = phaseStrings;

                            String desc = (distance > 0)
                                    ? context.getString(R.string.event_distance_format, titleStrings[i], formatDistanceString(distance))
                                    : titleStrings[i];
                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(cursor.getLong(i));
                            eventValues.add(adapter.createEventContentValues(calendarID, titleStrings[i], desc, null, eventTime));
                        }
                        cursor.moveToNext();
                        c++;

                        if (c % 128 == 0 || cursor.isLast())
                        {
                            adapter.createCalendarEvents(eventValues.toArray(new ContentValues[0]));
                            eventValues.clear();
                        }
                        progress.setProgress(c, totalProgress, calendarTitle);
                        publishProgress(progress0, progress);
                    }
                    cursor.close();
                    return !isCancelled();

                } else {
                    lastError = "Failed to resolve URI! " + uri;
                    Log.w(TAG, lastError);
                    return false;
                }
            } else {
                lastError = "Unable to getContentResolver!";
                Log.e("initMoonPhaseCalendar", lastError);
                return false;
            }
        } else return false;
    }

    private NumberFormat distanceFormatter = null;
    private String formatDistanceString(double distance)
    {
        if (distanceFormatter == null)
        {
            distanceFormatter = new DecimalFormat();
            distanceFormatter.setMinimumFractionDigits(0);
            distanceFormatter.setMaximumFractionDigits(2);
        }
        return distanceFormatter.format(distance);
    }

}
