/*
    Copyright (C) 2018-2022 Forrest Guice
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;

import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskBase;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskItem;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskListener;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendarTaskService;
import com.forrestguice.suntimeswidget.calendar.ui.AboutDialog;
import com.forrestguice.suntimeswidget.calendar.ui.reminders.ReminderDialog;
import com.forrestguice.suntimeswidget.calendar.ui.ColorDialog;
import com.forrestguice.suntimeswidget.calendar.ui.HelpDialog;
import com.forrestguice.suntimeswidget.calendar.ui.PopupMenuCompat;
import com.forrestguice.suntimeswidget.calendar.ui.ProgressDialog;
import com.forrestguice.suntimeswidget.calendar.ui.SuntimesCalendarPreference;
import com.forrestguice.suntimeswidget.calendar.ui.Utils;
import com.forrestguice.suntimeswidget.calendar.ui.template.TemplateDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * SuntimesCalendarActivity
 */
public class SuntimesCalendarActivity extends AppCompatActivity
{
    public static String TAG = "SuntimesCalendar";

    public static final String DIALOGTAG_ABOUT = "aboutdialog";
    public static final String DIALOGTAG_HELP = "helpdialog";
    public static final String DIALOGTAG_PROGRESS = "progressdialog";

    public static final int MIN_PROVIDER_VERSION = 1;
    public static final String MIN_SUNTIMES_VERSION = "0.10.3";
    public static final String MIN_SUNTIMES_VERSION_STRING = "Suntimes v" + MIN_SUNTIMES_VERSION;

    public static final int REQUEST_CALENDAR_ENABLED = 12;           // individual calendar enabled/disabled
    public static final int REQUEST_CALENDAR_DISABLED = 14;

    public static final int REQUEST_CALENDARS_ENABLED = 2;          // all calendars enabled/disabled
    public static final int REQUEST_CALENDARS_DISABLED = 4;

    public static final int REQUEST_CALENDAR_FIRSTLAUNCH = 0;

    private Context context;
    private String config_apptheme = null;
    private String config_appThemeOverride = null;
    private String config_textSize = null;
    private static String systemLocale = null;  // null until locale is overridden w/ loadLocale

    private static String appVersionName = null, providerVersionName = null;
    private static Integer appVersionCode = null, providerVersionCode = null;
    private static boolean needsSuntimesPermissions = false;
    protected static String locale = null;

    private CalendarPrefsFragment mainFragment = null;
    private FirstLaunchFragment firstLaunchFragment = null;

    public SuntimesCalendarActivity()
    {
        super();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void attachBaseContext(Context newBase)
    {
        ContentResolver resolver = newBase.getContentResolver();
        if (resolver != null)
        {
            Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG );
            String[] projection = new String[] { CalculatorProviderContract.COLUMN_CONFIG_LOCALE, CalculatorProviderContract.COLUMN_CONFIG_APP_THEME,
                                                 CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION, CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION_CODE,
                                                 CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION, CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE,
                                                 CalculatorProviderContract.COLUMN_CONFIG_APP_THEME_OVERRIDE, CalculatorProviderContract.COLUMN_CONFIG_APP_TEXT_SIZE };
            try {
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                needsSuntimesPermissions = false;

                if (cursor != null)
                {
                    // a valid cursor - Suntimes is installed (and we have access)
                    cursor.moveToFirst();
                    if (locale == null) {
                        locale = (!cursor.isNull(0)) ? cursor.getString(0) : null;
                    }
                    config_apptheme = (!cursor.isNull(1)) ? cursor.getString(1) : null;
                    appVersionName = (!cursor.isNull(2)) ? cursor.getString(2) : null;
                    appVersionCode = (!cursor.isNull(3)) ? cursor.getInt(3) : null;
                    providerVersionName = (!cursor.isNull(4)) ? cursor.getString(4) : null;
                    providerVersionCode = (!cursor.isNull(5)) ? cursor.getInt(5) : null;
                    config_appThemeOverride = (!cursor.isNull(6)) ? cursor.getString(6) : null;
                    config_textSize = (!cursor.isNull(7)) ? cursor.getString(7) : null;
                    cursor.close();
                    super.attachBaseContext((locale != null) ? loadLocale(newBase, locale) : resetLocale(newBase));

                } else {
                    // cursor is null (but no SecurityException..) - Suntimes isn't installed at all
                    super.attachBaseContext(newBase);
                }

            } catch (SecurityException e) {
                // Security Exception! Suntimes is installed (but we don't have permissions for some reason)
                Log.e(TAG, "attachBaseContext: Unable to access SuntimesCalculatorProvider! " + e);
                appVersionName = MIN_SUNTIMES_VERSION;
                needsSuntimesPermissions = true;
                super.attachBaseContext(newBase);
            }
        } else super.attachBaseContext(newBase);
    }

    protected static Context loadLocale( Context context, String languageTag )
    {
        if (systemLocale == null) {
            systemLocale = Locale.getDefault().getLanguage();
        }

        Locale customLocale = localeForLanguageTag(languageTag);
        Locale.setDefault(customLocale);
        Log.i(TAG, "loadLocale: " + languageTag);

        Resources resources = context.getApplicationContext().getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= 17)
            config.setLocale(customLocale);
        else config.locale = customLocale;

