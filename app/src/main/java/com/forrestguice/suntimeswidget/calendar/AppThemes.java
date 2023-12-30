/**
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

package com.forrestguice.suntimeswidget.calendar;

import android.app.Activity;
import android.app.UiModeManager;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;

public class AppThemes
{
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_SYSTEM = "system";

    public static final String THEME_CONTRAST_LIGHT = "contrast_light";
    public static final String THEME_CONTRAST_DARK = "contrast_dark";
    public static final String THEME_CONTRAST_SYSTEM = "contrast_system";

    public static final String[] THEMES = new String[] { THEME_DARK, THEME_LIGHT, THEME_SYSTEM, THEME_CONTRAST_DARK, THEME_CONTRAST_LIGHT, THEME_CONTRAST_SYSTEM };

    public static int setTheme(Activity activity, String appTheme)
    {
        //Log.d("DEBUG", "setTheme: " + appTheme);
        int themeResID = themePrefToStyleId(activity, appTheme);
        activity.setTheme(themeResID);
        AppCompatDelegate.setDefaultNightMode(loadThemeInfo(appTheme).getDefaultNightMode());
        return themeResID;
    }

    public static int themePrefToStyleId(Context context, String themeName)
    {
        if (themeName != null) {
            AppThemeInfo themeInfo = loadThemeInfo(themeName);
            TextSize textSize = AppThemeInfo.getTextSize(themeName);
            //Log.d("DEBUG", "themePrefToStyleId: textSize: " + textSize);
            return themeInfo.getStyleId(context, textSize);
        } else return R.style.AppTheme;
    }

    public static boolean systemInNightMode(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null) {
            return (uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES);
        } else return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @NonNull
    public static AppThemeInfo loadThemeInfo(@Nullable String extendedThemeName)
    {
        if (extendedThemeName == null) {
            return info_defaultTheme;

        } else if (extendedThemeName.startsWith(THEME_LIGHT)) {
            return info_lightTheme;

        } else if (extendedThemeName.startsWith(THEME_DARK)) {
            return info_darkTheme;

        } else if (extendedThemeName.startsWith(THEME_SYSTEM)) {
            return info_systemTheme;

        } else if (extendedThemeName.startsWith(THEME_CONTRAST_LIGHT)) {
            return info_lightTheme_contrast;

        } else if (extendedThemeName.startsWith(THEME_CONTRAST_DARK)) {
            return info_darkTheme_contrast;

        } else if (extendedThemeName.startsWith(THEME_CONTRAST_SYSTEM)) {
            return info_systemTheme_contrast;

        } // else if (extendedThemeName.startsWith(SOME_THEME_NAME)) { /* TODO: additional themes here */ }
        else {
            return info_defaultTheme;
        }
    }
    private static final AppThemeInfo info_darkTheme = new DarkThemeInfo();
    private static final AppThemeInfo info_lightTheme = new LightThemeInfo();
    private static final AppThemeInfo info_systemTheme = new SystemThemeInfo();

    private static final AppThemeInfo info_systemTheme_contrast = new ContrastSystemThemeInfo();
    private static final AppThemeInfo info_darkTheme_contrast = new ContrastDarkThemeInfo();
    private static final AppThemeInfo info_lightTheme_contrast = new ContrastLightThemeInfo();

    private static final AppThemeInfo info_defaultTheme = info_systemTheme;

    /**
     * AppThemeInfo
     */
    public abstract static class AppThemeInfo
    {
        public abstract int getStyleId(Context context, TextSize textSize);
        public abstract String getThemeName();

        /**
         * @return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_NO;
         */
        public abstract int getDefaultNightMode();

        public String getExtendedThemeName(TextSize textSize) {
            return getExtendedThemeName(getThemeName(), textSize.name());
        }
        public String getExtendedThemeName(String textSize) {
            return getExtendedThemeName(getThemeName(), textSize);
        }

        public String getDisplayString(Context context) {
            return getThemeName();
        }
        public String toString() {
            return getThemeName();
        }

        public static String getExtendedThemeName(String themeName, String textSize) {
            return themeName + "_" + textSize;
        }
        public static TextSize getTextSize(String extendedThemeName) {
            String[] parts = extendedThemeName.split("_");
            return TextSize.valueOf((parts.length > 0 ? parts[parts.length-1] : TextSize.NORMAL.name()), TextSize.NORMAL);
        }
    }

    public static class SystemThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_SYSTEM;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        @Override
        public int getStyleId(Context context, TextSize size) {
            switch (size) {
                case SMALL: return R.style.AppTheme_System_Small;
                case LARGE: return R.style.AppTheme_System_Large;
                case XLARGE: return R.style.AppTheme_System_XLarge;
                case NORMAL: default: return R.style.AppTheme_System;
            }
        }
    }

    public static class LightThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Light_Small;
                case LARGE: return R.style.AppTheme_Light_Large;
                case XLARGE: return R.style.AppTheme_Light_XLarge;
                case NORMAL: default: return R.style.AppTheme_Light;
            }
        }
    }

    public static class DarkThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Dark_Small;
                case LARGE: return R.style.AppTheme_Dark_Large;
                case XLARGE: return R.style.AppTheme_Dark_XLarge;
                case NORMAL: default: return R.style.AppTheme_Dark;
            }
        }
    }

    public static class ContrastSystemThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_CONTRAST_SYSTEM;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        @Override
        public int getStyleId(Context context, TextSize size) {
            switch (size) {
                case SMALL: return R.style.AppTheme_ContrastSystem_Small;
                case LARGE: return R.style.AppTheme_ContrastSystem_Large;
                case XLARGE: return R.style.AppTheme_ContrastSystem_XLarge;
                case NORMAL: default: return R.style.AppTheme_ContrastSystem;
            }
        }
    }

    public static class ContrastLightThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_CONTRAST_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size) {
            switch (size) {
                case SMALL: return R.style.AppTheme_ContrastLight_Small;
                case LARGE: return R.style.AppTheme_ContrastLight_Large;
                case XLARGE: return R.style.AppTheme_ContrastLight_XLarge;
                case NORMAL: default: return R.style.AppTheme_ContrastLight;
            }
        }
    }

    public static class ContrastDarkThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_CONTRAST_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size) {
            switch (size) {
                case SMALL: return R.style.AppTheme_ContrastDark_Small;
                case LARGE: return R.style.AppTheme_ContrastDark_Large;
                case XLARGE: return R.style.AppTheme_ContrastDark_XLarge;
                case NORMAL: default: return R.style.AppTheme_ContrastDark;
            }
        }
    }

    /**
     * Text sizes
     */
    public static enum TextSize
    {
        SMALL("Small"),
        NORMAL("Normal"),
        LARGE("Large"),
        XLARGE("Extra Large");

        private TextSize( String displayString ) {
            this.displayString = displayString;
        }

        public String getDisplayString() {
            return displayString;
        }
        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }
        private String displayString;

        public static void initDisplayStrings( Context context )
        {
            SMALL.setDisplayString(context.getString(R.string.textSize_small));
            NORMAL.setDisplayString(context.getString(R.string.textSize_normal));
            LARGE.setDisplayString(context.getString(R.string.textSize_large));
            XLARGE.setDisplayString(context.getString(R.string.textSize_xlarge));
        }

        public static TextSize valueOf(String value, TextSize defaultValue)
        {
            try {
                return TextSize.valueOf(value);
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
        }
    }

}
