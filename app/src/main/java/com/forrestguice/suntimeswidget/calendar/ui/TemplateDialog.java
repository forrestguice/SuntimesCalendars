/*
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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.TemplatePatterns;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.views.Toast;

public class TemplateDialog extends BottomSheetDialogFragment
{
    public static final String DIALOGTAG_HELP = "TemplateDialog_Help";

    protected TextView text_dialog_title;
    protected EditText edit_title, edit_desc;

    public TemplateDialog() {
        setArguments(new Bundle());
        setModified(false);
    }

    /**
     * setCalendar
     */
    public void setCalendar(String calendar) {
        getArguments().putString(KEY_CALENDAR, calendar);
    }
    public String getCalendar() {
        return getArguments().getString(KEY_CALENDAR);
    }
    public static final String KEY_CALENDAR = "calendar";

    /**
     * isModified
     */
    public boolean isModified() {
        return getArguments().getBoolean(KEY_MODIFIED, false);
    }
    public void setModified(boolean value) {
        getArguments().putBoolean(KEY_MODIFIED, value);
    }
    public static final String KEY_MODIFIED = "ismodified";

    /**
     * setTemplate
     */
    public void setTemplate(@Nullable CalendarEventTemplate template) {
        data = template;
        getArguments().putParcelable(KEY_DATA, template);
    }
    @Nullable
    public CalendarEventTemplate getTemplate() {
        if (data == null) {
            data = getArguments().getParcelable(KEY_DATA);
        }
        return data;
    }
    protected CalendarEventTemplate data = null;
    public static final String KEY_DATA = "data";

    /**
     * getResult
     */
    public CalendarEventTemplate getResult() {
        return new CalendarEventTemplate(edit_title.getText().toString(), edit_desc.getText().toString());
    }

    /**
     * setTheme
     */
    public void setThemeOverride(int themeResID) {
        getArguments().putInt(KEY_DIALOGTHEME, themeResID);
    }
    public int getThemeOverride() {
        return getArguments().getInt(KEY_DIALOGTHEME, R.style.AppTheme_Dark);
    }
    public static final String KEY_DIALOGTHEME = "themeResID";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(getContext(), getTheme());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        //int themeResID = getTheme();
        //@SuppressLint("RestrictedApi") ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), themeResID);    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(getActivity()).inflate(R.layout.layout_dialog_template, parent, true);  // TODO
        initViews(getActivity(), dialogContent);

        //if (savedState != null) {
        //}
        return dialogContent;
    }

    protected void initViews(Context context, View dialogContent)
    {
        text_dialog_title = (TextView) dialogContent.findViewById(R.id.text_title);
        edit_title = (EditText) dialogContent.findViewById(R.id.edit_title);
        edit_desc = (EditText) dialogContent.findViewById(R.id.edit_desc);
        setTextWatchers();

        ImageButton accept_button = (ImageButton) dialogContent.findViewById(R.id.accept_button);
        if (accept_button != null) {
            accept_button.setOnClickListener(onAcceptButtonClicked);
        }

        ImageButton cancel_button = (ImageButton) dialogContent.findViewById(R.id.back_button);
        if (cancel_button != null) {
            cancel_button.setOnClickListener(onCancelButtonClicked);
        }

        ImageButton help_button = (ImageButton) dialogContent.findViewById(R.id.help_button);
        if (help_button != null) {
            help_button.setOnClickListener(onHelpButtonClicked);
        }
    }

    private final TextWatcher edit_titleListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s)
        {
            CalendarEventTemplate data = getTemplate();
            if (data != null) {
                data.setTitle(s.toString());
                setTemplate(data);
            }
            setModified(true);
        }
    };

    private final TextWatcher edit_descListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s)
        {
            CalendarEventTemplate data = getTemplate();
            if (data != null) {
                data.setDesc(s.toString());
                setTemplate(data);
            }
            setModified(true);
        }
    };

    protected void setTextWatchers()
    {
        edit_title.addTextChangedListener(edit_titleListener);
        edit_desc.addTextChangedListener(edit_descListener);
    }
    protected void clearTextWatchers()
    {
        edit_title.removeTextChangedListener(edit_titleListener);
        edit_desc.removeTextChangedListener(edit_descListener);
    }

    protected void updateViews(Context context)
    {
        String calendar = getCalendar();
        if (calendar != null)
        {
            SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
            if (text_dialog_title != null) {
                text_dialog_title.setText(descriptor.calendarTitle());
            }

            clearTextWatchers();
            CalendarEventTemplate data = getTemplate();
            if (data != null)
            {
                edit_title.setText(data.getTitle());
                edit_desc.setText(data.getDesc());

            } else {
                edit_title.setText("");
                edit_desc.setText("");
            }
            setTextWatchers();

        } else {
            if (text_dialog_title != null) {
                text_dialog_title.setText("");
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        android.support.v4.app.FragmentManager fragments = getChildFragmentManager();
        HelpDialog helpDialog = (HelpDialog) fragments.findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setDialogListener(helpDialogListener);
        }

        updateViews(getActivity());
        expandSheet(getDialog());
    }

    private static void expandSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    protected View.OnClickListener onAcceptButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (dialogListener != null) {
                dialogListener.onDialogAccepted(TemplateDialog.this);
            }
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    };

    protected View.OnClickListener onCancelButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (dialogListener != null) {
                dialogListener.onDialogCanceled(TemplateDialog.this);
            }
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    };

    private final View.OnClickListener onHelpButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    protected void showHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setShowDefaultsButton(true);
        helpDialog.setContent(getString(R.string.help_template, TemplatePatterns.getAllHelpText(getActivity())) + "<br/>");
        helpDialog.setDialogListener(helpDialogListener);
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }

    private final HelpDialog.DialogListener helpDialogListener = new HelpDialog.DialogListener()
    {
        @Override
        public void onRestoreDefaultsClicked(HelpDialog dialog)
        {
            Context context = getActivity();
            SuntimesCalendar calendarObj = new SuntimesCalendarFactory().createCalendar(context, SuntimesCalendarDescriptor.getDescriptor(context, getCalendar()));
            setTemplate(calendarObj.defaultTemplate());
            setModified(true);

            dialog.dismiss();
            updateViews(context);
            Toast.makeText(getActivity(), getString(R.string.template_dialog_defaults_toast), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onSaveInstanceState( @NonNull Bundle out ) {
        super.onSaveInstanceState(out);
    }

    @Override
    public void onDismiss( DialogInterface dialog ) {
        super.onDismiss(dialog);
    }

    /**
     * DialogListener
     */
    public static abstract class DialogListener
    {
        public void onDialogAccepted(TemplateDialog dialog) {}
        public void onDialogCanceled(TemplateDialog dialog) {}
    }
    public DialogListener dialogListener = null;
    public void setDialogListener( DialogListener listener )
    {
        this.dialogListener = listener;
    }

}
