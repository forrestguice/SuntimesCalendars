/**
    Copyright (C) 2019 Forrest Guice
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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.CheckBoxPreference;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

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
    protected void onBindView(View view) {
        super.onBindView(view);

        View iconView = view.findViewById(android.R.id.icon);
        if (iconView != null && iconView instanceof ImageView)
        {
            icon = (ImageView)iconView;

            if (iconColor != null) {
                ImageViewCompat.setImageTintList(icon, iconColor);
            }

            if (onIconClick != null) {
                icon.setOnClickListener(onIconClick);
            }
        }
    }

    private ColorStateList iconColor = null;
    public void setIconColor(ColorStateList color) {
        iconColor = color;
    }

    @Override
    public void setIcon(int resId)
    {
        super.setIcon(resId);
    }

    public View.OnClickListener onIconClick = null;
    public void setOnIconClickListener(View.OnClickListener listener) {
        onIconClick = listener;
        if (icon != null) {
            icon.setOnClickListener(onIconClick);
        }
    }

}
