/**
    Copyright (C) 2023 Forrest Guice
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
import android.os.Build;
import android.util.AttributeSet;

import com.forrestguice.suntimescalendars.R;

public class PreferenceCategory extends android.preference.PreferenceCategory
{
    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public PreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PreferenceCategory(Context context) {
        super(context);
        init(context);
    }

    protected void init(Context context)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            setLayoutResource(R.layout.layout_pref_category_material);
        } else {
            setLayoutResource(R.layout.layout_pref_category);
        }
    }

}
