// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2026 Forrest Guice
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

import android.content.Context;

import com.forrestguice.suntimes.crashreport.ExceptionNotification;
import com.forrestguice.suntimes.crashreport.ExceptionNotification1;
import com.forrestguice.suntimescalendars.BuildConfig;
import com.forrestguice.suntimescalendars.R;

public class ExceptionHandler extends com.forrestguice.suntimes.crashreport.ExceptionHandler
{
    public ExceptionHandler(Context context, Thread.UncaughtExceptionHandler defaultHandler) {
        super(context, defaultHandler);
    }

    @Override
    protected String getAppName(Context context) {
        return context.getString(R.string.app_name);
    }

    @Override
    protected String getAppSupportURL(Context context) {
        return context.getString(R.string.app_support_url0);
    }

    @Override
    protected String getAppVersionInfo() {
        return "SuntimesCalendars " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") [" + BuildConfig.APPLICATION_ID + "]" + (BuildConfig.DEBUG ? " [debug] " : " ") + "[" + BuildConfig.GIT_HASH + "]";
    }

    @Override
    protected ExceptionNotification getNotification() {
        return new ExceptionNotification1()
        {
            @Override
            protected String getAppName(Context context) {
                return context.getString(R.string.app_name);
            }

            @Override
            protected String getChannelID() {
                return "suntimescalendars.notifications.crashreport";
            }

            @Override
            protected int getNotificationID() {
                return -9999;
            }
        };
    }
}
