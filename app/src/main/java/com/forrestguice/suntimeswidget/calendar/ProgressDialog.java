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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;

public class ProgressDialog extends DialogFragment
{
    private TextView txtMessage, txtMessageSecondary;
    private ProgressBar progressPrimary, progressSecondary;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title");
            message0 = savedInstanceState.getString("message0");
            message1 = savedInstanceState.getString("message1");
        }

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();

        final ViewGroup viewGroup = null;
        View dialogContent = inflater.inflate(R.layout.layout_dialog_progress, viewGroup);
        initViews(dialogContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);

        if (title != null) {
            builder.setTitle(title);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            Context context = getContext();
            if (context != null) {
                updateViews();
            }
        }
    };

    private void initViews(View dialogContent)
    {
        txtMessage = (TextView)dialogContent.findViewById(R.id.txt_message0);
        txtMessageSecondary = (TextView)dialogContent.findViewById(R.id.txt_message1);
        progressPrimary = (ProgressBar)dialogContent.findViewById(R.id.progress0);
        progressSecondary = (ProgressBar)dialogContent.findViewById(R.id.progress1);
    }

    private void updateViews()
    {
        if (txtMessage != null) {
            txtMessage.setText(message0);
            txtMessage.setVisibility((message0 == null || message0.isEmpty()) ? View.GONE : View.VISIBLE);
        }

        if (txtMessageSecondary != null) {
            txtMessageSecondary.setText(message1);
            txtMessageSecondary.setVisibility((message1 == null || message1.isEmpty()) ? View.GONE : View.VISIBLE);
        }
    }

    private String title;
    public void setTitle(String title)
    {
        this.title = title;
    }

    private String message0;
    public void setMessage(String msg)
    {
        this.message0 = msg;
        updateViews();
    }

    private String message1;
    public void setMessageSecondary(String msg)
    {
        this.message1 = msg;
        updateViews();
    }

    public void setMax(int max)
    {
        progressPrimary.setMax(max);
        progressSecondary.setMax(max);
    }

    public void setProgress(int value)
    {
        if (Build.VERSION.SDK_INT >= 24) {
            progressPrimary.setProgress(value, true);
        } else progressPrimary.setProgress(value);
    }
    public void setProgressSecondary(int value)
    {
        progressPrimary.setSecondaryProgress(value);
    }

    public void setSecondaryProgress(int value)
    {
        if (Build.VERSION.SDK_INT >= 24) {
            progressSecondary.setProgress(value, true);
        } else progressSecondary.setProgress(value);
    }

    public boolean isShowing()
    {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
        {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("message0", message0);
        outState.putString("message1", message1);
    }

}
