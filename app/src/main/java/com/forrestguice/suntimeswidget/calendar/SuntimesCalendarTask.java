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

package com.forrestguice.suntimeswidget.calendar;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeSet;

@SuppressWarnings("Convert2Diamond")
public class SuntimesCalendarTask extends AsyncTask<SuntimesCalendarTask.SuntimesCalendarTaskItem, SuntimesCalendarTask.CalendarTaskProgress, Boolean>
{
    public static final String TAG = "SuntimesCalendarTask";

    private SuntimesCalendarAdapter adapter;
    private WeakReference<Context> contextRef;

    private HashMap<String, SuntimesCalendarTaskItem> calendars = new HashMap<>();
    private HashMap<String, String> calendarDisplay = new HashMap<>();
    private HashMap<String, Integer> calendarColors = new HashMap<>();

    private String[] sunStrings = new String[5];       // {sunrise, sunset, civil twilight, nautical twilight, astronomical twilight}
    private String[] moonStrings = new String[2];      // {moonrise, moonset}
    private String[] phaseStrings = new String[4];     // {major phases}
    private String[] solsticeStrings = new String[4];  // {spring, summer, fall, winter}
    //private int[] solsticeColors = new int[4];

    private long lastSync = -1;
    private long calendarWindow0 = -1, calendarWindow1 = -1;

    private String notificationMsgAdding, notificationMsgAdded;
    private String notificationMsgClearing, notificationMsgCleared;
    private String notificationMsgAddFailed;
    private String lastError = null;

    public SuntimesCalendarTask(Context context)
    {
        contextRef = new WeakReference<Context>(context);
        adapter = new SuntimesCalendarAdapter(context.getContentResolver());
        calendarWindow0 = SuntimesCalendarSettings.loadPrefCalendarWindow0(context);
        calendarWindow1 = SuntimesCalendarSettings.loadPrefCalendarWindow1(context);

        // solstice calendar resources
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_SOLSTICE, context.getString(R.string.calendar_solstice_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_SOLSTICE, ContextCompat.getColor(context, R.color.colorSolsticeCalendar));

        solsticeStrings[0] = context.getString(R.string.timeMode_equinox_vernal);
        solsticeStrings[1] = context.getString(R.string.timeMode_solstice_summer);
        solsticeStrings[2] = context.getString(R.string.timeMode_equinox_autumnal);
        solsticeStrings[3] = context.getString(R.string.timeMode_solstice_winter);

        phaseStrings[0] = context.getString(R.string.timeMode_moon_new);
        phaseStrings[1] = context.getString(R.string.timeMode_moon_firstquarter);
        phaseStrings[2] = context.getString(R.string.timeMode_moon_full);
        phaseStrings[3] = context.getString(R.string.timeMode_moon_thirdquarter);

        //solsticeColors[0] = ContextCompat.getColor(context, R.color.springColor_light);
        //solsticeColors[1] = ContextCompat.getColor(context, R.color.summerColor_light);
        //solsticeColors[2] = ContextCompat.getColor(context, R.color.fallColor_light);
        //solsticeColors[3] = ContextCompat.getColor(context, R.color.winterColor_light);

        // sunrise, sunset calendar resources
        sunStrings[0] = context.getString(R.string.sunrise);
        sunStrings[1] = context.getString(R.string.sunset);
        sunStrings[2] = context.getString(R.string.timeMode_civil);
        sunStrings[3] = context.getString(R.string.timeMode_nautical);
        sunStrings[4] = context.getString(R.string.timeMode_astronomical);

        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_CIVIL, context.getString(R.string.calendar_civil_twilight_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_CIVIL, ContextCompat.getColor(context, R.color.colorCivilTwilightCalendar));

        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_NAUTICAL, context.getString(R.string.calendar_nautical_twilight_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_NAUTICAL, ContextCompat.getColor(context, R.color.colorNauticalTwilightCalendar));

        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_ASTRO, context.getString(R.string.calendar_astronomical_twilight_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_ASTRO, ContextCompat.getColor(context, R.color.colorAstroTwilightCalendar));

        // moonrise, moonset calendar resources
        moonStrings[0] = context.getString(R.string.moonrise);
        moonStrings[1] = context.getString(R.string.moonset);

        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_MOONRISE, context.getString(R.string.calendar_moonrise_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_MOONRISE, ContextCompat.getColor(context, R.color.colorMoonriseCalendar));

        // moon phase calendar resources
        calendarDisplay.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, context.getString(R.string.calendar_moonPhase_displayName));
        calendarColors.put(SuntimesCalendarAdapter.CALENDAR_MOONPHASE, ContextCompat.getColor(context, R.color.colorMoonCalendar));

