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

package com.forrestguice.suntimeswidget.calendar.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.forrestguice.suntimescalendars.R;

public class CalendarWindowPreference extends DialogPreference
{
    public static final String KEY_START= "_START";
    public static final String KEY_END = "_END";

    protected long defaultStartValue = -1;
    protected long defaultEndValue = -1;

    public CalendarWindowPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.layout_pref_calendarwindow);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        String key = getKey();
        SharedPreferences sharedPreferences = getSharedPreferences();
        long startMillis = sharedPreferences.getLong(key + KEY_START, defaultStartValue);
        long endMillis = sharedPreferences.getLong(key + KEY_END, defaultEndValue);
        // TODO: init view here
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        String defaultValue = a.getString(index);
        if (defaultValue != null)
        {
            String[] v = defaultValue.split(",");
            if (v.length >= 2) {
                defaultStartValue = Long.parseLong(v[0]);
                defaultEndValue = Long.parseLong(v[1]);
            }
        }
        return defaultValue;
    }

    @Override
    protected void onDialogClosed(boolean result)
    {
        super.onDialogClosed(result);

        if (result)
        {
            long startMillis = 0;  // TODO: from view
            long endMillis = 0;  // TODO: from view

            String key = getKey();
            SharedPreferences.Editor editor = getEditor();
            editor.putLong(key + KEY_START, startMillis);
            editor.putLong(key + KEY_END, endMillis);
            editor.commit();
        }
    }
}
