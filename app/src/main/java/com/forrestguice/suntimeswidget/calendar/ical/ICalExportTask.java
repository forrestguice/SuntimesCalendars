/**
    Copyright (C) 2024 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.calendar.ical;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettingsFactory;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskBase;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskInterface;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskItem;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskListener;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskProgress;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LATITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LENGTH_UNITS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LOCATION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE;

/**
 * ICalExportTask
 */
public class ICalExportTask extends ExportTask implements SuntimesCalendarTaskInterface
{
    public static final String TAG = "ICalExport";

    public static final long MIN_WAIT_TIME = 4000;

    protected int config_provider_version = 0;
    protected String config_location_name = "";
    protected String config_location_latitude = "";
    protected String config_location_longitude = "";
    protected String config_location_altitude = "";
    protected String config_provider_length_units = Utils.LengthUnit.METRIC.name();

    protected long calendarWindow0 = -1, calendarWindow1 = -1;
    protected String lastError = null;

    public ICalExportTask(Context context, String exportTarget) {
        super(context, exportTarget);
        initTask(context);
    }

    public ICalExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache) {
        super(context, exportTarget, useExternalStorage, saveToCache);
        initTask(context);
    }

    public ICalExportTask(Context context, Uri exportUri) {
        super(context, exportUri);
        initTask(context);
    }

    @Override
    @Nullable
    public Uri getFileUri() {
        return exportUri;
    }

    private void initTask(Context context)
    {
        ext = ICalFormat.ICS_FILEEXT;
        mimeType = ICalFormat.ICS_MIMETYPE;
        setMinWaitTime(MIN_WAIT_TIME);
        calendarWindow0 = SuntimesCalendarSettings.loadPrefCalendarWindow0(context);
        calendarWindow1 = SuntimesCalendarSettings.loadPrefCalendarWindow1(context);
    }

    private SuntimesCalendar calendar = null;
    public void setCalendar(SuntimesCalendar value) {
        calendar = value;
    }
    @Nullable
    public SuntimesCalendar getCalendar() {
        return calendar;
    }

    protected HashMap<String, SuntimesCalendarTaskItem> taskItems = new HashMap<>();
    public SuntimesCalendarTaskItem[] getItems() {
        return taskItems.values().toArray(new SuntimesCalendarTaskItem[0]);
    }

    @Override
    protected boolean export(final Context context, final BufferedOutputStream out) throws IOException
    {
        if (listener != null) {
            listener.onStarted(context, this, context.getString(R.string.calendars_notification_writing));
        }

        if (calendar != null) {
            return export(context, calendar, out);

        } else {
            TreeSet<String> calendarSet = new TreeSet<>(taskItems.keySet());
            SuntimesCalendarFactory factory = new SuntimesCalendarFactory();
            boolean retValue = true;
            for (String calendarName : calendarSet)
            {
                SuntimesCalendarTaskItem item = taskItems.get(calendarName);
                SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(contextRef.get(), calendarName);
                SuntimesCalendar calendar = (descriptor != null ? factory.createCalendar(contextRef.get(), descriptor, getSettings()) : null);
                if (item != null && calendar != null)
                {
                    int action = item.getAction();
                    //Log.d("DEBUG", "exportTask: " + calendar.calendarName() + " with action " + action);
                    switch (action)
                    {
                        case SuntimesCalendarTaskItem.ACTION_UPDATE:
                        case SuntimesCalendarTaskItem.ACTION_CREATE_FILE:
                        default:
                            retValue &= export(context, calendar, out);
                            break;
                    }
                }
            }
            return true;
        }
    }

    @Override
    protected void onPostExecute(ExportResult results)
    {
        super.onPostExecute(results);

        Context context = contextRef.get();
        if (listener != null && context != null)
        {
            if (results.getResult() && lastError == null) {
                listener.onSuccess(context, this, context.getString(R.string.calendars_notification_written));
            } else listener.onFailed(context, context.getString(R.string.calendars_notification_writing_failed));
        }
    }

    protected boolean export(final Context context, final SuntimesCalendar calendar, final BufferedOutputStream out) throws IOException
    {
        if (calendar == null) {
            Log.w("DEBUG", "export: SuntimesCalendar obj is null!");
            return false;
        }

        final ICalFormat iCalFormat = new ICalFormat();
        SuntimesCalendar.CalendarInitializer initializer = new SuntimesCalendar.CalendarInitializer()
        {
            @Override
            public long calendarID() {
                return 10;
            }

            @Override
            public boolean onStarted() {
                try {
                    String label = getSettings().loadPrefCalendarTitle(context, calendar.calendarName(), calendar.calendarTitle());
                    out.write(iCalFormat.icsCalendarStart(calendar.calendarName(), label).getBytes(ICalFormat.ENCODING));
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "Failed to write to file! " + e);
                    return false;
                }
            }

            @Override
            public void processEventValues(ContentValues... eventValues)
            {
                try {
                    out.write(iCalFormat.icsEvents(eventValues).getBytes(ICalFormat.ENCODING));
                } catch (IOException e) {
                    Log.e(TAG, "Failed to write to file! " + e);
                }
            }

            @Override
            public void onFinished() {
                try {
                    out.write(iCalFormat.icsCalendarEnd().getBytes(ICalFormat.ENCODING));
                } catch (IOException e) {
                    Log.e(TAG, "Failed to write to file! " + e);
                }
            }
        };

        Log.i(TAG, "ICalExportTask: calendar: " + calendar.calendarName() + " to " + exportUri);
        final SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(context.getContentResolver(), new String[] { calendar.calendarName() });
        queryConfig();

        SuntimesCalendarTaskProgress progress0 = createProgressObj(0, 1, "");    // TODO
        SuntimesCalendarTaskProgress progress1 = createProgressObj(0, 1, "");
        publishProgress(progress0, progress1);

        calendar.initCalendar(getSettings(), adapter, this, progress1, getWindow(), initializer);
        out.flush();
        return true;
    }

    @Override
    public boolean queryConfig()
    {
        Context context = contextRef.get();
        ContentResolver resolver = (context == null ? null : context.getContentResolver());
        if (resolver != null)
        {
            Uri configUri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG);
            String[] configProjection = new String[] { COLUMN_CONFIG_LOCATION, COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
                    COLUMN_CONFIG_LENGTH_UNITS, COLUMN_CONFIG_PROVIDER_VERSION_CODE };

            try {
                Cursor configCursor = resolver.query(configUri, configProjection, null, null, null);
                if (configCursor != null)
                {
                    configCursor.moveToFirst();
                    for (int i = 0; i < configProjection.length; i++) {
                        config_location_name = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_LOCATION));
                        config_location_latitude = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_LATITUDE));
                        config_location_longitude = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_LONGITUDE));
                        config_location_altitude = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_ALTITUDE));
                        config_provider_version = configCursor.getInt(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_PROVIDER_VERSION_CODE));
                        config_provider_length_units = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_LENGTH_UNITS));
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

    @Override
    public int getProviderVersion() {
        return config_provider_version;
    }

    @Override
    public String[] getLocation() {
        return new String[] { config_location_name, config_location_latitude, config_location_longitude, config_location_altitude };
    }

    @Override
    public String getLengthUnits() {
        return config_provider_length_units;
    }

    @Override
    public long[] getWindow() {
        return getWindow(calendarWindow0, calendarWindow1);
    }

    @Override
    public long[] getWindow(long calendarWindow0, long calendarWindow1) {
        return SuntimesCalendarTaskBase.getWindow(calendarWindow0, calendarWindow1, false);
    }

    @Override
    public SuntimesCalendarSettings getSettings() {
        return ((settings != null) ? settings : SuntimesCalendarSettingsFactory.createSettings());
    }
    @Override
    public void setSettings(SuntimesCalendarSettings value) {
        settings = value;
    }
    protected SuntimesCalendarSettings settings;

    @Override
    public void executeTask() {
        execute();
    }


    @Override
    protected void onCancelled ()
    {
        super.onCancelled();
        Context context = contextRef.get();
        if (listener != null && context != null) {
            Log.w("DEBUG", "task cancelled!" );
            listener.onCancelled(context, this);
        }
    }

    @Override
    public SuntimesCalendarTaskProgress createProgressObj(int i, int n, String message)
    {
        Context context = contextRef.get();
        String title = (context != null ? context.getString(R.string.progress_title1) : null);
        return new SuntimesCalendarTaskProgress(i, n, title, message);
    }

    @Override
    public void publishProgress(SuntimesCalendarTaskProgress primary, SuntimesCalendarTaskProgress secondary) {
        super.publishProgress( primary != null ? new SuntimesCalendarTaskProgress(primary) : null,
                secondary != null ? new SuntimesCalendarTaskProgress(secondary) : null );
    }

    @Override
    public String onFinishedActionID() {
        return SuntimesCalendarTaskInterface.ACTION_SHARE;
    }

    @Override
    public boolean createCalendarReminders(Context context, String calendar, @NonNull SuntimesCalendarTaskProgress progress0) {
        return false;    // not supported
    }

    @Override
    public void setFlagClearCalendars(boolean flag) { /* EMPTY*/ }
    @Override
    public boolean getFlagClearCalendars() {
        return false;
    }

    public void setItems(SuntimesCalendarTaskItem... items)
    {
        taskItems.clear();
        for (SuntimesCalendarTaskItem item : items) {
            taskItems.put(item.getCalendar(), item);
        }
    }

    protected SuntimesCalendarTaskListener listener;
    @Override
    public void setTaskListener( SuntimesCalendarTaskListener listener ) {
        this.listener = listener;
    }

    @Override
    protected void onProgressUpdate(Object... progress)
    {
        super.onProgressUpdate(progress);
        Context context = contextRef.get();
        if (listener != null && context != null)
        {
            SuntimesCalendarTaskProgress[] p = new SuntimesCalendarTaskProgress[progress.length];
            for (int i=0; i<progress.length; i++) {
                p[i] = (SuntimesCalendarTaskProgress) progress[i];
            }
            listener.onProgress(context, p);
        }
    }

}
