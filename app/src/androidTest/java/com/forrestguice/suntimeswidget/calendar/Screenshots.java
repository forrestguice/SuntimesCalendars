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

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.forrestguice.suntimescalendars.BuildConfig;
import com.forrestguice.suntimescalendars.R;
import com.jraska.falcon.Falcon;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Screenshots
{
    public static final String SCREENSHOT_DIR = "test-screenshots";

    @Rule
    public ActivityTestRule<SuntimesCalendarActivity> activityRule = new ActivityTestRule<>(SuntimesCalendarActivity.class);

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
            captureScreenshot(activity,BuildConfig.VERSION_NAME + "/" + languageTag,"activity-calendars0");   // TODO: themes
        }
    }

    public static void captureScreenshot(Activity activity, String subdir, String name)
    {
        subdir = subdir.trim();
        if (!subdir.isEmpty() && !subdir.startsWith("/")) {
            subdir = "/" + subdir;
        }

        // saves to..
        //     SD card\Android\data\com.forrestguice.suntimeswidget.calendar\files\Pictures\test-screenshots\subdir
        File d = activity.getExternalFilesDir(DIRECTORY_PICTURES);
        if (d != null)
        {
            String dirPath = d.getAbsolutePath() + "/" + SCREENSHOT_DIR + subdir;
            File dir = new File(dirPath);
            boolean dirCreated = dir.mkdirs();

            String path = dirPath + "/" + name + ".png";
            File file = new File(path);
            if (file.exists()) {
                if (!file.delete()) {
                    Log.w("captureScreenshot", "Failed to delete file! " + path);
                }
            }

            try {
                Falcon.takeScreenshot(activity, file);
                MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null, null);

            } catch (Exception e1) {
                Log.e("captureScreenshot", "Failed to write file! " + e1);
            }
        } else {
            Log.e("captureScreenshot", "Failed to write file! getExternalFilesDir() returns null..");
        }
    }
}
