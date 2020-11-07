/**
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

package com.forrestguice.suntimeswidget.calendar.task;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * SuntimesCalendarTaskListener
 */
public abstract class SuntimesCalendarTaskListener implements Parcelable
{
    public void onStarted(Context context, SuntimesCalendarTaskBase task, String message) {}
    public void onProgress(Context context, SuntimesCalendarTaskProgress... progress) {}
    public void onSuccess(Context context, SuntimesCalendarTaskBase task, String message) {}
    public void onCancelled(Context context, SuntimesCalendarTaskBase task) {}
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
