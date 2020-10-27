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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;

public class CalendarWindowPreference extends DialogPreference
{
    public static final String KEY_START= "_START";
    public static final String KEY_END = "_END";

    public static final double YEARS_FROM_MILLIS = 1d / 1000d / 60d / 60d / 24d / 365d;
    public static final long YEARS_TO_MILLIS = 1000L * 60L * 60L * 24L * 365L;

    protected long defaultStartValue = YEARS_TO_MILLIS;
    protected long defaultEndValue = YEARS_TO_MILLIS;

    protected TextView text0, text1;
    protected SeekBar seek0, seek1;
    protected int years0, years1;

    public CalendarWindowPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.layout_pref_calendarwindow);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        text0 = (TextView) view.findViewById(R.id.label_past);
        seek0 = (SeekBar) view.findViewById(R.id.seekbar_past);
        if (seek0 != null) {
            seek0.setMax(9);
            seek0.setOnSeekBarChangeListener(onSeek0);
        }

        text1 = (TextView) view.findViewById(R.id.label_future);
        seek1 = (SeekBar) view.findViewById(R.id.seekbar_future);
        if (seek1 != null) {
            seek1.setMax(9);
            seek1.setOnSeekBarChangeListener(onSeek1);
        }

        updateViews(getContext());
        updateSummary();
    }

    private SeekBar.OnSeekBarChangeListener onSeek0 = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                years0 = progress + 1;
                updateViews(getContext());
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    private SeekBar.OnSeekBarChangeListener onSeek1 = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                years1 = progress + 1;
                updateViews(getContext());
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    protected void updateViews(Context context)
    {
        if (text0 != null) {
            text0.setText(context.getResources().getQuantityString(R.plurals.units_years_ago, years0, years0));
        }
        if (seek0 != null) {
            seek0.setProgress(years0 - 1);
        }
        if (text1 != null) {
            text1.setText(context.getResources().getQuantityString(R.plurals.units_years_fromnow, years1, years1));
        }
        if (seek1 != null) {
            seek1.setProgress(years1 - 1);
        }
    }

    @Override
    protected void onDialogClosed(boolean result)
    {
        super.onDialogClosed(result);

        if (result)
        {
            String key = getKey();
            SharedPreferences.Editor editor = getEditor();
            editor.putLong(key + KEY_START, years0 * YEARS_TO_MILLIS);
            editor.putLong(key + KEY_END, years1 * YEARS_TO_MILLIS);
            editor.commit();

            updateSummary();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defValue)
    {
        super.onSetInitialValue(restoreValue, defValue);

        String defaultValue = (String) defValue;
        if (defaultValue != null)
        {
            String[] v = defaultValue.split(",");
            if (v.length >= 2) {
                defaultStartValue = Long.parseLong(v[0]);
                defaultEndValue = Long.parseLong(v[1]);
            }
        }

        String key = getKey();
        SharedPreferences sharedPreferences = getSharedPreferences();
        long millis0 = sharedPreferences.getLong(key + KEY_START, defaultStartValue);
        long millis1 = sharedPreferences.getLong(key + KEY_END, defaultEndValue);
        years0 = (int)(millis0 * YEARS_FROM_MILLIS);
        years1 = (int)(millis1 * YEARS_FROM_MILLIS);
        updateSummary();
    }

    @Override
    public void setSummary( CharSequence value )
    {
        if (summary0 == null) {
            summary0 = ((value != null) ? value.toString() : getSummary().toString());
            Log.d("DEBUG", "summary to: " + value);
        }
        super.setSummary(makeSummary(getContext()));
    }
    private String summary0 = null;

    @Nullable
    private CharSequence makeSummary(Context context)
    {
        String label0 = context.getResources().getQuantityString(R.plurals.units_years_ago, years0, years0);
        String label1 = context.getResources().getQuantityString(R.plurals.units_years_fromnow, years1, years1);
        return (summary0 != null) ? String.format(summary0, label0, label1) : summary0;
    }

    protected void updateSummary() {
        setSummary(makeSummary(getContext()));
    }
}
