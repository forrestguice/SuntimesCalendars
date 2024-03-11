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
package com.forrestguice.suntimeswidget.calendar.ui.template;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.forrestguice.suntimeswidget.views.TooltipCompat;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.CalendarEventFlags;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.ui.HelpDialog;
import com.forrestguice.suntimeswidget.views.Toast;
import com.forrestguice.suntimeswidget.views.ViewUtils;

public class EventFlagsDialog extends BottomSheetDialogFragment
{
    public static final String DIALOGTAG_STRINGS = "FlagsDialog_Strings";
    public static final String DIALOGTAG_HELP = "FlagsDialog_Help";

    protected TextView text_dialog_title;
    protected RecyclerView card_view;
    protected EventFlagsDialog.CardLayoutManager card_layout;
    protected EventFlagsAdapter card_adapter;

    public EventFlagsDialog() {
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
     * setData
     */
    public void setData(@Nullable CalendarEventFlags events) {
        data = events;
        getArguments().putParcelable(KEY_DATA, events);
    }
    @Nullable
    public CalendarEventFlags getData() {
        if (data == null) {
            data = getArguments().getParcelable(KEY_DATA);
        }
        return data;
    }
    protected CalendarEventFlags data = null;
    public static final String KEY_DATA = "data";

    /**
     * getResult
     */
    public CalendarEventFlags getResult() {
        return new CalendarEventFlags(data);
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
        View dialogContent = inflater.cloneInContext(getActivity()).inflate(R.layout.layout_dialog_flags, parent, true);
        initViews(getActivity(), dialogContent);

        //if (savedState != null) {
        //}
        return dialogContent;
    }

    protected void initViews(Context context, View dialogContent)
    {
        text_dialog_title = (TextView) dialogContent.findViewById(R.id.text_title);

        card_layout = new CardLayoutManager(context);

        card_view = (RecyclerView) dialogContent.findViewById(R.id.eventsView);
        card_view.setHasFixedSize(true);
        card_view.setLayoutManager(card_layout);
        initAdapter(context);

        Button strings_button = (Button) dialogContent.findViewById(R.id.strings_button);
        if (strings_button != null) {
            TooltipCompat.setTooltipText(strings_button, strings_button.getContentDescription());
            strings_button.setOnClickListener(onStringsButtonClicked);
        }

        ImageButton accept_button = (ImageButton) dialogContent.findViewById(R.id.accept_button);
        if (accept_button != null) {
            TooltipCompat.setTooltipText(accept_button, accept_button.getContentDescription());
            accept_button.setOnClickListener(onAcceptButtonClicked);
        }

        ImageButton cancel_button = (ImageButton) dialogContent.findViewById(R.id.back_button);
        if (cancel_button != null) {
            TooltipCompat.setTooltipText(cancel_button, cancel_button.getContentDescription());
            cancel_button.setOnClickListener(onCancelButtonClicked);
        }

        ImageButton help_button = (ImageButton) dialogContent.findViewById(R.id.help_button);
        if (help_button != null) {
            TooltipCompat.setTooltipText(help_button, help_button.getContentDescription());
            help_button.setOnClickListener(onHelpButtonClicked);
        }
    }

    protected void initAdapter(Context context)
    {
        card_adapter = new EventFlagsAdapter(context, getCalendar(), getData());
        card_adapter.setAdapterListener(card_adapterListener);
        card_view.setAdapter(card_adapter);
    }

    protected EventFlagsAdapter.AdapterListener card_adapterListener = new EventFlagsAdapter.AdapterListener()
    {
        @Override
        public void onItemChanged(int position, boolean value)
        {
            CalendarEventFlags data = getData();
            if (data != null)
            {
                data.setValue(position, value);
                setModified(true);
                //Toast.makeText(getActivity(), "saved " + value, Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void updateViews(Context context)
    {
        String calendar = getCalendar();
        if (calendar != null)
        {
            SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
            if (text_dialog_title != null) {
                text_dialog_title.setText(descriptor.calendarTitle());
            }

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

        EventStringsDialog stringsDialog = (EventStringsDialog) fragments.findFragmentByTag(DIALOGTAG_STRINGS);
        if (stringsDialog != null) {
            stringsDialog.setDialogListener(stringsDialogListener);
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
                ViewUtils.disableTouchOutsideBehavior(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    protected View.OnClickListener onAcceptButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (dialogListener != null) {
                dialogListener.onDialogAccepted(EventFlagsDialog.this);
            }
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    });

    protected View.OnClickListener onCancelButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (dialogListener != null) {
                dialogListener.onDialogCanceled(EventFlagsDialog.this);
            }
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    });

    private final View.OnClickListener onHelpButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    });

    protected void showHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setShowDefaultsButton(true);
        helpDialog.setContent(getString(R.string.help_flags));
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
            SuntimesCalendarSettings.clearPrefCalendarFlags(context, getCalendar());
            setData(calendarObj.defaultFlags());
            setModified(true);

            dialog.dismiss();
            updateViews(context);
            Toast.makeText(getActivity(), getString(R.string.flag_dialog_defaults_toast), Toast.LENGTH_SHORT).show();

            int animDelay = getResources().getInteger(android.R.integer.config_longAnimTime);
            text_dialog_title.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, animDelay);
        }
    };


    protected View.OnClickListener onStringsButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showStringsDialog();
        }
    });

    protected void showStringsDialog()
    {
        Context context = getActivity();
        String calendar = getCalendar();
        SuntimesCalendar calendarObj = new SuntimesCalendarFactory().createCalendar(context, SuntimesCalendarDescriptor.getDescriptor(context, calendar));

        EventStringsDialog dialog = new EventStringsDialog();
        dialog.setCalendar(getCalendar());
        dialog.setData(SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendar, calendarObj.defaultStrings()));
        dialog.setDialogListener(stringsDialogListener);
        dialog.show(getChildFragmentManager(), DIALOGTAG_STRINGS);
    }

    private final EventStringsDialog.DialogListener stringsDialogListener = new EventStringsDialog.DialogListener()
    {
        @Override
        public void onDialogAccepted(EventStringsDialog dialog)
        {
            Context context = getActivity();
            if (context != null) {
                SuntimesCalendarSettings.savePrefCalendarStrings(context, getCalendar(), dialog.getResult());
                Toast.makeText(context, context.getString(R.string.templatestrings_dialog_saved_toast), Toast.LENGTH_SHORT).show();
            }
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
     * CardLayoutManager
     */
    public static class CardLayoutManager extends LinearLayoutManager
    {
        public CardLayoutManager(Context context) {
            super(context);
            init(context);
        }

        public CardLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
            init(context);
        }

        public CardLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
        {
            super(context, attrs, defStyleAttr, defStyleRes);
            init(context);
        }

        private void init(Context context)
        {
            setOrientation(LinearLayoutManager.VERTICAL);
            setItemPrefetchEnabled(true);
        }
    }

    /**
     * DialogListener
     */
    public static abstract class DialogListener
    {
        public void onDialogAccepted(EventFlagsDialog dialog) {}
        public void onDialogCanceled(EventFlagsDialog dialog) {}
    }
    public DialogListener dialogListener = null;
    public void setDialogListener( DialogListener listener )
    {
        this.dialogListener = listener;
    }

}
