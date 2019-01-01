/*
    Copyright (C) 2018 Forrest Guice
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

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.forrestguice.suntimescalendars.R;

public class SuntimesCalendarErrorActivity extends AppCompatActivity
{
    public static final String EXTRA_ERROR_MESSAGE = "calendar_error";

    public SuntimesCalendarErrorActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setResult(RESULT_OK);
        super.onCreate(icicle);

        final Context context = this;
        final String errorMsg = getIntent().getStringExtra(EXTRA_ERROR_MESSAGE);

        AlertDialog.Builder errorDialog = new AlertDialog.Builder(context);
        errorDialog.setTitle(context.getString(R.string.calendars_notification_adding_failed))
                .setMessage(errorMsg)
                .setIcon(R.drawable.ic_action_about)
                .setNeutralButton(context.getString(R.string.actionCopyError), null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        SuntimesCalendarErrorActivity.this.finish();
                        overridePendingTransition(0, 0);
                    }
                })
                .setPositiveButton(android.R.string.ok, null);

        Dialog dialog = errorDialog.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                Button neutralButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            ClipData clip = ClipData.newPlainText("SuntimesCalendarErrorMsg", errorMsg);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(context, context.getString(R.string.actionCopyError_toast), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    protected void onSaveInstanceState( Bundle bundle )
    {
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState( Bundle bundle )
    {
        super.onRestoreInstanceState(bundle);
    }

}
