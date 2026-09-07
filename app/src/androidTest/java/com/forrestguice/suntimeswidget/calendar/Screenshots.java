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
import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.test.filters.LargeTest;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.platform.io.PlatformTestStorageRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import android.os.Build;
import android.util.Log;

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
@ScreenshotCreator
public class Screenshots extends TestBase
{
    @Rule
    public ActivityTestRule<SuntimesCalendarActivity> activityRule = new ActivityTestRule<>(SuntimesCalendarActivity.class);

    private final String[] testPermissions = (Build.VERSION.SDK_INT >= 33)
            ? new String[] { Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.POST_NOTIFICATIONS }
            : new String[] { Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR };

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(testPermissions);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
    }

    @Test
    public void makeScreenshots_fastlane_dark() {
        runTests(getContext(), AppThemes.THEME_DARK, AppThemes.THEME_MONET_DARK, testTextSize(), testLocale(), screenshotTest());
    }

    @Test
    public void makeScreenshots_fastlane_light() {
        runTests(getContext(), AppThemes.THEME_LIGHT, AppThemes.THEME_MONET_LIGHT, testTextSize(), testLocale(), screenshotTest());
    }

    protected ActivityTest screenshotTest()
    {
        return new CalendarActivityTest.CalendarActivityTestCase(activityRule)
        {
            @Override
            public void runTest(Activity activity, String tag)
            {
                onView(withId(android.R.id.content)).perform(swipeDown());     // clears focus
                CalendarActivityTest.CalendarActivityRobot robot = new CalendarActivityTest.CalendarActivityRobot();

                robot.captureScreenshot(activity, tag, "0");    // main activity
                robot.clickCalendarIcon(R.string.calendar_civil_twilight_displayName)
                        .sleep(1000)
                        .captureScreenshot(activity, tag, "1")
                        .clickCalendarOptionsMenu_preview()
                        .clickDialogButton_menu()
                        .captureScreenshot(activity, tag, "2")    // preview dialog (w/ menu)
                        .clickDialogButton_back();

                robot.clickCalendarIcon(R.string.calendar_civil_twilight_displayName)
                        .sleep(1000)
                        .clickCalendarOptionsMenu_flags()
                        .captureScreenshot(activity, tag, "3")    // flags dialog
                        .clickDialogButton_back();

                robot.clickCalendarIcon(R.string.calendar_civil_twilight_displayName)
                        .sleep(1000)
                        .clickCalendarOptionsMenu_template()
                        .captureScreenshot(activity, tag, "4")    // template dialog
                        .clickDialogButton_back();

                robot.clickCalendarIcon(R.string.calendar_civil_twilight_displayName)
                        .sleep(1000)
                        .clickCalendarOptionsMenu_template()
                        .clickDialogButton_eventStrings()
                        .captureScreenshot(activity, tag, "5");    // strings dialog

                /*robot.clickCalendarIcon(R.string.calendar_civil_twilight_displayName)
                        .clickCalendarOptionsMenu_reminders()
                        .clickDialogButton_addReminder()
                        .captureScreenshot(activity, tag, "5");*/
            }
        };
    }

    /**
     * captureScreenshot
     */

    public static Bitmap captureScreenshot(Activity activity) {
        return Falcon.takeScreenshotBitmap(activity);
    }
    public static void captureScreenshot(Activity activity, @Nullable String tag, String name) {
        saveScreenshot(tag, name, captureScreenshot(activity));
    }
    public static void saveScreenshot(@Nullable String tag, String name, Bitmap bitmap)
    {
        PlatformTestStorage storage = PlatformTestStorageRegistry.getInstance();
        if (storage != null)
        {
            String path = (tag != null && !tag.trim().isEmpty())
                    ? tag + "_" + name + ".png"
                    : name + ".png";

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
