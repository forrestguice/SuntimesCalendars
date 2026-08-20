/*
    Copyright (C) 2019 Forrest Guice
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
import android.content.Intent;
import android.graphics.Bitmap;

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
    public static final String SCREENSHOT_DIR = "screenshot";

    @Rule
    public ActivityTestRule<SuntimesCalendarActivity> activityRule = new ActivityTestRule<>(SuntimesCalendarActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);

    @Test
    public void make_screenshots()
    {
        String version = BuildConfig.VERSION_NAME;
        if (!version.startsWith("v")) {
            version = "v" + version;
        }

        Activity activity = activityRule.getActivity();
        Intent intent = activity.getIntent();

        String[] locales = activity.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            activity.finish();
            Log.d("screenshots", "making screenshots for " + languageTag);
            SuntimesCalendarActivity.locale = languageTag;
            activityRule.launchActivity(intent);
            activity = activityRule.getActivity();

            onView(withId(android.R.id.content)).perform(swipeDown());     // clears focus
            captureScreenshot(activity,BuildConfig.VERSION_NAME + "_" + languageTag,"activity-calendars0");   // TODO: themes
        }
    }

    public static void captureScreenshot(Activity activity, String tag, String name)
    {
        PlatformTestStorage storage = PlatformTestStorageRegistry.getInstance();
        if (storage != null)
        {
            String path = SCREENSHOT_DIR + "_" + tag + "_" + name + ".png";

            try {
                Bitmap b = Falcon.takeScreenshotBitmap(activity);
                BufferedOutputStream out = new BufferedOutputStream(storage.openOutputFile(path));
                b.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();

            } catch (IOException e) {
                Log.e("captureScreenshot", "Failed to write file! " + e);
            }
        } else {
            Log.e("captureScreenshot", "Failed to write file! getExternalFilesDir() returns null..");
        }
    }
}
