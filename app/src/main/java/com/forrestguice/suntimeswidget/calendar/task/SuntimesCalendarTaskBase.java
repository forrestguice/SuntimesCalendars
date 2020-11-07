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

package com.forrestguice.suntimeswidget.calendar.task;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

@SuppressWarnings("Convert2Diamond")
public abstract class SuntimesCalendarTaskBase extends AsyncTask<SuntimesCalendarTaskItem, SuntimesCalendarTaskProgress, Boolean>
{
    protected SuntimesCalendarAdapter adapter;
    protected WeakReference<Context> contextRef;
    protected long calendarWindow0 = -1, calendarWindow1 = -1;
    protected HashMap<String, SuntimesCalendarTaskItem> taskItems = new HashMap<>();

    protected boolean flag_notifications = true;
    protected boolean flag_clear = false;

    protected int config_provider_version = 0;
    protected String config_location_name = "";
    protected String config_location_latitude = "";
    protected String config_location_longitude = "";
    protected String config_location_altitude = "";

    protected long lastSync = -1;
    protected String lastError = null;

    public SuntimesCalendarTaskBase(Context context)
    {
        contextRef = new WeakReference<Context>(context);
        adapter = new SuntimesCalendarAdapter(context.getContentResolver(), SuntimesCalendarDescriptor.getCalendars(context));
        calendarWindow0 = SuntimesCalendarSettings.loadPrefCalendarWindow0(context);
        calendarWindow1 = SuntimesCalendarSettings.loadPrefCalendarWindow1(context);
        initDisplayStrings(context);
    }

    protected void initDisplayStrings(Context context)
    {
        notificationMsgAdding = context.getString(R.string.calendars_notification_adding);
        notificationMsgAdded = context.getString(R.string.calendars_notification_added);
        notificationMsgClearing = context.getString(R.string.calendars_notification_clearing);
        notificationMsgCleared = context.getString(R.string.calendars_notification_cleared);
        notificationMsgAddFailed = context.getString(R.string.calendars_notification_adding_failed);
    }
    protected String notificationMsgAdding, notificationMsgAdded;
    protected String notificationMsgClearing, notificationMsgCleared;
    protected String notificationMsgAddFailed;

    public long lastSync() {
        return lastSync;
    }

    public String lastError() {
        return lastError;
    }

    public void setItems(SuntimesCalendarTaskItem... items)
    {
        taskItems.clear();
        for (SuntimesCalendarTaskItem item : items) {
            taskItems.put(item.getCalendar(), item);
        }
    }
    public SuntimesCalendarTaskItem[] getItems() {
        return taskItems.values().toArray(new SuntimesCalendarTaskItem[0]);
    }

    public void addItems(SuntimesCalendarTaskItem... items)
    {
        for (SuntimesCalendarTaskItem item : items) {
            taskItems.put(item.getCalendar(), item);         // TODO: preserve existing
        }
    }

    public void setFlagClearCalendars( boolean flag ) {
        flag_clear = flag;
    }
    public boolean getFlagClearCalendars() {
        return flag_clear;
    }

    protected boolean initLocation()
    {
        Context context = contextRef.get();
        ContentResolver resolver = (context == null ? null : context.getContentResolver());
        if (resolver != null)
        {
            Uri configUri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG);
            String[] configProjection = new String[]{CalculatorProviderContract.COLUMN_CONFIG_LOCATION, CalculatorProviderContract.COLUMN_CONFIG_LATITUDE, CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE, CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE, CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE};

            try {
                Cursor configCursor = resolver.query(configUri, configProjection, null, null, null);
                if (configCursor != null)
                {
                    configCursor.moveToFirst();
                    for (int i = 0; i < configProjection.length; i++) {
                        config_location_name = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_LOCATION));
                        config_location_latitude = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_LATITUDE));
                        config_location_longitude = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE));
                        config_location_altitude = configCursor.getString(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE));
                        config_provider_version = configCursor.getInt(configCursor.getColumnIndex(CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE));
                    }
                    configCursor.close();
                    return true;

                } else {
                    lastError = "Failed to resolve URI! " + configUri;
                    Log.e(getClass().getSimpleName(), lastError);
                    return false;
                }
            } catch (SecurityException e) {
                lastError = "Permission Denied! " + configUri;
                Log.e(getClass().getSimpleName(), lastError);
                return false;
            }
        } else {
            lastError = "Unable to getContentResolver! ";
            Log.e(getClass().getSimpleName(), lastError);
            return false;
        }
    }
    public String[] getLocation() {
        return new String[] { config_location_name, config_location_latitude, config_location_longitude, config_location_altitude };
    }

    public int getProviderVersion() {
        return config_provider_version;
    }

    @Override
    protected void onPreExecute()
    {
        Context context = contextRef.get();
        if (context != null) {
            lastSync = SuntimesCalendarSettings.readLastSyncTime(context);
        }
        lastError = null;

        String message = "";
        if (flag_clear) {
            message = notificationMsgClearing;
            triggerOnStarted(message);

        } else {
            SuntimesCalendarTaskItem[] items = taskItems.values().toArray(new SuntimesCalendarTaskItem[0]);
            if (items.length > 0) {
                int action = items[0].getAction();
                message = (action == SuntimesCalendarTaskItem.ACTION_DELETE) ? notificationMsgClearing : notificationMsgAdding;

                if (action != SuntimesCalendarTaskItem.ACTION_DELETE) {
                    triggerOnStarted(message);
                } else triggerOnStarted("");
            } else triggerOnStarted("");
        }
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        Context context = contextRef.get();
        if (result)
        {
            if (context != null) {
                SuntimesCalendarSettings.writeLastSyncTime(context, Calendar.getInstance().getTimeInMillis());
            }

            String message = (flag_clear ? notificationMsgCleared : notificationMsgAdded);
            SuntimesCalendarTaskItem[] items = taskItems.values().toArray(new SuntimesCalendarTaskItem[0]);
            if (items.length > 0) {
                if (items[0].getAction() == SuntimesCalendarTaskItem.ACTION_DELETE) {
                    message = notificationMsgCleared;
                }
            }

            if (listener != null && context != null) {
                listener.onSuccess(context, this, message);
            }

        } else {
            Log.w(getClass().getSimpleName(), "Failed to complete task!");
            if (listener != null && context != null) {
                listener.onFailed(context, lastError);
            }
        }
    }

    @Override
    protected void onCancelled ()
    {
        super.onCancelled();
        Log.w(getClass().getSimpleName(), "task cancelled!" );
        Context context = contextRef.get();
        if (listener != null && context != null) {
            listener.onCancelled(context, this);
        }
    }

    public void publishProgress(SuntimesCalendarTaskProgress primary, SuntimesCalendarTaskProgress secondary) {
        super.publishProgress( primary != null ? new SuntimesCalendarTaskProgress(primary) : null,
                               secondary != null ? new SuntimesCalendarTaskProgress(secondary) : null );
    }

    @Override
    protected void onProgressUpdate(SuntimesCalendarTaskProgress... progress)
    {
        Context context = contextRef.get();
        if (listener != null && context != null) {
            listener.onProgress(context, progress);
        }
    }

    public SuntimesCalendarTaskProgress createProgressObj(int i, int n, String message) {
        return new SuntimesCalendarTaskProgress(i, n, message);
    }

    protected SuntimesCalendarTaskListener listener;
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

}
