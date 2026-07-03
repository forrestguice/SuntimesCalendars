/*
    Copyright (C) 2024 Forrest Guice
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.CalendarEventTemplate;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettingsFactory;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.ui.HelpDialog;
import com.forrestguice.suntimeswidget.calendar.ui.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LATITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LOCATION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE;

public class TemplatePreviewDialog extends BottomSheetDialogFragment
{
    public static final String DIALOGTAG_HELP = "TemplateDialog_Help";

    protected TextView text_dialog_title;
    protected RecyclerView card_view;
    protected CardLayoutManager card_layout;
    protected TemplatePreviewAdapter card_adapter;
    protected ProgressBar progress;

    public TemplatePreviewDialog() {
        setArguments(new Bundle());
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
     * setLocation
     */
    private void setLocation(String[] value) {
        getArguments().putStringArray(KEY_LOCATION, value);
    }
    public String[] getLocation() {
        return getArguments().getStringArray(KEY_LOCATION);
    }
    public static final String KEY_LOCATION = "location";

    /**
     * setShowAddButton
     */
    public void setShowAddButton(boolean value) {
        getArguments().putBoolean(KEY_SHOW_ADD_BUTTON, value);
    }
    public boolean showAddButton() {
        return getArguments().getBoolean(KEY_SHOW_ADD_BUTTON, false);
    }
    public static final String KEY_SHOW_ADD_BUTTON = "show_add_button";

    /**
     * setShowMenuButton
     */
    public void setShowMenuButton(boolean value) {
        getArguments().putBoolean(KEY_SHOW_MENU_BUTTON, value);
    }
    public boolean showMenuButton() {
        return getArguments().getBoolean(KEY_SHOW_MENU_BUTTON, false);
    }
    public static final String KEY_SHOW_MENU_BUTTON = "show_menu_button";

    /**
     * setShowSubtitle
     */
    public void setShowSubtitle(boolean value) {
        getArguments().putBoolean(KEY_SHOW_SUBTITLE, value);
    }
    public boolean showSubtitle() {
        return getArguments().getBoolean(KEY_SHOW_SUBTITLE, true);
    }
    public static final String KEY_SHOW_SUBTITLE = "show_subtitle";

    /**
     * setShowHelpButton
     */
    public void setShowHelpButton(boolean value) {
        getArguments().putBoolean(KEY_SHOW_HELP_BUTTON, value);
    }
    public boolean showHelpButton() {
        return getArguments().getBoolean(KEY_SHOW_HELP_BUTTON, true);
    }
    public static final String KEY_SHOW_HELP_BUTTON = "show_help_button";

    /**
     * getSettings
     */
    public SuntimesCalendarSettings getSettings() {
        return ((settings != null) ? settings : SuntimesCalendarSettingsFactory.createSettings());
    }
    public void setSettings(SuntimesCalendarSettings settings) {
        this.settings = settings;
    }
    protected SuntimesCalendarSettings settings = null;

    /**
     * queryLocation
     */
    protected String[] queryLocation(Context context)
    {
        ContentResolver resolver = (context == null ? null : context.getContentResolver());
        if (resolver != null)
        {
            Uri configUri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG);
            String[] configProjection = new String[] { COLUMN_CONFIG_LOCATION, COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE };

            try {
                Cursor configCursor = resolver.query(configUri, configProjection, null, null, null);
                if (configCursor != null)
                {
                    String location_name = null, location_latitude = null, location_longitude = null, location_altitude = null;
                    configCursor.moveToFirst();
                    for (int i = 0; i < configProjection.length; i++) {
                        location_name = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_LOCATION));
                        location_latitude = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_LATITUDE));
                        location_longitude = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_LONGITUDE));
                        location_altitude = configCursor.getString(configCursor.getColumnIndexOrThrow(COLUMN_CONFIG_ALTITUDE));
                    }
                    configCursor.close();
                    return new String[] { location_name, location_latitude, location_longitude, location_altitude };

                } else {
                    Log.e(getClass().getSimpleName(), "null cursor!");
                }
            } catch (SecurityException e) {
                Log.e(getClass().getSimpleName(), "Permission Denied! " + configUri);
            }
        } else {
            Log.e(getClass().getSimpleName(), "Unable to getContentResolver! ");
        }
        return new String[] {"", "", "", ""};
    }

    /**
     * setTemplate
     */
    public void setTemplate(@Nullable CalendarEventTemplate template) {
        data = template;
        getArguments().putParcelable(KEY_DATA, template);
    }
    @Nullable
    public CalendarEventTemplate getTemplate(Context context) {
        if (data == null)
        {
            data = getArguments().getParcelable(KEY_DATA);
            if (data == null)
            {
                SuntimesCalendar calendarObj = new SuntimesCalendarFactory().createCalendar(context, SuntimesCalendarDescriptor.getDescriptor(context, getCalendar()), getSettings());
                setTemplate(getSettings().loadPrefCalendarTemplate(context, getCalendar(), calendarObj.defaultTemplate()));
            }
        }
        return data;
    }
    protected CalendarEventTemplate data = null;
    public static final String KEY_DATA = "data";

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

    /**
     * showProgress
     */
    public boolean showProgress() {
        return getArguments().getBoolean(KEY_SHOWPROGRESS, false);
    }
    public void setShowProgress(boolean value) {
        getArguments().putBoolean(KEY_SHOWPROGRESS, value);
        Log.d("DEBUG", "setShowProgress: " + value);
        if (isAdded()) {
            updateViews(getActivity());
        }
    }
    public static final String KEY_SHOWPROGRESS = "showProgress";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        //int themeResID = getTheme();
        //@SuppressLint("RestrictedApi") ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), themeResID);    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(getActivity()).inflate(R.layout.layout_dialog_template_preview, parent, true);

        setLocation(queryLocation(getActivity()));

        initViews(getActivity(), dialogContent);
        //if (savedState != null) {
        //}
        return dialogContent;
    }

    protected void initViews(Context context, View dialogContent)
    {
        text_dialog_title = (TextView) dialogContent.findViewById(R.id.text_title);

        TextView text_dialog_subtitle = (TextView) dialogContent.findViewById(R.id.text_subtitle);
        if (text_dialog_subtitle != null) {
            text_dialog_subtitle.setVisibility(showSubtitle() ? View.VISIBLE : View.GONE);
        }

        card_layout = new CardLayoutManager(context);
        card_view = (RecyclerView) dialogContent.findViewById(R.id.previewArea);
        if (card_view != null)
        {
            card_view.setHasFixedSize(true);
            card_view.setLayoutManager(card_layout);
            initAdapter(context);
        }

        progress = (ProgressBar) dialogContent.findViewById(R.id.progress0);

        ImageButton accept_button = (ImageButton) dialogContent.findViewById(R.id.accept_button);
        if (accept_button != null) {
            TooltipCompat.setTooltipText(accept_button, accept_button.getContentDescription());
            accept_button.setOnClickListener(onCancelButtonClicked);
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
            help_button.setVisibility(showHelpButton() ? View.VISIBLE : View.GONE);
        }

        /**ImageButton add_button = (ImageButton) dialogContent.findViewById(R.id.add_button);
        if (add_button != null) {
            TooltipCompat.setTooltipText(add_button, add_button.getContentDescription());
            add_button.setOnClickListener(onAddButtonClicked);
            add_button.setVisibility(showAddButton() ? View.VISIBLE : View.GONE);
        }*/

        CheckBox add_check = (CheckBox) dialogContent.findViewById(R.id.add_check);
        if (add_check != null)
        {
            TooltipCompat.setTooltipText(add_check, add_check.getContentDescription());
            add_check.setChecked(SuntimesCalendarSettings.loadPrefCalendarEnabled(context, getCalendar()));
            add_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                public void onCheckedChanged(final CompoundButton compoundButton, boolean b)
                {
                    compoundButton.postDelayed(new Runnable() {
                        public void run() {
                            onAddButtonClicked.onClick(compoundButton);
                        }
                    }, 500);
                }
            });
            add_check.setVisibility(showAddButton() ? View.VISIBLE : View.GONE);
        }

        ImageButton menu_button = (ImageButton) dialogContent.findViewById(R.id.menu_button);
        if (menu_button != null) {
            TooltipCompat.setTooltipText(menu_button, menu_button.getContentDescription());
            menu_button.setOnClickListener(onMenuButtonClicked);
            menu_button.setVisibility(showMenuButton() ? View.VISIBLE : View.GONE);
        }
    }

    protected void initAdapter(Context context)
    {
        if (card_view != null) {
            card_view.setAdapter(card_adapter = new TemplatePreviewAdapter(context, getCalendar(), getSettings(), getTemplate(context), getLocation()));
            card_adapter.setAdapterListener(new TemplatePreviewAdapter.AdapterListener()
            {
                @Override
                public void onStartLoading() {
                    setShowProgress(true);
                }

                @Override
                public void onLoaded() {
                    setShowProgress(false);
                }
            });
            card_adapter.initData(getActivity());
        }
    }

    protected void updateViews(Context context)
    {
        if (progress != null) {
            progress.setVisibility(showProgress() ? View.VISIBLE : View.GONE);
        }

        String calendar = getCalendar();
        if (calendar != null)
        {
            SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
            if (text_dialog_title != null) {
                text_dialog_title.setText(descriptor != null ? descriptor.calendarTitle() : "");
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

        FragmentManager fragments = getChildFragmentManager();
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
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (layout != null) {
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    protected View.OnClickListener onCancelButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (dialogListener != null) {
                dialogListener.onDialogDismissed(TemplatePreviewDialog.this);
            }
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    });

    private final View.OnClickListener onMenuButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOverflowMenu(getActivity(), v);
        }
    });

    protected void showOverflowMenu(final Context context, View v)
    {
        PopupMenu popup = PopupMenuCompat.createMenu(context, v, R.menu.menu_preview, new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (item.getItemId() == R.id.action_help)
                {
                    showHelp();
                    return true;

                } else if (item.getItemId() == R.id.action_add_calendar) {
                    if (dialogListener != null) {
                        dialogListener.onRequestAdd(TemplatePreviewDialog.this);
                    }

                } else if (item.getItemId() == R.id.action_save) {
                    if (dialogListener != null) {
                        dialogListener.onRequestSave(TemplatePreviewDialog.this);
                    }
                    return true;
                }
                return false;
            }
        });

        Menu menu = popup.getMenu();
        MenuItem add_item = menu.findItem(R.id.action_add_calendar);
        if (add_item != null) {
            add_item.setChecked(SuntimesCalendarSettings.loadPrefCalendarEnabled(context, getCalendar()));
        }

        popup.show();
    }

    private final View.OnClickListener onAddButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (dialogListener != null) {
                dialogListener.onRequestAdd(TemplatePreviewDialog.this);
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
        helpDialog.setShowDefaultsButton(false);
        helpDialog.setContent(getString(R.string.help_template));
        helpDialog.setDialogListener(helpDialogListener);
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }
    private final HelpDialog.DialogListener helpDialogListener = new HelpDialog.DialogListener() {
        @Override
        public void onRestoreDefaultsClicked(HelpDialog dialog) { /* EMPTY */ }
    };

    @Override
    public void onSaveInstanceState( @NonNull Bundle out ) {
        super.onSaveInstanceState(out);
    }

    @Override
    public void onDismiss( @NonNull DialogInterface dialog ) {
        super.onDismiss(dialog);
    }

    /**
     * DialogListener
     */
    public static abstract class DialogListener {
        public void onRequestAdd(TemplatePreviewDialog dialog) {}
        public void onRequestSave(TemplatePreviewDialog dialog) {}
        public void onDialogDismissed(TemplatePreviewDialog dialog) {}
    }
    public DialogListener dialogListener = null;
    public void setDialogListener( DialogListener listener )
    {
        this.dialogListener = listener;
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
            setOrientation(LinearLayoutManager.HORIZONTAL);
            setItemPrefetchEnabled(true);
        }
    }

}
