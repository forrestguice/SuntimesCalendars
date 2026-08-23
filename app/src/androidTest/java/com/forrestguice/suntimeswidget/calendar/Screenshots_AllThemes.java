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

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

@LargeTest
@RunWith(AndroidJUnit4.class)
@ScreenshotCreator
public class Screenshots_AllThemes extends Screenshots
{
    /**
     * Default
     */

    @Test
    public void makeScreenshots_default_dark() {
        runTests(getContext(), AppThemes.THEME_DARK, null, testTextSize(), testLocale(), screenshotTest());
    }
    @Test
    public void makeScreenshots_default_light() {
        runTests(getContext(), AppThemes.THEME_LIGHT, null, testTextSize(), testLocale(), screenshotTest());
    }
    @Test
    public void makeScreenshots_default_system() {
        runTests(getContext(), AppThemes.THEME_SYSTEM, null, testTextSize(), testLocale(), screenshotTest());
    }

    /**
     * Contrast
     */

    @Test
    public void makeScreenshots_contrast_dark() {
        runTests(getContext(), AppThemes.THEME_DARK, AppThemes.THEME_CONTRAST_DARK, testTextSize(), testLocale(), screenshotTest());
    }
    @Test
    public void makeScreenshots_contrast_light() {
        runTests(getContext(), AppThemes.THEME_LIGHT, AppThemes.THEME_CONTRAST_LIGHT, testTextSize(), testLocale(), screenshotTest());
    }
    @Test
    public void makeScreenshots_contrast_system() {
        runTests(getContext(), AppThemes.THEME_SYSTEM, AppThemes.THEME_CONTRAST_SYSTEM, testTextSize(), testLocale(), screenshotTest());
    }

    /**
     * Material You
     */

    @Test
    public void makeScreenshots_monet_dark() {
        runTests(getContext(), AppThemes.THEME_DARK, AppThemes.THEME_MONET_DARK, testTextSize(), testLocale(), screenshotTest());
    }
    @Test
    public void makeScreenshots_monet_light() {
        runTests(getContext(), AppThemes.THEME_LIGHT, AppThemes.THEME_MONET_LIGHT, testTextSize(), testLocale(), screenshotTest());
    }
    @Test
    public void makeScreenshots_monet_system() {
        runTests(getContext(), AppThemes.THEME_SYSTEM, AppThemes.THEME_MONET_SYSTEM, testTextSize(), testLocale(), screenshotTest());
    }

}
