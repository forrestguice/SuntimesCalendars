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

package com.forrestguice.suntimeswidget.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;

public class SnackbarUtils
{
    public static int getSnackbarTextResourceID() {
        //return android.support.design.R.id.snackbar_text;    // support libraries
        return com.google.android.material.R.id.snackbar_text;   // androidx
    }

    public static int getSnackbarActionResourceID() {
        //return android.support.design.R.id.snackbar_action;    // support libraries
        return com.google.android.material.R.id.snackbar_action;   // androidx
    }

    @SuppressLint("ResourceType")
    public static void themeSnackbar(Context context, Snackbar snackbar)
    {
        Integer[] colors = new Integer[] { null, null, null };
        int[] colorAttr = new int[] { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor, android.R.attr.selectableItemBackground };
        int[] colorAttrDef = new int[] { android.R.color.primary_text_dark, R.color.text_accent_dark, R.color.dialog_bg, android.R.drawable.list_selector_background };

        TypedArray a = context.obtainStyledAttributes(colorAttr);
        colors[0] = ContextCompat.getColor(context, a.getResourceId(0, colorAttrDef[0]));
        colors[1] = ContextCompat.getColor(context, a.getResourceId(1, colorAttrDef[1]));
        colors[2] = ContextCompat.getColor(context, a.getResourceId(2, colorAttrDef[2]));
        Drawable buttonDrawable = ContextCompat.getDrawable(context, a.getResourceId(3, colorAttrDef[3]));
        int buttonPadding = (int)context.getResources().getDimension(R.dimen.snackbar_button_padding);
        a.recycle();

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(colors[2]);
        snackbar.setActionTextColor(colors[1]);

        TextView snackbarText = (TextView)snackbarView.findViewById(getSnackbarTextResourceID());
        if (snackbarText != null) {
            snackbarText.setTextColor(colors[0]);
            snackbarText.setMaxLines(3);
        }

        View snackbarAction = snackbarView.findViewById(getSnackbarActionResourceID());
        if (snackbarAction != null) {
            if (Build.VERSION.SDK_INT >= 16)
            {
                snackbarAction.setBackground(buttonDrawable);
                snackbarAction.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            }
        }
    }

}
