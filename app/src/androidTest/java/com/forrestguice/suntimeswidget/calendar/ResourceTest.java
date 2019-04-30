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

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimescalendars.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static junit.framework.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ResourceTest
{
    @Rule
    public ActivityTestRule<SuntimesCalendarActivity> activityRule = new ActivityTestRule<>(SuntimesCalendarActivity.class);

    @Test
    public void test_stringArrays()
    {
        Context context = activityRule.getActivity();
        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            SuntimesCalendarActivity.loadLocale(context, languageTag);
            verify_stringArrayLength("locale_values", R.array.locale_values, "locale_display", R.array.locale_display);
            verify_stringArrayLength("locale_credits", R.array.locale_credits, "locale_display", R.array.locale_display);

            verify_stringArrayLength("calendars_window_values", R.array.calendars_window_values, "calendars_window_display", R.array.calendars_window_display);
        }
    }

    public void verify_stringArrayLength(String tag1, int array1Id, String tag2, int array2Id)
    {
        Context context = activityRule.getActivity();
        String[] a1 = context.getResources().getStringArray(array1Id);
        String[] a2 = context.getResources().getStringArray(array2Id);
        verify_arrayLength(tag1, a1, tag2, a2);
    }

    public void verify_stringArrayLength(String tag1, int array1Id, String tag2, Object[] array2)
    {
        Context context = activityRule.getActivity();
        String[] a1 = context.getResources().getStringArray(array1Id);
        verify_arrayLength(tag1, a1, tag2, array2);
    }

    public void verify_stringArrayLength(String tag1, Object[] array1, String tag2, int array2Id)
    {
        Context context = activityRule.getActivity();
        String[] a2 = context.getResources().getStringArray(array2Id);
        verify_arrayLength(tag1, array1, tag2, a2);
    }

    public void verify_arrayLength(String tag1, Object[] a1, String tag2, Object[] a2)
    {
        assertTrue("The size of " + tag1 + " and " + tag2 + "DOES NOT MATCH! locale: " + Locale.getDefault().toString(),
                a1.length == a2.length);
    }

}
