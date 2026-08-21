/*
    Copyright (C) 2019-2026 Forrest Guice
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
import android.app.Activity;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.test.filters.LargeTest;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.platform.io.PlatformTestStorageRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import android.util.Log;

import com.forrestguice.suntimescalendars.BuildConfig;
import com.forrestguice.suntimescalendars.R;
import com.jraska.falcon.Falcon;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedOutputStream;
import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Screenshots
{
    @Rule
    public ActivityTestRule<SuntimesCalendarActivity> activityRule = new ActivityTestRule<>(SuntimesCalendarActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);

    public static final String TAG = "SuntimesCalendars";
    protected String tag(String languageTag, String themeName, AppThemes.TextSize textSize)
    {
        return TAG + "_v" + BuildConfig.VERSION_NAME
                + "_" + languageTag
                + "_" + themeName
                + "_t" + textSize.ordinal();
                //+ "^" + textSize.name().toLowerCase(Locale.ROOT);
    }

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
    }

    public static void setAnimationsEnabled(boolean enabled) throws IOException
    {
        UiAutomation automation = androidx.test.InstrumentationRegistry.getInstrumentation().getUiAutomation();
        automation.executeShellCommand("settings put global transition_animation_scale " + (enabled ? "1" : "0")).close();
        automation.executeShellCommand("settings put global window_animation_scale " + (enabled ? "1" : "0")).close();
        automation.executeShellCommand("settings put global animator_duration_scale " + (enabled ? "1" : "0")).close();
    }

    public static Context getContext() {
        return androidx.test.InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * Default
     */

    @Test
    public void makeScreenshots_default_dark() {
        makeScreenshots(getContext(), AppThemes.THEME_DARK, null, new MainScreenshot());
    }
    @Test
    public void makeScreenshots_default_light() {
        makeScreenshots(getContext(), AppThemes.THEME_LIGHT, null, new MainScreenshot());
    }
    @Test
    public void makeScreenshots_default_system() {
        makeScreenshots(getContext(), AppThemes.THEME_SYSTEM, null, new MainScreenshot());
    }

    /**
     * Contrast
     */

    @Test
    public void makeScreenshots_contrast_dark() {
        makeScreenshots(getContext(), AppThemes.THEME_DARK, AppThemes.THEME_CONTRAST_DARK, new MainScreenshot());
    }
    @Test
    public void makeScreenshots_contrast_light() {
        makeScreenshots(getContext(), AppThemes.THEME_LIGHT, AppThemes.THEME_CONTRAST_LIGHT, new MainScreenshot());
    }
    @Test
    public void makeScreenshots_contrast_system() {
        makeScreenshots(getContext(), AppThemes.THEME_SYSTEM, AppThemes.THEME_CONTRAST_SYSTEM, new MainScreenshot());
    }

    /**
     * Material You
     */

    @Test
    public void makeScreenshots_monet_dark() {
        makeScreenshots(getContext(), AppThemes.THEME_DARK, AppThemes.THEME_MONET_DARK, new MainScreenshot());
    }
    @Test
    public void makeScreenshots_monet_light() {
        makeScreenshots(getContext(), AppThemes.THEME_LIGHT, AppThemes.THEME_MONET_LIGHT, new MainScreenshot());
    }
    @Test
    public void makeScreenshots_monet_system() {
        makeScreenshots(getContext(), AppThemes.THEME_SYSTEM, AppThemes.THEME_MONET_SYSTEM, new MainScreenshot());
    }

    /**
     * makeScreenshots
     */

    public interface Screenshot
    {
        String name();
        void prepareForScreenshot(Activity activity);
        void validateResult(Activity activity, String themeBase, String themeName, AppThemes.TextSize textSize, String languageTag, Bitmap bitmap);
    }

    public static class MainScreenshot implements Screenshot
    {
        @Override
        public String name() {
            return "0";
        }

        @Override
        public void prepareForScreenshot(Activity activity) {
            onView(withId(android.R.id.content)).perform(swipeDown());     // clears focus
        }

        @Override
        public void validateResult(Activity activity, String themeBase, String themeName, @Nullable AppThemes.TextSize textSize, @Nullable String languageTag, Bitmap bitmap) {
            /* EMPTY */
        }
    }

    public void makeScreenshots(Context context, String themeBase, String themeName, Screenshot screenshot) {
        makeScreenshots(context, themeBase, themeName, AppThemes.TextSize.NORMAL, "en_US", screenshot);
    }
    public void makeScreenshots(Context context, String themeBase, String themeName, @Nullable AppThemes.TextSize textSize, @Nullable String languageTag, Screenshot screenshot)
    {
        AppThemes.TextSize[] sizes = (textSize != null)
                ? new AppThemes.TextSize[] { textSize }
                : AppThemes.TextSize.values();

        String[] locales = (languageTag != null)
                ? new String[] { languageTag }
                : context.getResources().getStringArray(R.array.locale_values);

        for (String locale : locales) {
            for (AppThemes.TextSize size : sizes) {
                makeScreenshots(themeBase, themeName, size, locale, screenshot);
            }
        }
    }
    public void makeScreenshots(String themeBase, String themeName, AppThemes.TextSize textSize, String languageTag, Screenshot screenshot)
    {
        Log.d(TAG, "making " + screenshot.getClass().getSimpleName() + " for " + languageTag + ":" + themeBase + ":" + themeName + ":" + textSize.name());

        Activity activity = activityRule.getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(SuntimesCalendarActivity.EXTRA_THEME, themeBase);
        intent.putExtra(SuntimesCalendarActivity.EXTRA_THEME_OVERRIDE, themeName);
        intent.putExtra(SuntimesCalendarActivity.EXTRA_THEME_TEXT_SIZE, textSize.name());

        activity.finish();
        SuntimesCalendarActivity.locale = languageTag;
        activityRule.launchActivity(intent);
        activity = activityRule.getActivity();

        screenshot.prepareForScreenshot(activity);
        Bitmap result = captureScreenshot(activity);
        saveScreenshot(tag(languageTag, themeName, textSize), screenshot.name(), result);
        screenshot.validateResult(activity, themeBase, themeName, textSize, languageTag, result);

        activity.finish();
    }

    public static Bitmap captureScreenshot(Activity activity) {
        return Falcon.takeScreenshotBitmap(activity);
    }
    public static void captureScreenshot(Activity activity, String tag, String name) {
        saveScreenshot(tag, name, captureScreenshot(activity));
    }
    public static void saveScreenshot(String tag, String name, Bitmap bitmap)
    {
        PlatformTestStorage storage = PlatformTestStorageRegistry.getInstance();
        if (storage != null)
        {
            String path = tag + "_" + name + ".png";

            try {
                BufferedOutputStream out = new BufferedOutputStream(storage.openOutputFile(path));
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();

            } catch (IOException e) {
                Log.e(TAG, "Failed to write file! " + e);
            }
        } else {
            Log.e(TAG, "Failed to write file! getExternalFilesDir() returns null..");
        }
    }

}
