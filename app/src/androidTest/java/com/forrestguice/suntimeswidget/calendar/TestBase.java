/*
    Copyright (C) 2026 Forrest Guice
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
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.util.Log;

import com.forrestguice.suntimescalendars.BuildConfig;
import com.forrestguice.suntimescalendars.R;

import org.hamcrest.CoreMatchers;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.hasDrawable;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.navigationButton;

public class TestBase
{
    protected String testLocale() {
        return "en_US";                                   // null for all locales
    }
    protected AppThemes.TextSize testTextSize() {
        return AppThemes.TextSize.NORMAL;                 // null for all sizes
    }

    protected String testAppThemeBase() {
        return AppThemes.THEME_SYSTEM;
    }

    protected String testAppTheme() {
        return AppThemes.THEME_MONET_SYSTEM;    // null; default
    }

    public static final String TAG = "SuntimesCalendars";
    public String tag(String languageTag, String themeName, AppThemes.TextSize textSize)
    {
        return TAG + "_v" + BuildConfig.VERSION_NAME
                + "_" + languageTag
                + "_" + themeName
                + "_t" + textSize.ordinal();
        //+ "^" + textSize.name().toLowerCase(Locale.ROOT);
    }

    public static Context getContext() {
        return androidx.test.InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    public static void setAnimationsEnabled(boolean enabled) throws IOException
    {
        UiAutomation automation = androidx.test.InstrumentationRegistry.getInstrumentation().getUiAutomation();
        automation.executeShellCommand("settings put global transition_animation_scale " + (enabled ? "1" : "0")).close();
        automation.executeShellCommand("settings put global window_animation_scale " + (enabled ? "1" : "0")).close();
        automation.executeShellCommand("settings put global animator_duration_scale " + (enabled ? "1" : "0")).close();
    }

    /**
     * ActivityTest
     */
    public interface ActivityTest
    {
        void initLocale(Intent intent, String locale);
        void initTheme(Intent intent, String themeBase, String themeName, AppThemes.TextSize textSize);
        void runTest(Activity activity, String tag);
        ActivityTestRule<?> activityRule();
    }

    public static abstract class BaseActivityTest implements ActivityTest
    {
        @Override
        public void initLocale(Intent intent, String locale) {
            SuntimesCalendarActivity.locale = locale;
        }

        @Override
        public void initTheme(Intent intent, String themeBase, String themeName, AppThemes.TextSize textSize)
        {
            intent.putExtra(SuntimesCalendarActivity.EXTRA_THEME, themeBase);
            intent.putExtra(SuntimesCalendarActivity.EXTRA_THEME_OVERRIDE, themeName);
            intent.putExtra(SuntimesCalendarActivity.EXTRA_THEME_TEXT_SIZE, textSize.name());
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            /*
            // captureScreenshot(activity, tag, name);    // saved automatically

            // Bitmap result = captureScreenshot(activity);
            // validateResult(oracle, result);
            // saveScreenshot(tag, name, result);    // save manually after validating
            */
        }

        @Override
        public abstract ActivityTestRule<?> activityRule();
    }

    /**
     * runTests
     */

    public void runTests(Context context, String themeBase, String themeName, ActivityTest test) {
        runTests(context, themeBase, themeName, AppThemes.TextSize.NORMAL, "en_US", test);
    }
    public void runTests(Context context, String themeBase, String themeName, @Nullable AppThemes.TextSize textSize, @Nullable String languageTag, ActivityTest test)
    {
        AppThemes.TextSize[] sizes = (textSize != null)
                ? new AppThemes.TextSize[] { textSize }
                : AppThemes.TextSize.values();

        String[] locales = (languageTag != null)
                ? new String[] { languageTag }
                : context.getResources().getStringArray(R.array.locale_values);

        for (String locale : locales) {
            for (AppThemes.TextSize size : sizes) {
                runTest(themeBase, themeName, size, locale, test);
                SystemClock.sleep(500);   // TODO: flaky activity finish
            }
        }
    }
    public void runTest(String themeBase, String themeName, AppThemes.TextSize textSize, String languageTag, ActivityTest test)
    {
        String tag = tag(languageTag, (themeName != null ? themeName : themeBase), textSize);
        Log.d(TAG, "running " + test.getClass().getSimpleName() + " with " + tag);

        ActivityTestRule<?> activityRule = test.activityRule();
        Activity activity = activityRule.getActivity();
        Intent intent = activity.getIntent();
        test.initTheme(intent, themeBase, themeName, textSize);

        activity.finish();
        test.initLocale(intent, languageTag);
        activityRule.launchActivity(intent);
        activity = activityRule.getActivity();
        test.runTest(activity, tag);
        activity.finish();
    }

    /**
     * Robot
     */
    public static abstract class Robot<T>
    {
        protected T robot;
        public void setRobot(T robot) {
            this.robot = robot;
        }

        public T sleep(long ms) {
            SystemClock.sleep(ms);
            return robot;
        }

        public T doubleRotateDevice(Activity activity)
        {
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            sleep(1000);
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return robot;
        }
        public T rotateDevice(Activity activity, int orientation) {
            activity.setRequestedOrientation(orientation);
            return robot;
        }

        public T captureScreenshot(Activity activity, String name) {
            captureScreenshot(activity, "", name);
            return robot;
        }
        public T captureScreenshot(Activity activity, String tag, String name) {
            Screenshots.captureScreenshot(activity, tag, name);
            return robot;
        }
    }

    /**
     * ActivityRobot
     */
    public static abstract class ActivityRobot<T> extends Robot<T>
    {
        public ActivityRobot() {}
        public ActivityRobot(T robot) {
            this.robot = robot;
        }

        public T recreateActivity(final Activity activity)
        {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                public void run() {
                    activity.recreate();
                }
            });
            return robot;
        }

        public T finishActivity(final Activity activity)
        {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                public void run() {
                    activity.finish();
                }
            });
            return robot;
        }

        public T clickHomeButton(Context context) {
            onView(navigationButton()).perform(click());
            return robot;
        }

        public T clickOverflowMenu(Context context) {
            openActionBarOverflowOrOptionsMenu(context);
            return robot;
        }

        public T assertActionBar_homeButtonShown(boolean shown) {
            onView(CoreMatchers.allOf(navigationButton(), hasDrawable(R.drawable.ic_suntimes_calendar))).check(shown ? assertShown : doesNotExist());
            return robot;
        }

        public T clickDialogButton_ok() {
            onView(withText(R.string.dialog_ok)).perform(click());
            return robot;
        }

        public T clickDialogButton_accept() {
            onView(withId(R.id.accept_button)).perform(click());
            return robot;
        }

        public T clickDialogButton_back() {
            onView(withId(R.id.back_button)).perform(click());
            return robot;
        }

        public T clickDialogButton_menu() {
            onView(withId(R.id.menu_button)).perform(click());
            return robot;
        }

        public T clickDialogButton_cancel() {
            onView(withText(R.string.dialog_cancel)).perform(click());
            return robot;
        }

    }
}
