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

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TreeSet;

@SuppressWarnings("Convert2Diamond")
public class SuntimesCalendarTask extends SuntimesCalendarTaskBase
{
    public static final String TAG = "SuntimesCalendarTask";

    public SuntimesCalendarTask(Context context)
    {
        super(context);
        contextRef = new WeakReference<Context>(context);
        adapter = new SuntimesCalendarAdapter(context.getContentResolver(), SuntimesCalendarDescriptor.getCalendars(context));
        calendarWindow0 = SuntimesCalendarSettings.loadPrefCalendarWindow0(context);
        calendarWindow1 = SuntimesCalendarSettings.loadPrefCalendarWindow1(context);
    }

    private long[] getWindow()
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
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.setTimeInMillis(now.getTimeInMillis() + calendarWindow1);
        endDate.add(Calendar.YEAR, 1);       // round up to end of year
        endDate.set(Calendar.MONTH, 0);
        endDate.set(Calendar.DAY_OF_MONTH, 0);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        return new long[] { startDate.getTimeInMillis(), endDate.getTimeInMillis() };
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
            for (String calendar : SuntimesCalendarDescriptor.getCalendars(contextRef.get())) {
                SuntimesCalendarSettings.clearNotes(contextRef.get(), calendar);
            }
        }

        long[] window = getWindow();
        boolean retValue = initLocation();
        if (!retValue) {
            return false;
        }

        publishProgress(new SuntimesCalendarTaskProgress(1, 1000, notificationMsgAdding));
        try {
            int c = 0;
            int n = taskItems.size();
            TreeSet<String> calendarSet = new TreeSet<>(taskItems.keySet());
            SuntimesCalendarFactory factory = new SuntimesCalendarFactory();
            for (String calendarName : calendarSet)
            {
                SuntimesCalendarTaskItem item = taskItems.get(calendarName);
                SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(contextRef.get(), calendarName);
                SuntimesCalendar calendar = factory.createCalendar(contextRef.get(), descriptor);
                int action = item.getAction();
                switch (action)
                {
                    case SuntimesCalendarTaskItem.ACTION_DELETE:
                        publishProgress(null, new SuntimesCalendarTaskProgress(0, 1, notificationMsgClearing));
                        retValue = retValue && adapter.removeCalendar(calendarName);
                        SuntimesCalendarSettings.clearNotes(contextRef.get(), calendarName);
                        break;

                    case SuntimesCalendarTaskItem.ACTION_REMINDERS_DELETE:
                        // TODO
                        break;

                    case SuntimesCalendarTaskItem.ACTION_REMINDERS_UPDATE:
                        publishProgress(null, new SuntimesCalendarTaskProgress(1, 1000, notificationMsgReminderUpdating));
                        // no-break; fall through to next case

                    case SuntimesCalendarTaskItem.ACTION_UPDATE:
                    default:
                        if (calendar != null)
                        {
                            switch (action)
                            {
                                case SuntimesCalendarTaskItem.ACTION_REMINDERS_UPDATE:
                                    Log.d("DEBUG", "ACTION_REMINDERS_UPDATE");
                                    retValue = retValue && updateCalendarReminders(calendar, new SuntimesCalendarTaskProgress(0, 1, calendar.calendarTitle()));
                                    break;
                                default:
                                    retValue = retValue && initCalendar(calendar, window, new SuntimesCalendarTaskProgress(c, n, calendar.calendarTitle()));
                                    break;
                            }
                            if (!retValue) {
                                lastError = calendar.lastError();
                            }
                        } else {
                            lastError = "Unrecognized calendar " + calendarName;
                            Log.w(TAG, lastError);
                            if (taskItems.size() == 1 || c == (n-1)) {
                                return false;
                            }
                        }
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
    private boolean initCalendar(@NonNull SuntimesCalendar calendar, @NonNull long[] window, @NonNull SuntimesCalendarTaskProgress progress) throws SecurityException
    {
        if (window.length != 2) {
            Log.e(TAG, "initCalendar: invalid window with length " + window.length);
            return false;
        }

        boolean retValue = true;
        long calendarID = adapter.queryCalendarID(calendar.calendarName());
        if (calendarID != -1) {
            retValue = (adapter.removeCalendarEventsBefore(calendarID, window[0]) > 0);
        }

        long bench_start = System.nanoTime();
        retValue = retValue && calendar.initCalendar(new SuntimesCalendarSettings(), adapter, this, progress, window);
        long bench_end = System.nanoTime();
        Log.i(TAG, "initCalendar (" + calendar + ") in " + ((bench_end - bench_start) / 1000000.0) + " ms");

        return retValue;
    }

    /**
     * updateCalendarReminders
     */
    private boolean updateCalendarReminders(@NonNull SuntimesCalendar calendar, @NonNull SuntimesCalendarTaskProgress progress)
    {
        publishProgress(progress);
        return adapter.updateCalendarReminders(contextRef.get(), calendar.calendarName());
    }

}
