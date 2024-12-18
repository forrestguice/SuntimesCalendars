/**
    Copyright (C) 2019-2024 Forrest Guice
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.ImageViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.forrestguice.suntimescalendars.R;

public class SuntimesCalendarPreference extends CheckBoxPreference
{
    protected FloatingActionButton button;
    protected View layout;

    public SuntimesCalendarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public SuntimesCalendarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public SuntimesCalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SuntimesCalendarPreference(Context context) {
        super(context);
        init(context);
    }

    protected void init(Context context)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            setLayoutResource(R.layout.layout_pref_calendar_material);
        } else {
            setLayoutResource(R.layout.layout_pref_calendar);
        }
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        layout = view.findViewById(R.id.layout_pref);
        if (layout != null && backgroundColor != null) {
            layout.setBackgroundColor(backgroundColor);
        }

        button = (FloatingActionButton) view.findViewById(R.id.button_options);
        if (button != null)
        {
            if (iconColor != null) {
                ImageViewCompat.setImageTintList(button, iconColor);
            }
            button.setOnClickListener(onIconClick);
        }
    }

    @Override
    public void setSummary( CharSequence value )
    {
        if (summary0 == null) {
            summary0 = ((value != null) ? value : getSummary());
        }
        super.setSummary(makeSummary(getContext()));
    }

    private CharSequence summary0 = null;
    private CharSequence makeSummary(Context context)
    {
        if (resID_noteFormat != -1)
        {
            if (note != null) {
                return (summary0 == null || summary0.toString().isEmpty())
                        ? note : context.getString(resID_noteFormat, summary0, note);
            } else {
                return summary0;
            }
        } else {
            return summary0 + " " + note;
        }
    }

    private CharSequence note = null;
    public void setNote(CharSequence value)
    {
        note = value;
        setSummary(makeSummary(getContext()));
    }

    private int resID_noteFormat = -1;
    public void setNoteFormat( int stringFormatResourceID ) {
        resID_noteFormat = stringFormatResourceID;
    }

    private Integer backgroundColor = null;
    public void setBackgroundColor(Integer color)
    {
        backgroundColor = color;
        if (layout != null && backgroundColor != null) {
            layout.setBackgroundColor(backgroundColor);
        }
    }

    private ColorStateList iconColor = null;
    public void setIconColor(ColorStateList color) {
        iconColor = color;
        if (button != null && iconColor != null) {
            ImageViewCompat.setImageTintList(button, iconColor);
        }
    }

    public View.OnClickListener onIconClick = null;
    public void setOnIconClickListener(View.OnClickListener listener) {
        onIconClick = listener;
        if (button != null) {
            button.setOnClickListener(onIconClick);
        }
    }

    public void performClickIcon() {
        if (button != null) {
            button.performClick();
        }
    }

}
