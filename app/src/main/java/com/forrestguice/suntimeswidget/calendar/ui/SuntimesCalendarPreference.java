/**
    Copyright (C) 2019-2020 Forrest Guice
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
import android.support.v4.widget.ImageViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.forrestguice.suntimescalendars.R;

public class SuntimesCalendarPreference extends CheckBoxPreference
{
    private ImageView icon;

    public SuntimesCalendarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public SuntimesCalendarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SuntimesCalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuntimesCalendarPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        View iconView = view.findViewById(android.R.id.icon);
        if (iconView instanceof ImageView)
        {
            icon = (ImageView)iconView;

            if (iconColor != null)
            {
                icon.setImageDrawable(icon.getDrawable().mutate());
                int padding = (int) getContext().getResources().getDimension(R.dimen.calendarpref_icon_padding);
                icon.setPadding(padding, padding, padding, padding);
                ImageViewCompat.setImageTintList(icon, iconColor);
            }

            TypedValue selectableItemBackground = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true);
            icon.setBackgroundResource(selectableItemBackground.resourceId);

            if (onIconClick != null) {
                icon.setOnClickListener(onIconClick);
            }
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

    private ColorStateList iconColor = null;
    public void setIconColor(ColorStateList color) {
        iconColor = color;
        if (icon != null && iconColor != null) {
            icon.setImageDrawable(icon.getDrawable().mutate());
            ImageViewCompat.setImageTintList(icon, iconColor);
        }
    }

    public View.OnClickListener onIconClick = null;
    public void setOnIconClickListener(View.OnClickListener listener) {
        onIconClick = listener;
        if (icon != null) {
            icon.setOnClickListener(onIconClick);
        }
    }

    public void performClickIcon() {
        icon.performClick();
    }

}
