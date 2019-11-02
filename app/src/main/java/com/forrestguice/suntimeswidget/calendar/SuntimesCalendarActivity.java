/*
    Copyright (C) 2018-2019 Forrest Guice
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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * SuntimesCalendarActivity
 */
public class SuntimesCalendarActivity extends AppCompatActivity
{
    public static String TAG = "SuntimesCalendar";

    public static final String DIALOGTAG_ABOUT = "aboutdialog";
    public static final String DIALOGTAG_PROGRESS = "progressdialog";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
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
                                                 CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION, CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE };
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

        if (config_apptheme != null) {
            setTheme(config_apptheme.equals(THEME_LIGHT) ? R.style.AppTheme_Light : R.style.AppTheme_Dark);
        }

        super.onCreate(icicle);

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
        getFragmentManager().beginTransaction().replace(android.R.id.content, firstLaunchFragment).commit();
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
        getFragmentManager().beginTransaction().replace(android.R.id.content, mainFragment).commit();
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

                        ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> items = loadItems(this.getIntent(), true);
                        savePendingItems(this, taskIntent, items);
                        calendarTaskService.runCalendarTask(context, taskIntent, false, false, mainFragment.calendarTaskListener);

                        if (mainFragment != null)
                        {
                            SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                            for (SuntimesCalendarTask.SuntimesCalendarTaskItem item : items)
                            {
                                boolean enabled = (item.getAction() == SuntimesCalendarTask.SuntimesCalendarTaskItem.ACTION_UPDATE);
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

                        ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> items = new ArrayList<>();
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
                    .setMessage(fromHtml(permissionMessage))
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

        private HashMap<String, CheckBoxPreference> calendarPrefs = new HashMap<>();
        public CheckBoxPreference getCalendarPref(String calendar)
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

        /**@Override
        public void onStop()
        {
            super.onStop();
        }*/

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i(TAG, "CalendarPrefsFragment: Arguments: " + getArguments());
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_calendars, false);
            addPreferencesFromResource(R.xml.preference_calendars);

            final Activity activity = getActivity();
            initAboutDialog();
            initProgressDialog();

            calendarsEnabledPref = (CheckBoxPreference) findPreference(SuntimesCalendarSettings.PREF_KEY_CALENDARS_ENABLED);
            for (String calendar : SuntimesCalendarAdapter.ALL_CALENDARS)
            {
                CheckBoxPreference calendarPref = (CheckBoxPreference)findPreference(SuntimesCalendarSettings.PREF_KEY_CALENDARS_CALENDAR + calendar);
                calendarPrefs.put(calendar, calendarPref);
            }

            updatePrefs(activity);
            initPrefListeners(activity);

            if (needsSuntimesPermissions || !checkDependencies())
            {
                if (!calendarsEnabledPref.isChecked())
                {
                    calendarsEnabledPref.setEnabled(false);
                    Preference windowStart = findPreference(SuntimesCalendarSettings.PREF_KEY_CALENDAR_WINDOW0);
                    windowStart.setEnabled(false);
                    Preference windowEnd = findPreference(SuntimesCalendarSettings.PREF_KEY_CALENDAR_WINDOW1);
                    windowEnd.setEnabled(false);
                }

                if (needsSuntimesPermissions)
                    showPermissionDeniedMessage(getActivity(), getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
                else showMissingDepsMessage(getActivity(), getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
            }
            setIsBusy(isBusy);
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

        private void updatePrefs(Activity activity)
        {
            if (activity == null)
                return;

            SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(activity.getContentResolver());
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(activity).edit();

            if (hasCalendarPermissions(activity))
            {
                boolean calendarsEnabled0 = adapter.hasCalendars();
                boolean calendarsEnabled1 = calendarsEnabledPref.isChecked();
                if (calendarsEnabled0 != calendarsEnabled1)
                {
                    Log.w(TAG, "onCreate: out of sync! setting pref to " + (calendarsEnabled0 ? "enabled" : "disabled"));
                    prefs.putBoolean(SuntimesCalendarSettings.PREF_KEY_CALENDARS_ENABLED, calendarsEnabled0);
                    prefs.apply();
                    calendarsEnabledPref.setChecked(calendarsEnabled0);
                }

                for (String calendar : calendarPrefs.keySet())
                {
                    CheckBoxPreference calendarPref = calendarPrefs.get(calendar);
                    if (calendarsEnabledPref.isChecked())
                    {
                        boolean enabled0 = adapter.hasCalendar(calendar);
                        boolean enabled1 = SuntimesCalendarSettings.loadPrefCalendarEnabled(activity, calendar);
                        if (enabled0 != enabled1)
                        {
                            Log.w(TAG, "onCreate: out of sync! setting " + calendar + " to " + (enabled0 ? "enabled" : "disabled"));
                            prefs.putBoolean(SuntimesCalendarSettings.PREF_KEY_CALENDARS_CALENDAR + calendar, enabled0);
                            prefs.apply();
                            calendarPref.setChecked(enabled0);
                        }
                    }
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
                            if (enabled) {
                                showConfirmAddDialog(activity, calendar);
                            } else {
                                showConfirmClearDialog(activity, calendar);
                            }
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

        protected void showConfirmClearDialog(Context context, final String calendar)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.confirm_clear_message1));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    runCalendarTask1(getActivity(), calendar, false);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }

        protected void showConfirmAddDialog(Context context, final String calendar)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.confirm_add_message1));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    runCalendarTask1(getActivity(), calendar, true);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
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
                    public void onClick(View v)
                    {
                        Intent intent = SuntimesCalendarTaskService.getCalendarIntent();
                        startActivity(intent);
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

        public SuntimesCalendarTask.SuntimesCalendarTaskListener calendarTaskListener = new SuntimesCalendarTask.SuntimesCalendarTaskListener()
        {
            @Override
            public void onStarted(Context context, SuntimesCalendarTask task, String message) {
                dismissSnackbar();
            }

            @Override
            public void onSuccess(Context context, SuntimesCalendarTask task, String message) {
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
            CharSequence message = fromHtml(context.getString(R.string.snackbar_missing_dependency, MIN_SUNTIMES_VERSION_STRING));
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
            CharSequence message = fromHtml(context.getString(R.string.snackbar_missing_permission));
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
                textView.setMaxLines(7);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            }

            snackbar.show();
        }
    }

    /**
     * loadItems
     */
    public static ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> loadItems(Intent intent, boolean clearPending)
    {
        SuntimesCalendarTask.SuntimesCalendarTaskItem[] items;
        Parcelable[] parcelableArray = intent.getParcelableArrayExtra(SuntimesCalendarTaskService.EXTRA_CALENDAR_ITEMS);
        if (parcelableArray != null) {
            items = Arrays.copyOf(parcelableArray, parcelableArray.length, SuntimesCalendarTask.SuntimesCalendarTaskItem[].class);
        } else items = new SuntimesCalendarTask.SuntimesCalendarTaskItem[0];

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
        ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> items = new ArrayList<>();
        for (String calendar : SuntimesCalendarAdapter.ALL_CALENDARS) {
            if (SuntimesCalendarSettings.loadPrefCalendarEnabled(activity, calendar)) {
                items.add(new SuntimesCalendarTask.SuntimesCalendarTaskItem(calendar, SuntimesCalendarTask.SuntimesCalendarTaskItem.ACTION_UPDATE));
            }
        }
        savePendingItems(activity, intent, items);
    }

    public static void savePendingItems(Activity activity, Intent intent, ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> items)
    {
        intent.putExtra(SuntimesCalendarTaskService.EXTRA_CALENDAR_ITEMS, items.toArray(new SuntimesCalendarTask.SuntimesCalendarTaskItem[0]));
    }

    public static void savePendingItem(Activity activity, Intent intent, String calendar, boolean enabled)
    {
        ArrayList<SuntimesCalendarTask.SuntimesCalendarTaskItem> items = new ArrayList<>();
        int action = (enabled ? SuntimesCalendarTask.SuntimesCalendarTaskItem.ACTION_UPDATE : SuntimesCalendarTask.SuntimesCalendarTaskItem.ACTION_DELETE);
        items.add(new SuntimesCalendarTask.SuntimesCalendarTaskItem(calendar, action));
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

    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }
}
