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

import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;

import java.util.ArrayList;
import java.util.Arrays;

public class SuntimesCalendarTaskService extends Service
{
    public static final String TAG = "SuntimesCalendarsTask";
    public static final String ACTION_UPDATE_CALENDARS = "update_calendars";
    public static final String ACTION_CLEAR_CALENDARS = "clear_calendars";

    public static final String EXTRA_CALENDAR_ITEMS = "calendar_items";
    public static final String EXTRA_CALENDAR_LISTENER = "calendar_listener";
    public static final String EXTRA_SERVICE_LISTENER = "service_listener";

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return taskBinder;
    }

    private final SuntimesCalendarTaskServiceBinder taskBinder = new SuntimesCalendarTaskServiceBinder();
    public class SuntimesCalendarTaskServiceBinder extends Binder
    {
        SuntimesCalendarTaskService getService() {
            return SuntimesCalendarTaskService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = ((intent != null) ? intent.getAction() : null);
        if (action != null)
        {
            SuntimesCalendarServiceListener serviceListener = intent.getParcelableExtra(EXTRA_SERVICE_LISTENER);
            SuntimesCalendarTask.SuntimesCalendarTaskListener listener = intent.getParcelableExtra(EXTRA_CALENDAR_LISTENER);
            if (action.equals(ACTION_UPDATE_CALENDARS))
            {
                Log.d(TAG, "onStartCommand: " + action);
                boolean started = runCalendarTask(this, intent, false, false, listener);
                signalOnStartCommand(started);
                if (serviceListener != null) {
                    serviceListener.onStartCommand(started);
                }

            } else if (action.equals(ACTION_CLEAR_CALENDARS)) {
                Log.d(TAG, "onStartCommand: " + action);
                boolean started = runCalendarTask(this, intent, true, false, listener);
                signalOnStartCommand(started);
                if (serviceListener != null) {
                    serviceListener.onStartCommand(started);
                }

            } else Log.d(TAG, "onStartCommand: unrecognized action: " + action);
        } else Log.d(TAG, "onStartCommand: null action");
        return START_NOT_STICKY;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final int NOTIFICATION_PROGRESS = 10;
    public static final int NOTIFICATION_COMPLETE = 20;

    private static SuntimesCalendarTask calendarTask = null;
    private static SuntimesCalendarTask.SuntimesCalendarTaskListener calendarTaskListener;
    private static NotificationCompat.Builder progressNotification;
    public boolean runCalendarTask(final Context context, Intent intent, boolean clearCalendars, boolean clearPending, @Nullable final SuntimesCalendarTask.SuntimesCalendarTaskListener listener)
    {
        ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> items = new ArrayList<>();
        if (!clearCalendars) {
            items = loadItems(intent, clearPending);
        }

        if (isBusy()) {
            Log.w(TAG, "runCalendarTask: A task is already running! ignoring...");
            return false;
        }

        calendarTask = new SuntimesCalendarTask(context);
        calendarTaskListener = new SuntimesCalendarTask.SuntimesCalendarTaskListener()
        {
            @Override
            public void onStarted(Context context, SuntimesCalendarTask task, String message)
            {
                if (listener != null) {
                    listener.onStarted(context, task, message);
                }

                if (!task.getFlagClearCalendars() && hasUpdateAction(task.getItems()))
                {
                    signalOnBusyStatusChanged(true);
                    signalOnProgressMessage(0, 1, getString(R.string.calendars_notification_adding));

                    progressNotification = createProgressNotification(context, message);
                    startService(new Intent( context, SuntimesCalendarTaskService.class ));  // bind the service to itself (to keep things running if the activity unbinds)
                    startForeground(NOTIFICATION_PROGRESS, progressNotification.build());

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.cancel(NOTIFICATION_COMPLETE);
                }
            }

            private boolean hasUpdateAction(SuntimesCalendarTask.SuntimesCalendarTaskItem[] items)
            {
                for (SuntimesCalendarTask.SuntimesCalendarTaskItem item : items) {
                    if (item.getAction() == SuntimesCalendarTask.SuntimesCalendarTaskItem.ACTION_UPDATE) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onProgress(Context context, SuntimesCalendarTask.CalendarTaskProgress... progress)
            {
                if (listener != null) {
                    listener.onProgress(context, progress);
                }

                if (progress.length > 1 && progress[0] != null && progress[1] != null)
                {
                    signalOnProgressMessage(progress[0].itemNum(), progress[0].getCount(), progress[1].itemNum(), progress[1].getCount(), progress[1].getMessage());
                    if (progressNotification != null) {
                        progressNotification.setProgress(progress[1].getCount(), progress[1].itemNum(), progress[1].isIndeterminate());  // TODO: secondary progress
                        startForeground(NOTIFICATION_PROGRESS, progressNotification.build());
                    }

                } else if (progress.length > 0 && progress[0] != null) {
                    signalOnProgressMessage(progress[0].itemNum(), progress[0].getCount(), progress[0].getMessage());
                    if (progressNotification != null) {
                        progressNotification.setProgress(progress[0].getCount(), progress[0].itemNum(), progress[0].isIndeterminate());
                        startForeground(NOTIFICATION_PROGRESS, progressNotification.build());
                    }
                }
            }

            @Override
            public void onSuccess(Context context, SuntimesCalendarTask task, String message)
            {
                if (listener != null) {
                    listener.onSuccess(context, task, message);
                }

                NotificationCompat.Builder notificationBuilder = createSuccessNotification(context, message);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(NOTIFICATION_COMPLETE, notificationBuilder.build());
                signalOnBusyStatusChanged(false);
                stopForeground(true);
                stopSelf();
            }

            @Override
            public void onCancelled(Context context, SuntimesCalendarTask task)
            {
                if (listener != null) {
                    listener.onCancelled(context, task);
                }

                signalOnBusyStatusChanged(false);
                stopForeground(true);
                stopSelf();
            }

            @Override
            public void onFailed(final Context context, final String errorMsg)
            {
                if (listener != null) {
                    listener.onFailed(context, errorMsg);
                }

                Intent errorIntent = new Intent(context, SuntimesCalendarErrorActivity.class);
                errorIntent.putExtra(SuntimesCalendarErrorActivity.EXTRA_ERROR_MESSAGE, errorMsg);
                errorIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(errorIntent);

                signalOnBusyStatusChanged(false);
                stopForeground(true);
                stopSelf();
            }
        };
        calendarTask.setTaskListener(calendarTaskListener);

        if (clearCalendars) {
            calendarTask.setFlagClearCalendars(true);
        }
        calendarTask.setItems(items.toArray(new SuntimesCalendarTask.SuntimesCalendarTaskItem[0]));
        calendarTask.execute();
        return true;
    }

    private static NotificationCompat.Builder createProgressNotification(Context context, String message)
    {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        notification.setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_action_update)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(getSuntimesCalendarsPendingIntent(context))
                .setProgress(0, 0, true);
        return notification;
    }

    private static NotificationCompat.Builder createSuccessNotification(Context context, String message)
    {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        notification.setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_action_calendar)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(getCalendarPendingIntent(context)).setAutoCancel(true)
                .setProgress(0, 0, false);
        return notification;
    }

    private static PendingIntent getCalendarPendingIntent(Context context)
    {
        Intent intent = getCalendarIntent();
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private static PendingIntent getSuntimesCalendarsPendingIntent(Context context)
    {
        Intent intent = new Intent(context, SuntimesCalendarActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    public boolean isBusy()
    {
        if (calendarTask != null)
        {
            switch (calendarTask.getStatus())
            {
                case PENDING:
                case RUNNING:
                    return true;

                case FINISHED:
                default:
                    return false;
            }
        } else return false;
    }

    private String lastProgressMessage;
    public String getLastProgressMessage()
    {
        return lastProgressMessage;
    }

    public static ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> loadItems(Intent intent, boolean clearPending)
    {
        SuntimesCalendarTask.SuntimesCalendarTaskItem[] items;
        Parcelable[] parcelableArray = intent.getParcelableArrayExtra(EXTRA_CALENDAR_ITEMS);
        if (parcelableArray != null) {
            items = Arrays.copyOf(parcelableArray, parcelableArray.length, SuntimesCalendarTask.SuntimesCalendarTaskItem[].class);
        } else items = new SuntimesCalendarTask.SuntimesCalendarTaskItem[0];

        if (clearPending) {
            intent.removeExtra(EXTRA_CALENDAR_ITEMS);
        }
        return new ArrayList<>(Arrays.asList(items));
    }

    public static Intent getCalendarIntent()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            Uri.Builder uriBuilder = CalendarContract.CONTENT_URI.buildUpon();
            uriBuilder.appendPath("time");
            ContentUris.appendId(uriBuilder, System.currentTimeMillis());
            intent = intent.setData(uriBuilder.build());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * SuntimesCalendarServiceListener
     */
    public static abstract class SuntimesCalendarServiceListener implements Parcelable
    {
        public void onStartCommand(boolean result) {}
        public void onBusyStatusChanged(boolean isBusy) {}
        public void onProgressMessage(int i, int n, String message) {}
        public void onProgressMessage(int i, int n, int j, int m, String message) {}

        public SuntimesCalendarServiceListener() {}
        protected SuntimesCalendarServiceListener(Parcel in) {}

        @Override
        public void writeToParcel(Parcel dest, int flags) {}

        @Override
        public int describeContents() {
            return 0;
        }
    }

    private ArrayList<SuntimesCalendarServiceListener> serviceListeners = new ArrayList<>();
    public void addCalendarServiceListener(SuntimesCalendarServiceListener listener)
    {
        serviceListeners.add(listener);
    }
    public void removeCalendarServiceListener(SuntimesCalendarServiceListener listener)
    {
        if (serviceListeners.contains(listener)) {
            serviceListeners.remove(listener);
        }
    }

    private void signalOnStartCommand(boolean result)
    {
        for (SuntimesCalendarServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onStartCommand(result);
            }
        }
    }

    private void signalOnBusyStatusChanged(boolean isBusy)
    {
        for (SuntimesCalendarServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onBusyStatusChanged(isBusy);
            }
        }
    }

    private void signalOnProgressMessage(int i, int n, String message)
    {
        lastProgressMessage = message;
        for (SuntimesCalendarServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onProgressMessage(i, n, message);
            }
        }
    }

    private void signalOnProgressMessage(int i, int n, int j, int m, String message)
    {
        lastProgressMessage = message;
        for (SuntimesCalendarServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onProgressMessage(i, n, j, m, message);
            }
        }
    }

}
