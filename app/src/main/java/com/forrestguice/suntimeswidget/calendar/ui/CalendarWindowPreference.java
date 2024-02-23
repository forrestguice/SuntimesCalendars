/**
    Copyright (C) 2020-2024 Forrest Guice
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
import android.os.Build;
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
    public static final String KEY_START= "0";
    public static final String KEY_END = "1";

    protected String defaultStartValue = getDefaultStartValue() + "";
    protected String defaultEndValue = getDefaultEndValue() + "";

    protected TextView text0, text1;
    protected SeekBar seek0, seek1;
    protected int value0, value1;
    protected TextView text_message;

    public CalendarWindowPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.layout_pref_calendarwindow);
        if (Build.VERSION.SDK_INT >= 21) {
            setLayoutResource(R.layout.layout_pref_material);
        } else {
            setLayoutResource(R.layout.layout_pref);
        }
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        text0 = (TextView) view.findViewById(R.id.label_past);
        seek0 = (SeekBar) view.findViewById(R.id.seekbar_past);
        if (seek0 != null) {
            seek0.setMax(getMaxValue());
            seek0.setOnSeekBarChangeListener(onSeek0);
        }

        text1 = (TextView) view.findViewById(R.id.label_future);
        seek1 = (SeekBar) view.findViewById(R.id.seekbar_future);
        if (seek1 != null) {
            seek1.setMax(getMaxValue());
            seek1.setOnSeekBarChangeListener(onSeek1);
        }

        text_message = (TextView) view.findViewById(R.id.txt_message0);
        if (text_message != null) {
            text_message.setVisibility(View.GONE);
        }

        updateViews(getContext());
        updateSummary();
    }

    private final SeekBar.OnSeekBarChangeListener onSeek0 = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                value0 = progress + 1;
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
                value1 = progress + 1;
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
            text0.setText(context.getResources().getQuantityString(getAgoStringResID(), value0, value0));
        }
        if (seek0 != null) {
            seek0.setProgress(value0 - 1);
        }
        if (text1 != null) {
            text1.setText(context.getResources().getQuantityString(getFromNowStringResID(), value1, value1));
        }
        if (seek1 != null) {
            seek1.setProgress(value1 - 1);
        }
        if (text_message != null && message != null)
        {
            text_message.setText(message);
            text_message.setVisibility(View.VISIBLE);

            if (messageClickListener != null) {
                text_message.setOnClickListener(messageClickListener);
            }
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
            editor.putString(key + KEY_START, value0 * getValueToMillisFactor() + "");
            editor.putString(key + KEY_END, value1 * getValueToMillisFactor() + "");
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
        this.defValue = defValue;

        String defaultValue = (String) defValue;
        if (defaultValue != null)
        {
            String[] v = defaultValue.split(",");
            if (v.length >= 2) {
                defaultStartValue = v[0];
                defaultEndValue = v[1];
            }
        }

        String key = getKey();
        SharedPreferences sharedPreferences = getSharedPreferences();
        long millis0 = Long.parseLong(sharedPreferences.getString(key + KEY_START, defaultStartValue));
        long millis1 = Long.parseLong(sharedPreferences.getString(key + KEY_END, defaultEndValue));
        value0 = (int)(millis0 * getValueFromMillisFactor());
        value1 = (int)(millis1 * getValueFromMillisFactor());
        updateSummary();
    }

    private Object defValue;    // cached value
    protected void resetInitialValue() {
        onSetInitialValue(true, defValue);
    }

    @Override
    public void setSummary( CharSequence value )
    {
        if (summary0 == null) {
            summary0 = ((value != null) ? value.toString() : getSummary().toString());
            //Log.d("DEBUG", "summary to: " + value);
        }
        super.setSummary(makeSummary(getContext()));
    }
    private String summary0 = null;

    @Nullable
    private CharSequence makeSummary(Context context)
    {
        String label0 = context.getResources().getQuantityString(getAgoStringResID(), value0, value0);
        String label1 = context.getResources().getQuantityString(getFromNowStringResID(), value1, value1);
        return (summary0 != null) ? String.format(summary0, label0, label1) : summary0;
    }

    protected void updateSummary() {
        setSummary(makeSummary(getContext()));
    }

    protected int maxValue = 9;
    public int getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(int value)
    {
        maxValue = value;
        if (seek0 != null) {
            seek0.setMax(getMaxValue());
        }
        if (seek1 != null) {
            seek1.setMax(getMaxValue());
        }
    }

    protected CharSequence message = null;
    protected View.OnClickListener messageClickListener = null;

    public void setMessage(@Nullable CharSequence value, @Nullable View.OnClickListener listener) {
        message = value;
        messageClickListener = listener;
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    public static final int MODE_YEARS = 0;
    public static final int MODE_DAYS = 1;

    protected int mode = MODE_YEARS;
    public int getMode() {
        return mode;
    }
    public void setMode(int value)
    {
        mode = value;
        defaultStartValue = getDefaultStartValue();
        defaultEndValue = getDefaultEndValue();
        resetInitialValue();
        updateViews(getContext());
    }

    public static final double YEARS_FROM_MILLIS = 1d / 1000d / 60d / 60d / 24d / 365d;
    public static final long YEARS_TO_MILLIS = 1000L * 60L * 60L * 24L * 365L;

    public static final double DAYS_FROM_MILLIS = 1d / 1000d / 60d / 60d / 24d;
    public static final long DAYS_TO_MILLIS = 1000L * 60L * 60L * 24L;

    protected String getDefaultStartValue()
    {
        switch (mode) {
            case MODE_DAYS: return DAYS_TO_MILLIS + "";
            case MODE_YEARS: default: return YEARS_TO_MILLIS + "";
        }
    }
    protected String getDefaultEndValue()
    {
        switch (mode) {
            case MODE_DAYS: return DAYS_TO_MILLIS + "";
            case MODE_YEARS: default: return YEARS_TO_MILLIS + "";
        }
    }

    public double getValueFromMillisFactor()
    {
        switch (mode) {
            case MODE_DAYS: return DAYS_FROM_MILLIS;
            case MODE_YEARS: default: return YEARS_FROM_MILLIS;
        }
    }

    public long getValueToMillisFactor()
    {
        switch (mode) {
            case MODE_DAYS: return DAYS_TO_MILLIS;
            case MODE_YEARS: default: return YEARS_TO_MILLIS;
        }
    }

    protected int getAgoStringResID()
    {
        switch (mode) {
            case MODE_DAYS: return R.plurals.units_days_ago;
            case MODE_YEARS: default: return R.plurals.units_years_ago;
        }
    }
    protected int getFromNowStringResID()
    {
        switch (mode) {
            case MODE_DAYS: return R.plurals.units_days_fromnow;
            case MODE_YEARS: default: return R.plurals.units_years_fromnow;
        }
    }

}
