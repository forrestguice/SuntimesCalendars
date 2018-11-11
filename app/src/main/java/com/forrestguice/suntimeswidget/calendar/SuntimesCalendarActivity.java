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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract;

import java.util.Locale;

/**
 * SuntimesCalendarActivity
 */
public class SuntimesCalendarActivity extends AppCompatActivity
{
    public static final String DIALOGTAG_ABOUT = "aboutdialog";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    public static final int REQUEST_CALENDARPREFSFRAGMENT_ENABLED = 2;
    public static final int REQUEST_CALENDARPREFSFRAGMENT_DISABLED = 4;

    private Context context;
    private String config_apptheme = null;
    private static String systemLocale = null;  // null until locale is overridden w/ loadLocale

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
            String[] projection = new String[] { CalculatorProviderContract.COLUMN_CONFIG_LOCALE, CalculatorProviderContract.COLUMN_CONFIG_APPTHEME };
            try {
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                    String locale = cursor.getString(0);
                    config_apptheme = cursor.getString(1);
                    cursor.close();
                    super.attachBaseContext((locale != null) ? loadLocale(newBase, locale) : resetLocale(newBase));

                } else {
                    super.attachBaseContext(newBase);
                }
            } catch (SecurityException e) {
                Log.e("SuntimesCalendar", "attachBaseContext: Unable to access provider! " + e);
                super.attachBaseContext(newBase);
            }
        } else super.attachBaseContext(newBase);
    }

    private static Context loadLocale( Context context, String languageTag )
    {
        if (systemLocale == null) {
            systemLocale = Locale.getDefault().getLanguage();
        }

        Locale customLocale = localeForLanguageTag(languageTag);
        Locale.setDefault(customLocale);
        Log.i("loadLocale", languageTag);

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

    private static Context resetLocale( Context context )
    {
        if (systemLocale != null) {
            return loadLocale(context, systemLocale);
        }
        return context;
    }

    private static @NonNull Locale localeForLanguageTag(@NonNull String languageTag)
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
        Log.d("localeForLanguageTag", "tag: " + languageTag + " :: locale: " + locale.toString());
        return locale;
    }

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
        CalendarPrefsFragment fragment = new CalendarPrefsFragment();
        fragment.setAboutClickListener(onAboutClick);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (grantResults.length > 0 && permissions.length > 0)
        {
            switch (requestCode)
            {
                case REQUEST_CALENDARPREFSFRAGMENT_ENABLED:
                case REQUEST_CALENDARPREFSFRAGMENT_DISABLED:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        boolean enabled = requestCode == (REQUEST_CALENDARPREFSFRAGMENT_ENABLED);
                        runCalendarTask(SuntimesCalendarActivity.this, enabled);

                        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        pref.putBoolean(SuntimesCalendarSettings.PREF_KEY_CALENDARS_ENABLED, enabled);
                        pref.apply();

                        if (tmp_calendarPref != null)
                        {
                            tmp_calendarPref.setChecked(enabled);
                            tmp_calendarPref = null;
                        }
                    }
                    break;
            }
        }
    }

    /**
    /**
     * CalendarPrefsFragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CalendarPrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("CalendarPrefsFragment", "Arguments: " + getArguments());
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_calendars, false);
            addPreferencesFromResource(R.xml.preference_calendars);
            initPref_calendars(CalendarPrefsFragment.this, onAboutClick);
        }

        private Preference.OnPreferenceClickListener onAboutClick = null;
        public void setAboutClickListener( Preference.OnPreferenceClickListener onClick )
        {
            onAboutClick = onClick;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_calendars(final PreferenceFragment fragment, Preference.OnPreferenceClickListener onAboutClick)
    {
        Preference aboutPref = fragment.findPreference("app_about");
        if (aboutPref != null && onAboutClick != null) {
            aboutPref.setOnPreferenceClickListener(onAboutClick);
        }

        final Activity activity = fragment.getActivity();
        final CheckBoxPreference calendarsEnabledPref = (CheckBoxPreference) fragment.findPreference(SuntimesCalendarSettings.PREF_KEY_CALENDARS_ENABLED);
        final Preference.OnPreferenceChangeListener onPreferenceChanged0 = new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                boolean enabled = (Boolean)newValue;
                int calendarPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR);
                if (calendarPermission != PackageManager.PERMISSION_GRANTED)
                {
                    final int requestCode = (enabled ? REQUEST_CALENDARPREFSFRAGMENT_ENABLED : REQUEST_CALENDARPREFSFRAGMENT_DISABLED);
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALENDAR))
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
                                        tmp_calendarPref = calendarsEnabledPref;
                                    }
                                });

                        //if (Build.VERSION.SDK_INT >= 11)
                            //builder.setIconAttribute(R.attr.icActionWarning);
                        //else builder.setIcon(R.drawable.ic_action_warning);

                        builder.show();
                        return false;

                    } else {
                        ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_CALENDAR }, requestCode);
                        tmp_calendarPref = calendarsEnabledPref;
                        return false;
                    }

                } else {
                    runCalendarTask(activity, enabled);
                    return true;
                }
            }
        };
        calendarsEnabledPref.setOnPreferenceChangeListener(onPreferenceChanged0);
    }
    private static CheckBoxPreference tmp_calendarPref = null;

    private static void runCalendarTask(final Activity activity, boolean enabled)
    {
        SuntimesCalendarTask calendarTask = new SuntimesCalendarTask(activity);
        if (!enabled) {
            calendarTask.setFlagClearCalendars(true);
        }
        calendarTask.execute();
    }

    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

    /**
     * showAbout
     */
    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
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
