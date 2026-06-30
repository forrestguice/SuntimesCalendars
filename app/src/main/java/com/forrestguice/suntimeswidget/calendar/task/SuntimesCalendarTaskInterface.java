/**
    Copyright (C) 2024 Forrest Guice
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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;

public interface SuntimesCalendarTaskInterface
{
    void setFlagClearCalendars( boolean flag );
    boolean getFlagClearCalendars();

    void setTaskListener( SuntimesCalendarTaskListener listener );
    boolean queryConfig();

    void setItems(SuntimesCalendarTaskItem... items);
    SuntimesCalendarTaskItem[] getItems();

    int getProviderVersion();
    String[] getLocation();
    String getLengthUnits();
    long[] getWindow();
    long[] getWindow(long calendarWindow0, long calendarWindow1);

    SuntimesCalendarSettings getSettings();
    void setSettings(SuntimesCalendarSettings settings);

    void executeTask();
    boolean cancel(boolean value);
    boolean isCancelled();

    android.os.AsyncTask.Status getStatus();
    SuntimesCalendarTaskProgress createProgressObj(int i, int n, String message);
    void publishProgress(SuntimesCalendarTaskProgress primary, SuntimesCalendarTaskProgress secondary);

    @Nullable
    Uri getFileUri();
    String onFinishedActionID();
    String ACTION_SHARE = "SHARE";
    String ACTION_CALENDAR = "CALENDAR";

    boolean createCalendarReminders(Context context, String calendar, @NonNull SuntimesCalendarTaskProgress progress0);
}