        notificationMsgAdding = context.getString(R.string.calendars_notification_adding);
        notificationMsgAdded = context.getString(R.string.calendars_notification_added);
        notificationMsgClearing = context.getString(R.string.calendars_notification_clearing);
        notificationMsgCleared = context.getString(R.string.calendars_notification_cleared);
        notificationMsgAddFailed = context.getString(R.string.calendars_notification_adding_failed);
    }

    private boolean flag_notifications = true;
    private boolean flag_clear = false;
    public void setFlagClearCalendars( boolean flag )
    {
        flag_clear = flag;
    }
    public boolean getFlagClearCalendars()
    {
        return flag_clear;
    }

    public void setItems(SuntimesCalendarTaskItem... items)
    {
        calendars.clear();
        for (SuntimesCalendarTaskItem item : items) {
            calendars.put(item.getCalendar(), item);
        }
    }
    public SuntimesCalendarTaskItem[] getItems() {
        return calendars.values().toArray(new SuntimesCalendarTaskItem[0]);
    }

    public void addItems(SuntimesCalendarTaskItem... items)
    {
        for (SuntimesCalendarTaskItem item : items) {
            calendars.put(item.getCalendar(), item);         // TODO: preserve existing
        }
    }

    @Override
    protected void onCancelled ()
    {
        super.onCancelled();
        Log.w(TAG, "task canceled!" );
    }

    @Override
    protected void onPreExecute()
    {
        Context context = contextRef.get();
        if (context != null) {
            lastSync = SuntimesCalendarSyncAdapter.readLastSyncTime(context);
        }
        lastError = null;

        String message = "";
        if (flag_clear) {
            message = notificationMsgClearing;
            triggerOnStarted(message);

        } else {
            SuntimesCalendarTaskItem[] items = calendars.values().toArray(new SuntimesCalendarTask.SuntimesCalendarTaskItem[0]);
            if (items.length > 0) {
                int action = items[0].getAction();
                message = (action == SuntimesCalendarTaskItem.ACTION_DELETE) ? notificationMsgClearing : notificationMsgAdding;

                if (action != SuntimesCalendarTaskItem.ACTION_DELETE) {
                    triggerOnStarted(message);
                } else triggerOnStarted("");
            } else triggerOnStarted("");
        }
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
        }

        Calendar[] window = getWindow();
        Log.d(TAG, "Adding... startWindow: " + calendarWindow0 + " (" + window[0].get(Calendar.YEAR) + "), "
                + "endWindow: " + calendarWindow1 + " (" + window[1].get(Calendar.YEAR) + ")");

        boolean retValue = initLocation();
        if (!retValue) {
            return false;
        }

        publishProgress(new CalendarTaskProgress(1, 1000, notificationMsgAdding));
        try {
            int c = 0;
            int n = calendars.size();
            TreeSet<String> calendarSet = new TreeSet<>(calendars.keySet());
            for (String calendar : calendarSet)
            {
                SuntimesCalendarTaskItem item = calendars.get(calendar);
                switch (item.getAction())
                {
                    case SuntimesCalendarTaskItem.ACTION_DELETE:
                        publishProgress(null, new CalendarTaskProgress(0, 1, notificationMsgClearing));
                        retValue = retValue && adapter.removeCalendar(calendar);
                        break;

                    case SuntimesCalendarTaskItem.ACTION_UPDATE:
                    default:
                        retValue = retValue && initCalendar(calendar, window, new CalendarTaskProgress(c, n, calendarDisplay.get(calendar)));
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

    @Override
    protected void onProgressUpdate(CalendarTaskProgress... progress)
    {
        Context context = contextRef.get();
        if (listener != null && context != null) {
            listener.onProgress(context, progress);
        }
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        Context context = contextRef.get();
        if (result)
        {
            if (context != null) {
                SuntimesCalendarSyncAdapter.writeLastSyncTime(context, Calendar.getInstance());
            }

            String message = (flag_clear ? notificationMsgCleared : notificationMsgAdded);
            SuntimesCalendarTaskItem[] items = calendars.values().toArray(new SuntimesCalendarTask.SuntimesCalendarTaskItem[0]);
            if (items.length > 0) {
                if (items[0].getAction() == SuntimesCalendarTaskItem.ACTION_DELETE) {
                    message = notificationMsgCleared;
                }
            }

            if (listener != null && context != null) {
                listener.onSuccess(context, this, message);
            }

        } else {
            Log.w(TAG, "Failed to complete task!");
            if (listener != null && context != null) {
                listener.onFailed(context, lastError);
            }
        }
    }

    private String config_location_name = "";
    private String config_location_latitude = "";
    private String config_location_longitude = "";
    private String config_location_altitude = "";

    /**
     * initLocation
     */
    private boolean initLocation()
    {
        Context context = contextRef.get();
        ContentResolver resolver = (context == null ? null : context.getContentResolver());
        if (resolver != null) {
            Uri configUri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG);
            String[] configProjection = new String[]{CalculatorProviderContract.COLUMN_CONFIG_LOCATION, CalculatorProviderContract.COLUMN_CONFIG_LATITUDE, CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE, CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE};
            Cursor configCursor = resolver.query(configUri, configProjection, null, null, null);

            if (configCursor != null) {
                configCursor.moveToFirst();
                for (int i = 0; i < configProjection.length; i++) {
                    config_location_name = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_LOCATION));
                    config_location_latitude = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_LATITUDE));
                    config_location_longitude = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE));
                    config_location_altitude = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE));
                }
                configCursor.close();
                return true;

            } else {
                lastError = "Failed to resolve URI! " + configUri;
                Log.e(TAG, lastError);
                return false;
            }
        } else {
            lastError = "Unable to getContentResolver! ";
            Log.e(TAG, lastError);
            return false;
        }
    }

    /**
     * initCalendar
     */
    private boolean initCalendar(@NonNull String calendar, @NonNull Calendar[] window, @NonNull CalendarTaskProgress progress) throws SecurityException
    {
        if (window.length != 2) {
            Log.e(TAG, "initCalendar: invalid window with length " + window.length);
            return false;

        } else if (window[0] == null || window[1] == null) {
            Log.e(TAG, "initCalendar: invalid window; null!");
            return false;
        }

        if (calendar.equals(SuntimesCalendarAdapter.CALENDAR_SOLSTICE)) {
            return initSolsticeCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_CIVIL)) {
            return initCivilTwilightCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_NAUTICAL)) {
            return initNauticalTwilightCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapter.CALENDAR_TWILIGHT_ASTRO)) {
            return initAstroTwilightCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapter.CALENDAR_MOONRISE)) {
            return initMoonriseCalendar(progress, window[0], window[1]);

        } else if (calendar.equals(SuntimesCalendarAdapter.CALENDAR_MOONPHASE)) {
            return initMoonPhaseCalendar(progress, window[0], window[1]);

        } else {
            Log.w(TAG, "initCalendar: unrecognized calendar " + calendar);
            return false;
        }
    }

    /**
     * initCivilTwilightCalendar
     */
    private boolean initCivilTwilightCalendar(@NonNull CalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_CIVIL;
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
                    int c = 0;
                    int numRows = cursor.getCount();
                    CalendarTaskProgress progress = new CalendarTaskProgress(c, numRows, calendarTitle);
                    publishProgress(progress0, progress);

                    String morningTitle, morningDesc;
                    String eveningTitle, eveningDesc;
                    Calendar[] twilightMorning = new Calendar[2];
                    Calendar[] twilightEvening = new Calendar[2];
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        twilightMorning[0] = Calendar.getInstance();
                        twilightMorning[1] = Calendar.getInstance();
                        twilightMorning[0].setTimeInMillis(cursor.getLong(0));
                        twilightMorning[1].setTimeInMillis(cursor.getLong(1));
                        morningTitle = calendarDisplay.get(calendarName);
                        morningDesc = context.getString(R.string.event_at_format, sunStrings[0], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        adapter.createCalendarEvent(calendarID, morningTitle, morningDesc, twilightMorning);

                        twilightEvening[0] = Calendar.getInstance();
                        twilightEvening[1] = Calendar.getInstance();
                        twilightEvening[0].setTimeInMillis(cursor.getLong(2));
                        twilightEvening[1].setTimeInMillis(cursor.getLong(3));
                        eveningTitle = calendarDisplay.get(calendarName);
                        eveningDesc = context.getString(R.string.event_at_format, sunStrings[1], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        adapter.createCalendarEvent(calendarID, eveningTitle, eveningDesc, twilightEvening);

                        cursor.moveToNext();
                        if (c % 8 == 0) {
                            progress.setProgress(c, numRows, calendarTitle);
                            publishProgress(progress0, progress);
                        }
                        c++;
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
    private boolean initNauticalTwilightCalendar(@NonNull CalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_NAUTICAL;
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
                    int c = 0;
                    int numRows = cursor.getCount();
                    CalendarTaskProgress progress = new CalendarTaskProgress(c, numRows, calendarTitle);
                    publishProgress(progress0, progress);

                    String morningTitle, morningDesc;
                    String eveningTitle, eveningDesc;
                    Calendar[] twilightMorning = new Calendar[2];
                    Calendar[] twilightEvening = new Calendar[2];
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        twilightMorning[0] = Calendar.getInstance();
                        twilightMorning[1] = Calendar.getInstance();
                        twilightMorning[0].setTimeInMillis(cursor.getLong(0));
                        twilightMorning[1].setTimeInMillis(cursor.getLong(1));
                        morningTitle = calendarDisplay.get(calendarName);
                        morningDesc = context.getString(R.string.event_at_format, sunStrings[3], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        adapter.createCalendarEvent(calendarID, morningTitle, morningDesc, twilightMorning);

                        twilightEvening[0] = Calendar.getInstance();
                        twilightEvening[1] = Calendar.getInstance();
                        twilightEvening[0].setTimeInMillis(cursor.getLong(2));
                        twilightEvening[1].setTimeInMillis(cursor.getLong(3));
                        eveningTitle = calendarDisplay.get(calendarName);
                        eveningDesc = context.getString(R.string.event_at_format, sunStrings[3], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        adapter.createCalendarEvent(calendarID, eveningTitle, eveningDesc, twilightEvening);

                        cursor.moveToNext();
                        if (c % 8 == 0) {
                            progress.setProgress(c, numRows, calendarTitle);
                            publishProgress(progress0, progress);
                        }
                        c++;
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
    private boolean initAstroTwilightCalendar(@NonNull CalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapter.CALENDAR_TWILIGHT_ASTRO;
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
                    int c = 0;
                    int numRows = cursor.getCount();
                    CalendarTaskProgress progress = new CalendarTaskProgress(c, numRows, notificationMsgAdding);
                    publishProgress(progress0, progress);

                    String morningTitle, morningDesc;
                    String eveningTitle, eveningDesc;
                    Calendar[] twilightMorning = new Calendar[2];
                    Calendar[] twilightEvening = new Calendar[2];
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        twilightMorning[0] = Calendar.getInstance();
                        twilightMorning[1] = Calendar.getInstance();
                        twilightMorning[0].setTimeInMillis(cursor.getLong(0));
                        twilightMorning[1].setTimeInMillis(cursor.getLong(1));
                        morningTitle = calendarDisplay.get(calendarName);
                        morningDesc = context.getString(R.string.event_at_format, sunStrings[4], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        adapter.createCalendarEvent(calendarID, morningTitle, morningDesc, twilightMorning);

                        twilightEvening[0] = Calendar.getInstance();
                        twilightEvening[1] = Calendar.getInstance();
                        twilightEvening[0].setTimeInMillis(cursor.getLong(2));
                        twilightEvening[1].setTimeInMillis(cursor.getLong(3));
                        eveningTitle = calendarDisplay.get(calendarName);
                        eveningDesc = context.getString(R.string.event_at_format, sunStrings[4], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                        adapter.createCalendarEvent(calendarID, eveningTitle, eveningDesc, twilightEvening);

                        cursor.moveToNext();
                        if (c % 8 == 0) {
                            progress.setProgress(c, numRows, calendarTitle);
                            publishProgress(progress0, progress);
                        }
                        c++;
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
    private boolean initMoonriseCalendar(@NonNull CalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapter.CALENDAR_MOONRISE;
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
                    int c = 0;
                    int numRows = moonCursor.getCount();
                    CalendarTaskProgress progress = new CalendarTaskProgress(c, numRows, calendarTitle);
                    publishProgress(progress0, progress);

                    String title, desc;
                    moonCursor.moveToFirst();
                    while (!moonCursor.isAfterLast() && !isCancelled())
                    {
                        for (int i=0; i<moonProjection.length; i++)
                        {
                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(moonCursor.getLong(i));
                            title = moonStrings[i];
                            desc = context.getString(R.string.event_at_format, moonStrings[i], context.getString(R.string.location_format_short, config_location_name, config_location_latitude, config_location_longitude));
                            adapter.createCalendarEvent(calendarID, title, desc, eventTime);
                            //Log.d("DEBUG", "create event: " + moonStrings[i] + " at " + eventTime.toString());
                        }
                        moonCursor.moveToNext();
                        if (c % 8 == 0) {
                            progress.setProgress(c, numRows, calendarTitle);
                            publishProgress(progress0, progress);
                        }
                        c++;
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
    private boolean initSolsticeCalendar(@NonNull CalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate ) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapter.CALENDAR_SOLSTICE;
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
                    int numRows = cursor.getCount();
                    CalendarTaskProgress progress = new CalendarTaskProgress(c, numRows, calendarTitle);
                    publishProgress(progress0, progress);

                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        for (int i=0; i<projection.length; i++)
                        {
                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(cursor.getLong(i));
                            adapter.createCalendarEvent(calendarID, solsticeStrings[i], solsticeStrings[i], eventTime);
                        }
                        cursor.moveToNext();
                        progress.setProgress(c, numRows, calendarTitle);
                        publishProgress(progress0, progress);
                        c++;
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
     * initMoonPhaseCalendar
     */
    private boolean initMoonPhaseCalendar(@NonNull CalendarTaskProgress progress0, @NonNull Calendar startDate, @NonNull Calendar endDate ) throws SecurityException
    {
        if (isCancelled()) {
            return false;
        }

        String calendarName = SuntimesCalendarAdapter.CALENDAR_MOONPHASE;
        String calendarTitle = calendarDisplay.get(calendarName);
        if (!adapter.hasCalendar(calendarName)) {
            adapter.createCalendar(calendarName, calendarTitle, calendarColors.get(calendarName));
        } else return false;

        String[] projection = new String[] {
                CalculatorProviderContract.COLUMN_MOON_NEW,
                CalculatorProviderContract.COLUMN_MOON_FIRST,
                CalculatorProviderContract.COLUMN_MOON_FULL,
                CalculatorProviderContract.COLUMN_MOON_THIRD };

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
                    int numRows = cursor.getCount();
                    CalendarTaskProgress progress = new CalendarTaskProgress(c, numRows, calendarTitle);
                    publishProgress(progress0, progress);

                    cursor.moveToFirst();
                    while (!cursor.isAfterLast() && !isCancelled())
                    {
                        for (int i=0; i<projection.length; i++)
                        {
                            Calendar eventTime = Calendar.getInstance();
                            eventTime.setTimeInMillis(cursor.getLong(i));
                            adapter.createCalendarEvent(calendarID, phaseStrings[i], phaseStrings[i], eventTime);
                        }
                        cursor.moveToNext();
                        progress.setProgress(c, numRows, calendarTitle);
                        publishProgress(progress0, progress);
                        c++;
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

    /**
     * SuntimesCalendarTaskItem
     */
    public static class SuntimesCalendarTaskItem implements Parcelable
    {
        public static final int ACTION_UPDATE = 0;
        public static final int ACTION_DELETE = 2;

        private String calendar;
        private int action;

        public SuntimesCalendarTaskItem( String calendar, int action )
        {
            this.calendar = calendar;
            this.action = action;
        }

        private SuntimesCalendarTaskItem(Parcel in)
        {
            this.calendar = in.readString();
            this.action = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            dest.writeString(calendar);
            dest.writeInt(action);
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        public String getCalendar()
        {
            return calendar;
        }

        public int getAction()
        {
            return action;
        }

        public static final Parcelable.Creator<SuntimesCalendarTaskItem> CREATOR = new Parcelable.Creator<SuntimesCalendarTaskItem>()
        {
            public SuntimesCalendarTaskItem createFromParcel(Parcel in)
            {
                return new SuntimesCalendarTaskItem(in);
            }

            public SuntimesCalendarTaskItem[] newArray(int size)
            {
                return new SuntimesCalendarTaskItem[size];
            }
        };
    }

    /**
     * SuntimesCalendarTaskListener
     */
    public static abstract class SuntimesCalendarTaskListener implements Parcelable
    {
        public void onStarted(Context context, SuntimesCalendarTask task, String message) {}
        public void onProgress(Context context, CalendarTaskProgress... progress) {}
        public void onSuccess(Context context, SuntimesCalendarTask task, String message) {}
        public void onFailed(Context context, String errorMsg) {}

        public SuntimesCalendarTaskListener() {}

        protected SuntimesCalendarTaskListener(Parcel in) {}

        @Override
        public void writeToParcel(Parcel dest, int flags) {}

        @Override
        public int describeContents() {
            return 0;
        }
    }

    private SuntimesCalendarTaskListener listener;
    public void setTaskListener( SuntimesCalendarTaskListener listener )
    {
        this.listener = listener;
    }
    protected void triggerOnStarted(String message)
    {
        Context context = contextRef.get();
        if (listener != null && context != null) {
            listener.onStarted(context, this, message);
        }
    }

    public static class CalendarTaskProgress
    {
        public CalendarTaskProgress(int i, int n, String message)
        {
            setProgress(i, n, message);
        }
        public void setProgress(int i, int n, String message)
        {
            this.i = i;
            this.n = n;
            this.message = message;
        }

        private int i;
        public int itemNum() {
            return i;
        }

        private int n;
        public int getCount() {
            return n;
        }

        private String message;
        public String getMessage() {
            return message;
        }

        public boolean isIndeterminate()
        {
            return (i == 0 || n == 0);
        }

        public String toString() {
            return message + ": " + i + "/" + n + " (" + (isIndeterminate() ? "true" : "false") + ")";
        }
    }

}
