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
package com.forrestguice.suntimeswidget.calendar.ui.reminders;

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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.views.ViewUtils;

public class ReminderDialog extends BottomSheetDialogFragment
{
    protected TextView text_title;
    protected RecyclerView card_view;
    protected CardLayoutManager card_layout;
    protected ReminderViewAdapter card_adapter;
    protected ImageButton button_accept;

    public ReminderDialog() {
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
        return getArguments().getBoolean(KEY_MODIFIED);
    }
    public void setModified(boolean value) {
        getArguments().putBoolean(KEY_MODIFIED, value);
        if (isAdded())
        {
            if (button_accept != null) {
                button_accept.setVisibility(value ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }
    public static final String KEY_MODIFIED = "modified";

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
        View dialogContent = inflater.cloneInContext(getActivity()).inflate(R.layout.layout_dialog_reminders, parent, true);
        initViews(getActivity(), dialogContent);

        //if (savedState != null) {
        //}
        return dialogContent;
    }

    protected void initViews(Context context, View dialogContent)
    {
        text_title = (TextView) dialogContent.findViewById(R.id.text_title);

        card_layout = new CardLayoutManager(context);
        card_layout.setStackFromEnd(true);
        card_layout.setReverseLayout(true);

        card_view = (RecyclerView) dialogContent.findViewById(R.id.remindersView);
        card_view.setHasFixedSize(true);
        card_view.setLayoutManager(card_layout);
        initAdapter(context);

        TextView button_add = (TextView) dialogContent.findViewById(R.id.text_add_reminder);
        if (button_add != null) {
            button_add.setOnClickListener(onAddButtonClicked);
        }

        button_accept = (ImageButton) dialogContent.findViewById(R.id.accept_button);
        if (button_accept != null) {
            button_accept.setOnClickListener(onAcceptButtonClicked);
        }
    }

    protected void initAdapter(Context context)
    {
        card_adapter = new ReminderViewAdapter(context, getCalendar());
        card_adapter.setAdapterListener(card_adapterListener);
        card_view.setAdapter(card_adapter);
    }

    protected void updateViews(Context context)
    {
        String calendar = getCalendar();
        if (calendar != null)
        {
            SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
            if (text_title != null) {
                text_title.setText(descriptor.calendarTitle());
            }

        } else {
            if (text_title != null) {
                text_title.setText("");
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
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
                //behavior.setHideable(false);
                //behavior.setSkipCollapsed(false);
                //behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle out ) {
        super.onSaveInstanceState(out);
    }

    protected ReminderViewAdapter.AdapterListener card_adapterListener = new ReminderViewAdapter.AdapterListener() {
        @Override
        public void onItemChanged(int position, int minutes, int method)
        {
            SuntimesCalendarSettings.savePrefCalendarReminder(getActivity(), getCalendar(), position, minutes, method);
            setModified(true);
            if (dialogListener != null) {
                dialogListener.onModifiedReminder(getCalendar(), position, minutes, method);
            }
        }

        @Override
        public void onItemDelete(int position) {
            if (SuntimesCalendarSettings.removeCalendarReminder(getActivity(), getCalendar(), position))
            {
                setModified(true);
                initAdapter(getActivity());
                if (dialogListener != null) {
                    dialogListener.onRemovedReminder(getCalendar(), position);
                }
            }
        }
    };

    private final View.OnClickListener onAcceptButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();    // triggers save
        }
    });

    private final View.OnClickListener onAddButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addReminder();
        }
    });

    protected void addReminder()
    {
        Context context = getActivity();
        String calendar = getCalendar();
        int n = SuntimesCalendarSettings.loadPrefCalendarReminderCount(context, calendar);
        int minutes = SuntimesCalendarSettings.defaultCalendarReminderMinutes(context, calendar, n);
        int method = SuntimesCalendarSettings.defaultCalendarReminderMethod(context, calendar, n);
        if (SuntimesCalendarSettings.addCalendarReminder(context, calendar, minutes, method))
        {
            setModified(true);
            initAdapter(getActivity());
            if (dialogListener != null) {
                dialogListener.onAddedReminder(getCalendar(), n, minutes, method);
            }
        }
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

    @Override
    public void onDismiss( DialogInterface dialog )
    {
        super.onDismiss(dialog);
        if (dialogListener != null) {
            dialogListener.onDialogDismissed(getCalendar(), isModified());
        }
    }

    /**
     * DialogListener
     */
    public static abstract class DialogListener
    {
        public void onAddedReminder(String calendar, int reminderNum, int minutes, int method) {}
        public void onModifiedReminder(String calendar, int reminderNum, int minutes, int method) {}
        public void onRemovedReminder(String calendar, int reminderNum) {}
        public void onDialogDismissed(String calendar, boolean modified) {}
    }
    public DialogListener dialogListener = null;
    public void setDialogListener( DialogListener listener )
    {
        this.dialogListener = listener;
    }

}