        if (Build.VERSION.SDK_INT >= 25) {
            return new ContextWrapper(context.createConfigurationContext(config));

        } else {
            DisplayMetrics metrics = resources.getDisplayMetrics();
            //noinspection deprecation
            resources.updateConfiguration(config, metrics);
            return new ContextWrapper(context);
        }
    }

    protected static Context resetLocale( Context context )
    {
        if (systemLocale != null) {
            return loadLocale(context, systemLocale);
        }
        return context;
    }

    protected static @NonNull Locale localeForLanguageTag(@NonNull String languageTag)
    {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            locale = Locale.forLanguageTag(languageTag.replaceAll("_", "-"));

        } else {
            String[] parts = languageTag.split("[_]");
            String language = parts[0];
            String country = (parts.length >= 2) ? parts[1] : null;
            locale = (country != null) ? new Locale(language, country) : new Locale(language);
        }
        Log.d(TAG, "localeForLanguageTag: tag: " + languageTag + " :: locale: " + locale.toString());
        return locale;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static SuntimesCalendarTaskService calendarTaskService;
    boolean boundToTaskService = false;

    @Override
    protected void onStart()
    {
        super.onStart();
        bindService(new Intent(this, SuntimesCalendarTaskService.class),
                calendarSyncServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        calendarTaskService.removeCalendarServiceListener(serviceListener);
        unbindService(calendarSyncServiceConnection);
        boundToTaskService = false;
    }

    private ServiceConnection calendarSyncServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            SuntimesCalendarTaskService.SuntimesCalendarTaskServiceBinder binder = (SuntimesCalendarTaskService.SuntimesCalendarTaskServiceBinder) service;
            calendarTaskService = binder.getService();
            boundToTaskService = true;
            calendarTaskService.addCalendarServiceListener(serviceListener);

            if (mainFragment != null) {
                mainFragment.setIsBusy(calendarTaskService.isBusy());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundToTaskService = false;
        }
    };

    private SuntimesCalendarTaskService.SuntimesCalendarServiceListener serviceListener = new SuntimesCalendarTaskService.SuntimesCalendarServiceListener() {
        @Override
        public void onBusyStatusChanged(boolean isBusy)
        {
            if (mainFragment != null) {
                mainFragment.setIsBusy(isBusy);
            }
        }

        @Override
        public void onProgressMessage(int i, int n, String message)
        {
            //Log.d("DEBUG", "onProgressMessage: " + i + " of " + n);
            if (mainFragment != null) {
                mainFragment.updateProgressDialog(i, n, 0, n, message);
            }
        }

        @Override
        public void onProgressMessage(int i, int n, int j, int m, String message)
        {
            //Log.d("DEBUG", "onProgressMessage: " + i + " of " + n + " .. " + j + " of " + m);
            if (mainFragment != null) {
                mainFragment.updateProgressDialog(i, n, j, m, message);
            }
        }

    };

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle icicle)
    {
        setResult(RESULT_OK);
        context = this;

        String themeName = (config_appThemeOverride != null ? config_appThemeOverride : config_apptheme);
        if (themeName != null) {
            if (config_textSize != null) {
                themeName += "_" + config_textSize;
            }
            AppThemes.setTheme(this, themeName);
        }

        super.onCreate(icicle);
        setContentView(R.layout.layout_activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_suntimes_calendar);
        }

        if (SuntimesCalendarSettings.isFirstLaunch(context) && !hasCalendarPermissions(this)) {
            initFirstLaunchFragment();

        } else {
            initMainFragment();
        }
    }

    private void initFirstLaunchFragment()
    {
        firstLaunchFragment = new FirstLaunchFragment();
        firstLaunchFragment.setAboutClickListener(onAboutClick);
        firstLaunchFragment.setProviderVersion(providerVersionCode);
        firstLaunchFragment.setSupportFragmentManager(getSupportFragmentManager());
        getFragmentManager().beginTransaction().replace(R.id.content, firstLaunchFragment).commit();
    }

    private void initMainFragment()
    {
        mainFragment = new CalendarPrefsFragment();
        mainFragment.setAboutClickListener(onAboutClick);
        mainFragment.setProviderVersion(providerVersionCode);
        mainFragment.setSupportFragmentManager(getSupportFragmentManager());
        if (boundToTaskService) {
            mainFragment.setIsBusy(calendarTaskService.isBusy());
        }
        getFragmentManager().beginTransaction().replace(R.id.content, mainFragment).commit();
    }

    private static boolean hasCalendarPermissions(Activity activity)
    {
        int calendarPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR);
        return (calendarPermission == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (grantResults.length > 0 && permissions.length > 0)
        {
            switch (requestCode)
            {
                case REQUEST_CALENDAR_FIRSTLAUNCH:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        SuntimesCalendarSettings.saveFirstLaunch(this);
                        initMainFragment();
                    }
                    break;

                case REQUEST_CALENDAR_ENABLED:
                case REQUEST_CALENDAR_DISABLED:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        Intent taskIntent = new Intent(this, SuntimesCalendarSyncService.class);
                        taskIntent.setAction(SuntimesCalendarTaskService.ACTION_UPDATE_CALENDARS);

                        ArrayList<SuntimesCalendarTaskItem> items = loadItems(this.getIntent(), true);
                        savePendingItems(this, taskIntent, items);
                        calendarTaskService.runCalendarTask(context, taskIntent, false, false, mainFragment.calendarTaskListener);

                        if (mainFragment != null)
                        {
                            SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                            for (SuntimesCalendarTaskItem item : items)
                            {
                                boolean enabled = (item.getAction() == SuntimesCalendarTaskItem.ACTION_UPDATE);
                                pref.putBoolean(SuntimesCalendarSettings.PREF_KEY_CALENDARS_CALENDAR + item.getCalendar(), enabled);
                                pref.apply();

                                CheckBoxPreference calendarPref = mainFragment.getCalendarPref(item.getCalendar());
                                if (calendarPref != null) {
                                    calendarPref.setChecked(enabled);
                                }
                            }
                        }
                    }
                    break;

                case REQUEST_CALENDARS_ENABLED:
                case REQUEST_CALENDARS_DISABLED:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        boolean enabled = requestCode == (REQUEST_CALENDARS_ENABLED);
                        Intent taskIntent = new Intent(this, SuntimesCalendarSyncService.class);
                        taskIntent.setAction( !enabled ? SuntimesCalendarTaskService.ACTION_CLEAR_CALENDARS : SuntimesCalendarTaskService.ACTION_UPDATE_CALENDARS );

                        ArrayList<SuntimesCalendarTaskItem> items = new ArrayList<>();
                        if (enabled) {
                            items = loadItems(this.getIntent(), true);
                        }

                        savePendingItems(this, taskIntent, items);
                        calendarTaskService.runCalendarTask(context, taskIntent, !enabled, true, mainFragment.calendarTaskListener);

                        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        pref.putBoolean(SuntimesCalendarSettings.PREF_KEY_CALENDARS_ENABLED, enabled);
                        pref.apply();

                        if (mainFragment != null)
                        {
                            CheckBoxPreference calendarsPref = mainFragment.getCalendarsEnabledPref();
                            if (calendarsPref != null) {
                                calendarsPref.setChecked(enabled);
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * onSaveInstanceState
     * @param bundle outState
     */
    @Override
    protected void onSaveInstanceState( Bundle bundle )
    {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("needsSuntimesPermissions", needsSuntimesPermissions);
    }

    /**
     * onRestoreInstanceState
     * @param bundle inState
     */
    @Override
    protected void onRestoreInstanceState( Bundle bundle )
    {
        super.onRestoreInstanceState(bundle);
        needsSuntimesPermissions = bundle.getBoolean("needsSuntimesPermissions");
    }


    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_openCalendar:
                openCalendarApp(this);
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                //onBackPressed();
                onHomePressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onHomePressed()
    {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.forrestguice.suntimeswidget", "com.forrestguice.suntimeswidget.SuntimesActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(getClass().getSimpleName(), "Failed to start activity: " + e);
        }
    }

    protected static void openCalendarApp(@Nullable Activity context)
    {
        if (context != null) {
            try {
                Intent intent = SuntimesCalendarTaskService.getCalendarIntent();
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e("SuntimesCalendar", "Failed to start activity: " + e);
            }
        } else {
            Log.e("SuntimesCalendar", "Failed to start activity: null context");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CalendarPrefsFragmentBase
     */
    public static class CalendarPrefsFragmentBase extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null && savedInstanceState.containsKey("providerVersion")) {
                providerVersion = savedInstanceState.getInt("providerVersion");
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            super.onSaveInstanceState(outState);
            if (providerVersion != null) {
                outState.putInt("providerVersion", providerVersion);
            }
        }

        protected Integer providerVersion = null;
        public void setProviderVersion( Integer version )
        {
            providerVersion = version;
        }

        protected Preference.OnPreferenceClickListener onAboutClick = null;
        public void setAboutClickListener( Preference.OnPreferenceClickListener onClick )
        {
            onAboutClick = onClick;
        }

        protected boolean checkDependencies()
        {
            return (providerVersion != null && providerVersion >= MIN_PROVIDER_VERSION);
        }

        protected void showPermissionRational(final Activity activity, final int requestCode)
        {
            String permissionMessage = activity.getString(R.string.privacy_permission_calendar);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getString(R.string.privacy_permissiondialog_title))
                    .setMessage(Utils.fromHtml(permissionMessage))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_CALENDAR }, requestCode);
                        }
                    });
            builder.show();
        }

        protected void initAboutDialog()
        {
            Preference aboutPref = findPreference("app_about");
            if (aboutPref != null && onAboutClick != null) {
                aboutPref.setOnPreferenceClickListener(onAboutClick);
            }
        }

        protected ProgressDialog progressDialog;
        public void updateProgressDialog(int i, int n, int j, int m, String message)
        {
            if (progressDialog != null && progressDialog.isShowing())
            {
                if (n > 0)
                {
                    progressDialog.setMax(m);
                    progressDialog.setMessageSecondary(message);
                    if (n == 1) {
                        progressDialog.setProgress(j);
                        progressDialog.setSecondaryProgress(0);

                    } else {
                        int progress = (i * m) / n;
                        progressDialog.setProgress( progress == 0 ? 1 : progress );
                        progressDialog.setProgressSecondary(m);
                        progressDialog.setSecondaryProgress(j);
                    }

                } else {
                    progressDialog.setProgress(1);
                    progressDialog.setMax(1);
                    progressDialog.setSecondaryProgress(1);
                    progressDialog.setMessageSecondary("");
                }
            }
        }

        private android.support.v4.app.FragmentManager supportFragments;
        public void setSupportFragmentManager(android.support.v4.app.FragmentManager fragments)
        {
            supportFragments = fragments;
        }
        public android.support.v4.app.FragmentManager getSupportFragmentManager()
        {
            return supportFragments;
        }

        protected void initProgressDialog()
        {
            android.support.v4.app.FragmentManager fragments = getSupportFragmentManager();
            if (fragments != null)
            {
                ProgressDialog dialog = (ProgressDialog) fragments.findFragmentByTag(DIALOGTAG_PROGRESS);
                if (dialog != null) {
                    progressDialog = dialog;
                    progressDialog.setTitle(getString(R.string.progress_title));
                    progressDialog.setMessage(getString(R.string.progress_message));
                    progressDialog.setCancelable(false);
                    progressDialog.setOnCancelClickListener(onCancelClick);
                    return;
                }
            }

            progressDialog = new ProgressDialog();
            progressDialog.setTitle(getString(R.string.progress_title));
            progressDialog.setMessage(getString(R.string.progress_message));
            progressDialog.setCancelable(false);
            progressDialog.setOnCancelClickListener(onCancelClick);
        }

        private View.OnClickListener onCancelClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Context context = getActivity();
                AlertDialog.Builder confirmCancel = new AlertDialog.Builder(context);
                confirmCancel.setMessage(context.getString(R.string.confirm_cancel_message));
                confirmCancel.setNegativeButton(context.getString(R.string.confirm_cancel_no), null);
                confirmCancel.setPositiveButton(context.getString(R.string.confirm_cancel_yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        calendarTaskService.cancelRunningTask();
                    }
                });
                confirmCancel.show();
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * FirstLaunchFragment
     */
    public static class FirstLaunchFragment extends CalendarPrefsFragmentBase
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i(TAG, "FirstLaunchFragment: Arguments: " + getArguments());
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_firstlaunch, false);
            addPreferencesFromResource(R.xml.preference_firstlaunch);

            Preference aboutPref = findPreference("app_about");
            if (aboutPref != null && onAboutClick != null) {
                aboutPref.setOnPreferenceClickListener(onAboutClick);
            }

            CheckBoxPreference permissionsPref = (CheckBoxPreference) findPreference(SuntimesCalendarSettings.PREF_KEY_CALENDARS_PERMISSIONS);
            permissionsPref.setChecked(false);
            permissionsPref.setOnPreferenceChangeListener(onPermissionsPrefChanged);

            if (needsSuntimesPermissions || !checkDependencies())
            {
                if (needsSuntimesPermissions)
                    showPermissionDeniedMessage(getActivity(), getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
                else showMissingDepsMessage(getActivity(), getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
            }
        }

        Preference.OnPreferenceChangeListener onPermissionsPrefChanged = new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                Activity activity = getActivity();
                boolean checkPrefs = (Boolean)newValue;
                if (checkPrefs && activity != null)
                {
                    if (!hasCalendarPermissions(activity))
                    {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALENDAR)) {
                            showPermissionRational(activity, REQUEST_CALENDAR_FIRSTLAUNCH);
                        } else {
                            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_CALENDAR }, REQUEST_CALENDAR_FIRSTLAUNCH);
                        }

                    } else {
                        SuntimesCalendarSettings.saveFirstLaunch(activity);
                        activity.recreate();
                    }
                }
                return false;
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CalendarPrefsFragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CalendarPrefsFragment extends CalendarPrefsFragmentBase
    {
        private CheckBoxPreference calendarsEnabledPref = null;
        public CheckBoxPreference getCalendarsEnabledPref()
        {
            return calendarsEnabledPref;
        }

        private HashMap<String, SuntimesCalendarPreference> calendarPrefs = new HashMap<>();
        public SuntimesCalendarPreference getCalendarPref(String calendar)
        {
            return calendarPrefs.get(calendar);
        }

        private boolean isBusy = false;
        public void setIsBusy(boolean isBusy)
        {
            this.isBusy = isBusy;
            if (progressDialog != null)
            {
                if (isBusy)
                {
                    if (!progressDialog.isShowing()) {
                        android.support.v4.app.FragmentManager fragments = getSupportFragmentManager();
                        if (fragments != null) {
                            progressDialog.show(fragments, DIALOGTAG_PROGRESS);
                        }
                    }

                } else {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    clearPrefListeners();
                    updatePrefs(getActivity());
                    initPrefListeners(getActivity());
                }
            }
        }

        @Override
        public void onStart()
        {
            super.onStart();
            if (isBusy && progressDialog != null && !progressDialog.isShowing())
            {
                android.support.v4.app.FragmentManager fragments = getSupportFragmentManager();
                if (fragments != null) {
                    progressDialog.show(fragments, DIALOGTAG_PROGRESS);
                }
            }
        }

        @Override
        public void onResume()
        {
            super.onResume();

            android.support.v4.app.FragmentManager fragments = getSupportFragmentManager();
            for (String calendar : SuntimesCalendarDescriptor.getCalendars(getActivity()))    // restore dialog listeners
            {
                ReminderDialog reminderDialog = (ReminderDialog) fragments.findFragmentByTag(DIALOGTAG_REMINDER + "_" + calendar);
                if (reminderDialog != null) {
                    reminderDialog.setDialogListener(reminderDialog_listener);
                }

                TemplateDialog templateDialog = (TemplateDialog) fragments.findFragmentByTag(DIALOGTAG_TEMPLATE + "_" + calendar);
                if (templateDialog != null) {
                    templateDialog.setDialogListener(templateDialog_listener);
                }

                ColorDialog colorDialog = (ColorDialog) fragments.findFragmentByTag(DIALOGTAG_COLOR + "_" + calendar);
                if (colorDialog != null) {
                    colorDialog.setColorChangeListener(onColorChanged(calendar));
                }
            }
        }

        /**@Override
        public void onStop()
        {
            super.onStop();
        }*/


        @SuppressLint("ResourceType")
        private void initColors(Context context)
        {
            int[] colorAttrs = { R.attr.text_accentColor, R.attr.text_disabledColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            pressedColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.text_accent_dark));
            disabledColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_disabled_dark));
            typedArray.recycle();
        }
        private int pressedColor = Color.WHITE;
        private int disabledColor = Color.GRAY;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i(TAG, "CalendarPrefsFragment: Arguments: " + getArguments());
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_calendars, false);
            addPreferencesFromResource(R.xml.preference_calendars);

            final Activity activity = getActivity();
            initColors(activity);
            initAboutDialog();
            initProgressDialog();

            SuntimesCalendarSettings settings = new SuntimesCalendarSettings();
            PreferenceCategory category = (PreferenceCategory) findPreference("app_calendars");

            final Context context = getActivity();
            calendarsEnabledPref = (CheckBoxPreference) findPreference(SuntimesCalendarSettings.PREF_KEY_CALENDARS_ENABLED);
            for (final SuntimesCalendarDescriptor descriptor : SuntimesCalendarDescriptor.getDescriptors(context))
            {
                if (descriptor == null) {
                    continue;
                }
                final String calendar = descriptor.calendarName();
                SuntimesCalendarPreference calendarPref = new SuntimesCalendarPreference(context);
                calendarPref.setKey(SuntimesCalendarSettings.PREF_KEY_CALENDARS_CALENDAR + calendar);
                calendarPref.setTitle(descriptor.calendarTitle());
                calendarPref.setSummary(descriptor.calendarSummary());
                category.addPreference(calendarPref);

                int calendarColor = settings.loadPrefCalendarColor(context, calendar);
                calendarPref.setNoteFormat(R.string.summarylist_format);
                calendarPref.setNote(settings.loadCalendarNote(context, calendar, SuntimesCalendarSettings.NOTE_LOCATION_NAME));
                calendarPref.setIconColor(createColorStateList(calendarColor));
                calendarPref.setIcon(R.drawable.ic_action_calendar);
                calendarPref.setOnIconClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        showContextMenu(context, v, calendar);
                    }
                });
                calendarPrefs.put(calendar, calendarPref);
            }

            updatePrefs(activity);
            initPrefListeners(activity);

            if (needsSuntimesPermissions || !checkDependencies())
            {
                if (needsSuntimesPermissions)
                    showPermissionDeniedMessage(getActivity(), getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
                else showMissingDepsMessage(getActivity(), getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
            }
            setIsBusy(isBusy);
        }

        /**
         * showContextMenu
         * @param context
         * @param calendar
         */
        protected boolean showContextMenu(Context context, View v, String calendar)
        {
            PopupMenu menu = PopupMenuCompat.createMenu(context, v, R.menu.menu_context, onContextMenuClick(context, calendar));
            updateContextMenu(context, menu, calendar);
            menu.show();
            return true;
        }

        protected void updateContextMenu(Context context, PopupMenu menu, String calendar)
        {
            /*MenuItem template_item = menu.getMenu().findItem(R.id.action_template);
            if (template_item != null)
            {
                SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
                template_item.setEnabled(!descriptor.isAddon());
            }*/
        }

        protected PopupMenu.OnMenuItemClickListener onContextMenuClick(final Context context, final String calendar)
        {
            return new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    switch (item.getItemId())
                    {
                        case R.id.action_color:
                            showColorPicker(context, calendar);
                            return true;

                        case R.id.action_reminders:
                            showReminderDialog(context, calendar);
                            return true;

                        case R.id.action_template:
                            showTemplateDialog(context, calendar);
                            return true;

                        default:
                            return false;
                    }
                }
            };
        }


        /**
         * showTemplateDialog
         */

        private static final String DIALOGTAG_TEMPLATE = "configtemplate";
        protected void showTemplateDialog(Context context, String calendar)
        {
            SuntimesCalendar calendarObj = new SuntimesCalendarFactory().createCalendar(context, SuntimesCalendarDescriptor.getDescriptor(context, calendar));
            TemplateDialog dialog = new TemplateDialog();
            dialog.setCalendar(calendar);
            dialog.setTemplate(SuntimesCalendarSettings.loadPrefCalendarTemplate(context, calendar, calendarObj.defaultTemplate()));
            dialog.setDialogListener(templateDialog_listener);
            dialog.show(getSupportFragmentManager(), DIALOGTAG_TEMPLATE + "_" + calendar);
        }

        private final TemplateDialog.DialogListener templateDialog_listener = new TemplateDialog.DialogListener()
        {
            @Override
            public void onDialogAccepted(TemplateDialog dialog)
            {
                if (dialog.isModified())
                {
                    SuntimesCalendarSettings.savePrefCalendarTemplate(getActivity(), dialog.getCalendar(), dialog.getResult());
                    Toast.makeText(getActivity(), getString(R.string.template_dialog_saved_toast), Toast.LENGTH_SHORT).show();
                }
            }
        };

        /**
         * showReminderDialog
         */

        private static final String DIALOGTAG_REMINDER = "configreminders";
        protected void showReminderDialog(Context context, String calendar)
        {
            ReminderDialog dialog = new ReminderDialog();
            dialog.setCalendar(calendar);
            dialog.setDialogListener(reminderDialog_listener);
            dialog.show(getSupportFragmentManager(), DIALOGTAG_REMINDER + "_" + calendar);
        }

        private final ReminderDialog.DialogListener reminderDialog_listener = new ReminderDialog.DialogListener()
        {
            @Override
            public void onAddedReminder(String calendar, int reminderNum, int minutes, int method) {
                //Toast.makeText(getActivity(), "added " + reminderNum + " : " + minutes + " : " + method, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onModifiedReminder(String calendar, int reminderNum, int minutes, int method) {
                //Toast.makeText(getActivity(), "saved " + reminderNum + " : " + minutes + " : " + method, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemovedReminder(String calendar, int reminderNum) {
                //Toast.makeText(getActivity(), "removed " + reminderNum, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDialogDismissed(String calendar, boolean modified)
            {
                if (modified)
                {
                    Context context = getActivity();
                    if (SuntimesCalendarSettings.loadCalendarsEnabledPref(context) &&
                        SuntimesCalendarSettings.loadPrefCalendarEnabled(context, calendar))  // modify existing calendars
                    {
                        runCalendarTask2(getActivity(), calendar);
                    }
                }
            }
        };

        /**
         * showColorPicker
         */

        private ColorStateList createColorStateList(int calendarColor)
        {
            return new ColorStateList(
                    new int[][] {
                            new int[] { android.R.attr.state_pressed},
                            new int[] { android.R.attr.state_focused},
                            new int[] {-android.R.attr.state_enabled}, new int[] {} },
                    new int[] {pressedColor, calendarColor, disabledColor, calendarColor});
        }

        private static final String DIALOGTAG_COLOR = "colorchooser";
        private void showColorPicker(Context context, String calendar)
        {
            SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(getActivity().getContentResolver(), SuntimesCalendarDescriptor.getCalendars(getActivity()));
            SuntimesCalendarSettings settings = new SuntimesCalendarSettings();
            int color = settings.loadPrefCalendarColor(context, calendar);
            ArrayList<Integer> recentColors = new ArrayList<>();
            for (String item : adapter.getCalendarList()) {
                recentColors.add(settings.loadPrefCalendarColor(context, item));
            }

            Intent intent = new Intent(Intent.ACTION_PICK);
            //intent.setComponent(new ComponentName("com.forrestguice.suntimeswidget", "com.forrestguice.suntimeswidget.settings.colors.ColorActivity"));
            intent.setData(Uri.parse("color://" + String.format("#%08X", color)));
            //intent.putExtra("color", color);
            intent.putExtra("showAlpha", false);
            intent.putExtra("recentColors", recentColors);

            List<ResolveInfo> info = getActivity().getPackageManager().queryIntentActivities(intent, 0);
            if (!info.isEmpty())
            {
                int calendarNum = adapter.calendarOrdinal(calendar);
                if (calendarNum >= 0) {
                    startActivityForResult(intent, REQUEST_COLOR + calendarNum);
                }
            } else {
                showColorPickerFallback(context, calendar);
            }
        }
        private static final int REQUEST_COLOR = 1000;

        private void showColorPickerFallback(Context context, String calendar)
        {
            ColorDialog colorDialog = new ColorDialog();
            colorDialog.setShowAlpha(false);
            colorDialog.setColor(new SuntimesCalendarSettings().loadPrefCalendarColor(context, calendar));
            colorDialog.setColorChangeListener(onColorChanged(calendar));

            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                colorDialog.show(fragmentManager, DIALOGTAG_COLOR + "_" + calendar);
            } else {
                Log.w("showColorPicker", "fragmentManager is null; showing fallback ...");
                Dialog dialog = colorDialog.getDialog();
                dialog.show();
            }
        }

        /**
         * onActivityResult
         */
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            if (resultCode == RESULT_OK)
            {
                SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(getActivity().getContentResolver(), SuntimesCalendarDescriptor.getCalendars(getActivity()));
                String calendar = adapter.calendarName(requestCode - REQUEST_COLOR);
                if (calendar != null)
                {
                    int color;
                    Uri uri = data.getData();
                    if (uri != null)
                    {
                        try {
                            color = Color.parseColor("#" + uri.getFragment());

                        } catch (IllegalArgumentException e) {
                            color = Color.WHITE;
                            Log.e("ColorActivity", e.toString());
                        }
                    } else {
                        color = data.getIntExtra("color", Color.WHITE);
                    }
                    onColorChanged(calendar).onColorChanged( color );
                }
            }
        }

        private ColorDialog.ColorChangeListener onColorChanged(final String calendar)
        {
            return new ColorDialog.ColorChangeListener()
            {
                @Override
                public void onColorChanged(final int color)
                {
                    final Context context = getActivity();
                    if (context != null)
                    {
                        new SuntimesCalendarSettings().savePrefCalendarColor(context, calendar, color);

                        SuntimesCalendarPreference pref = calendarPrefs.get(calendar);
                        if (pref != null) {
                            pref.setIconColor(createColorStateList(color));
                        }

                        Thread thread = new Thread( new Runnable() {
                            @Override
                            public void run() {
                                SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(context.getContentResolver(), SuntimesCalendarDescriptor.getCalendars(context));
                                adapter.updateCalendarColor(calendar, color);
                            }
                        });
                        thread.start();
                    }
                }
            };
        }

        private void initPrefListeners(Activity activity)
        {
            if (activity == null)
                return;

            calendarsEnabledPref.setOnPreferenceChangeListener(onPreferenceChanged0(activity));
            for (String calendar : calendarPrefs.keySet())
            {
                CheckBoxPreference calendarPref = calendarPrefs.get(calendar);
                if (calendarPref != null) {
                    calendarPref.setOnPreferenceChangeListener(onPreferenceChanged1(activity, calendar));
                }
            }
        }

        private void clearPrefListeners()
        {
            calendarsEnabledPref.setOnPreferenceChangeListener(null);
            for (String calendar : calendarPrefs.keySet())
            {
                CheckBoxPreference calendarPref = calendarPrefs.get(calendar);
                if (calendarPref != null) {
                    calendarPref.setOnPreferenceChangeListener(null);
                }
            }
        }

        private Preference.OnPreferenceClickListener onLocationPrefClicked = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.setContent(getString(R.string.help_location) + "<br/>");
                helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                return false;
            }
        };

        private void updatePrefs(Activity activity)
        {
            if (activity == null)
                return;

            SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(activity.getContentResolver(), SuntimesCalendarDescriptor.getCalendars(activity));
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(activity).edit();

            Preference locationPref = findPreference("app_calendars_location");
            if (locationPref != null) {
                locationPref.setSummary(getLocationString(activity));
                locationPref.setOnPreferenceClickListener(onLocationPrefClicked);
            }

            if (hasCalendarPermissions(activity))
            {
                boolean calendarsEnabled0 = adapter.hasCalendars(activity);
                boolean calendarsEnabled1 = calendarsEnabledPref.isChecked();
                if (calendarsEnabled0 != calendarsEnabled1)
                {
                    Log.w(TAG, "onCreate: out of sync! setting pref to " + (calendarsEnabled0 ? "enabled" : "disabled"));
                    prefs.putBoolean(SuntimesCalendarSettings.PREF_KEY_CALENDARS_ENABLED, calendarsEnabled0);
                    prefs.apply();
                    calendarsEnabledPref.setChecked(calendarsEnabled0);
                }

                SuntimesCalendarSettings settings = new SuntimesCalendarSettings();
                for (String calendar : calendarPrefs.keySet())
                {
                    SuntimesCalendarPreference calendarPref = calendarPrefs.get(calendar);
                    if (calendarsEnabledPref.isChecked())
                    {
                        boolean enabled0 = false;
                        int color0 = -1;
                        Cursor cursor = adapter.queryCalendar(calendar);
                        if (cursor != null)
                        {
                            cursor.moveToFirst();
                            if (cursor.getCount() > 0)
                            {
                                enabled0 = true;
                                color0 = cursor.getInt(SuntimesCalendarAdapter.PROJECTION_CALENDAR_COLOR_INDEX);
                            } else {
                                enabled0 = false;
                                color0 = -1;
                            }
                            cursor.close();
                        }

                        boolean enabled1 = SuntimesCalendarSettings.loadPrefCalendarEnabled(activity, calendar);
                        if (enabled0 != enabled1)
                        {
                            Log.w(TAG, "onCreate: out of sync! setting " + calendar + " to " + (enabled0 ? "enabled" : "disabled"));
                            prefs.putBoolean(SuntimesCalendarSettings.PREF_KEY_CALENDARS_CALENDAR + calendar, enabled0);
                            prefs.apply();
                            calendarPref.setChecked(enabled0);
                        }

                        int color1 = settings.loadPrefCalendarColor(activity, calendar);
                        if (color0 != -1 && color0 != color1) {
                            Log.w(TAG, "onCreate: out of sync! setting " + calendar + " color to " + color0);
                            prefs.putInt(SuntimesCalendarSettings.PREF_KEY_CALENDARS_COLOR + calendar, color0);
                            prefs.apply();
                            calendarPref.setIconColor(createColorStateList(color0));
                        }
                    }
                    calendarPref.setNote(settings.loadCalendarNote(activity, calendar, SuntimesCalendarSettings.NOTE_LOCATION_NAME));
                }
            }
        }

        protected boolean runCalendarTask0(Activity activity, boolean enabled, String taskAction)
        {
            Intent taskIntent = new Intent(getActivity(), SuntimesCalendarSyncService.class);
            taskIntent.setAction(taskAction);
            savePendingItems(activity, taskIntent);
            return calendarTaskService.runCalendarTask(activity, taskIntent, !enabled, true, calendarTaskListener);
        }

        protected boolean runCalendarTask1(Activity activity, String calendar, boolean enabled)
        {
            Intent taskIntent = new Intent(getActivity(), SuntimesCalendarSyncService.class);
            taskIntent.setAction( SuntimesCalendarTaskService.ACTION_UPDATE_CALENDARS );
            savePendingItem(activity, taskIntent, calendar, enabled);
            return calendarTaskService.runCalendarTask(activity, taskIntent, false, true, calendarTaskListener);
        }

        protected boolean runCalendarTask2(Activity activity, String calendar)
        {
            Intent taskIntent = new Intent(getActivity(), SuntimesCalendarSyncService.class);
            taskIntent.setAction( SuntimesCalendarTaskService.ACTION_UPDATE_REMINDERS );
            savePendingItem(activity, taskIntent, calendar, SuntimesCalendarTaskItem.ACTION_REMINDERS_UPDATE);
            return calendarTaskService.runCalendarTask(activity, taskIntent, false, true, calendarTaskListener);
        }

        private Preference.OnPreferenceChangeListener onPreferenceChanged0(final Activity activity)
        {
            return new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    boolean enabled = (Boolean)newValue;
                    if (!hasCalendarPermissions(activity))
                    {
                        final int requestCode = (enabled ? REQUEST_CALENDARS_ENABLED : REQUEST_CALENDARS_DISABLED);
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALENDAR))
                        {
                            if (enabled) {
                                savePendingItems(activity, activity.getIntent());
                            }
                            showPermissionRational(activity, requestCode);
                            return false;

                        } else {
                            if (enabled) {
                                savePendingItems(activity, activity.getIntent());
                            }
                            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_CALENDAR }, requestCode);
                            return false;
                        }

                    } else {
                        if (enabled) {
                            return runCalendarTask0(activity, true, SuntimesCalendarTaskService.ACTION_UPDATE_CALENDARS);

                        } else {
                            showConfirmClearAllDialog(activity);
                            return false;
                        }
                    }
                }
            };
        }

        private Preference.OnPreferenceChangeListener onPreferenceChanged1(final Activity activity, final String calendar)
        {
            return new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    boolean calendarsEnabled = SuntimesCalendarSettings.loadCalendarsEnabledPref(activity);
                    if (calendarsEnabled)
                    {
                        boolean enabled = (Boolean)newValue;
                        if (!hasCalendarPermissions(activity))
                        {
                            final int requestCode = (enabled ? REQUEST_CALENDAR_ENABLED : REQUEST_CALENDAR_DISABLED);
                            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALENDAR))
                            {
                                savePendingItem(activity, activity.getIntent(), calendar, enabled);
                                showPermissionRational(activity, requestCode);
                                return false;

                            } else {
                                savePendingItem(activity, activity.getIntent(), calendar, enabled);
                                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_CALENDAR }, requestCode);
                                return false;
                            }

                        } else {
                            showConfirmDialog(activity, calendar, enabled);
                            return false;
                        }

                    } else {
                        return true;
                    }
                }
            };
        }

        protected void showConfirmClearAllDialog(Context context)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.confirm_clear_message0));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    runCalendarTask0(getActivity(), false, SuntimesCalendarTaskService.ACTION_CLEAR_CALENDARS);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }

        protected void showConfirmDialog(final Context context, final String calendar, boolean add)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(add ? R.string.confirm_add_message1 : R.string.confirm_clear_message1));

            final TextView textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setText(createConfirmDialogMessage(context, calendar, add));

            int padding = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics()));
            textView.setPadding(padding, padding, padding, padding);
            textView.setGravity(Gravity.CENTER_VERTICAL);

            Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_action_calendar).mutate();
            icon.setColorFilter(new SuntimesCalendarSettings().loadPrefCalendarColor(context, calendar), PorterDuff.Mode.MULTIPLY);
            textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            textView.setCompoundDrawablePadding(padding);
            
            builder.setView(textView);

            DialogInterface.OnClickListener onOkClick = (add ? new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    runCalendarTask1(getActivity(), calendar, true);
                }
            } : new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    runCalendarTask1(getActivity(), calendar, false);
                }
            });

            DialogInterface.OnClickListener onOptionsClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCalendarPref(calendar).performClickIcon();
                        }
                    }, 250);
                }
            };

            builder.setPositiveButton(android.R.string.yes, onOkClick);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setNeutralButton(R.string.configLabel_options, onOptionsClick);
            builder.show();
        }

        private CharSequence createConfirmDialogMessage(Context context, String calendar, boolean add)
        {
            SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
            String locationDisplay = (add ? getLocationString(context) : new SuntimesCalendarSettings().loadCalendarNote(context, calendar, SuntimesCalendarSettings.NOTE_LOCATION_NAME));
            String calendarDisplay = getCalendarDisplayString(context, descriptor, locationDisplay);
            if (locationDisplay != null)
            {
                SpannableString span = new SpannableString(calendarDisplay);
                int start = calendarDisplay != null ? calendarDisplay.indexOf(locationDisplay) : -1;
                if (start >= 0) {
                    int end = start + locationDisplay.length();
                    span.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return span;

            } else {
                return calendarDisplay;
            }
        }

        /**
         * getCalendarDisplayString
         */
        public static String getCalendarDisplayString(Context context, SuntimesCalendarDescriptor descriptor, @Nullable CharSequence locationDisplay)
        {
            String calendarDisplay;
            calendarDisplay = descriptor.calendarTitle();
            return (locationDisplay != null) ? context.getString(R.string.confirm_display_format, calendarDisplay, locationDisplay) : calendarDisplay;
        }

        private String getLocationString(Context context)
        {
            String location = null;
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG );
                String[] projection = new String[] { CalculatorProviderContract.COLUMN_CONFIG_LOCATION };
                try {
                    Cursor cursor = resolver.query(uri, projection, null, null, null);
                    if (cursor != null)
                    {
                        cursor.moveToFirst();
                        location = (!cursor.isNull(0)) ? cursor.getString(0) : null;
                        cursor.close();
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "getLocationString: Unable to access SuntimesCalculatorProvider! " + e);
                }
            }

            if (location != null) {
                return location;
            } else return null;
        }

        private Snackbar snackbar;
        private void showSnackbar(String message)
        {
            dismissSnackbar();
            View v = getView();
            if (v != null)
            {
                snackbar = Snackbar.make(v, message, Snackbar.LENGTH_INDEFINITE);          // TODO: swipeable (needs a coordinatorLayout)
                snackbar.setAction(getString(R.string.action_openCalendar), new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        openCalendarApp(getActivity());
                    }
                });
                snackbar.show();
            }
        }

        private void dismissSnackbar() {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                snackbar = null;
            }
        }

        public SuntimesCalendarTaskListener calendarTaskListener = new SuntimesCalendarTaskListener()
        {
            @Override
            public void onStarted(Context context, SuntimesCalendarTaskBase task, String message) {
                dismissSnackbar();
            }

            @Override
            public void onSuccess(Context context, SuntimesCalendarTaskBase task, String message) {
                showSnackbar(message);
            }
        };

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**private static class OnServiceResponse extends SuntimesCalendarSyncService.SuntimesCalendarServiceListener
    {
        @Override
        public void onStartCommand(boolean result)
        {
            super.onStartCommand(result);
        }

        public OnServiceResponse() {
            super();
        }
        public OnServiceResponse(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public OnServiceResponse createFromParcel(Parcel in) {
                return new OnServiceResponse(in);
            }
            public OnServiceResponse[] newArray(int size) {
                return new OnServiceResponse[size];
            }
        };
    }*/

    protected static void showMissingDepsMessage(final Activity context, View view)
    {
        if (view != null)
        {
            CharSequence message = Utils.fromHtml(context.getString(R.string.snackbar_missing_dependency, MIN_SUNTIMES_VERSION_STRING));
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbarError_background));
            snackbarView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AboutDialog.WEBSITE_URL));
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                    {
                        context.startActivity(intent);
                    }
                }
            });

            TextView textView = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            if (textView != null)
            {
                textView.setTextColor(ContextCompat.getColor(context, R.color.snackbarError_text));
                textView.setMaxLines(3);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            }

            snackbar.show();
        }
    }

    protected static void showPermissionDeniedMessage(final Activity context, View view)
    {
        if (view != null)
        {
            CharSequence message = Utils.fromHtml(context.getString(R.string.snackbar_missing_permission, context.getString(R.string.app_name)));
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbarError_background));
            snackbarView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO: show dialog
                }
            });

            TextView textView = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            if (textView != null)
            {
                textView.setTextColor(ContextCompat.getColor(context, R.color.snackbarError_text));
                textView.setMaxLines(9);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            }

            snackbar.show();
        }
    }

    /**
     * loadItems
     */
    public static ArrayList<SuntimesCalendarTaskItem> loadItems(Intent intent, boolean clearPending)
    {
        SuntimesCalendarTaskItem[] items;
        Parcelable[] parcelableArray = intent.getParcelableArrayExtra(SuntimesCalendarTaskService.EXTRA_CALENDAR_ITEMS);
        if (parcelableArray != null) {
            items = Arrays.copyOf(parcelableArray, parcelableArray.length, SuntimesCalendarTaskItem[].class);
        } else items = new SuntimesCalendarTaskItem[0];

        if (clearPending) {
            intent.removeExtra(SuntimesCalendarTaskService.EXTRA_CALENDAR_ITEMS);
        }
        return new ArrayList<>(Arrays.asList(items));
    }

    /**
     * saveItems
     */
    public static void savePendingItems(Activity activity, Intent intent)
    {
        ArrayList<SuntimesCalendarTaskItem> items = new ArrayList<>();
        for (String calendar : SuntimesCalendarDescriptor.getCalendars(activity)) {
            if (SuntimesCalendarSettings.loadPrefCalendarEnabled(activity, calendar)) {
                items.add(new SuntimesCalendarTaskItem(calendar, SuntimesCalendarTaskItem.ACTION_UPDATE));
            }
        }
        savePendingItems(activity, intent, items);
    }

    public static void savePendingItems(Activity activity, Intent intent, ArrayList<SuntimesCalendarTaskItem> items)
    {
        intent.putExtra(SuntimesCalendarTaskService.EXTRA_CALENDAR_ITEMS, items.toArray(new SuntimesCalendarTaskItem[0]));
    }

    public static void savePendingItem(Activity activity, Intent intent, String calendar, boolean enabled)
    {
        int action = (enabled ? SuntimesCalendarTaskItem.ACTION_UPDATE : SuntimesCalendarTaskItem.ACTION_DELETE);
        savePendingItem(activity, intent, calendar, action);
    }
    public static void savePendingItem(Activity activity, Intent intent, String calendar, int action)
    {
        ArrayList<SuntimesCalendarTaskItem> items = new ArrayList<>();
        items.add(new SuntimesCalendarTaskItem(calendar, action));
        savePendingItems(activity, intent, items);
    }

    /**
     * showAbout
     */
    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.setVersion(appVersionName, providerVersionCode);
        aboutDialog.setPermissionStatus(needsSuntimesPermissions);
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
    }
    private Preference.OnPreferenceClickListener onAboutClick = new Preference.OnPreferenceClickListener()
    {
        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            showAbout();
            return false;
        }
    };


}
