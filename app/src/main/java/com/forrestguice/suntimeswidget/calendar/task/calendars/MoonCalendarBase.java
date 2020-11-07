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

package com.forrestguice.suntimeswidget.calendar.task.calendars;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@SuppressWarnings("Convert2Diamond")
public abstract class MoonCalendarBase extends SuntimesCalendarBase
{
    protected NumberFormat distanceFormatter = null;
    protected String formatDistanceString(double distance)
    {
        if (distanceFormatter == null)
        {
            distanceFormatter = new DecimalFormat();
            distanceFormatter.setMinimumFractionDigits(0);
            distanceFormatter.setMaximumFractionDigits(2);
        }
        return distanceFormatter.format(distance);
    }

    protected double lookupMoonDistance(@NonNull Context context, @NonNull ContentResolver resolver, long dateMillis )
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
}
