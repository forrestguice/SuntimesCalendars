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

import android.graphics.Color;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CalendarAdapterTest
{
    private static final String CALENDAR_TEST0 = "test0";

    @Rule
    public ActivityTestRule<SuntimesCalendarActivity> activityRule = new ActivityTestRule<>(SuntimesCalendarActivity.class);

    @Before
    public void initAdapter() {
        SuntimesCalendarAdapter adapter = new SuntimesCalendarAdapter(activityRule.getActivity().getContentResolver(), SuntimesCalendarDescriptor.getCalendars(activityRule.getActivity()));
    }
    private SuntimesCalendarAdapter adapter;

    @Test
    public void test_createCalendar()
    {
        assertNotNull(adapter);
        adapter.removeCalendars();

        assertTrue("hasCalendars should return false", !adapter.hasCalendars(activityRule.getActivity()));
        long calendarID = adapter.queryCalendarID(CALENDAR_TEST0);
        assertTrue(CALENDAR_TEST0 + " shouldn't exist", (calendarID == -1));
        assertTrue(CALENDAR_TEST0 + " shouldn't exist", !adapter.hasCalendar(CALENDAR_TEST0));

        adapter.createCalendar(CALENDAR_TEST0, "test0", Color.RED);
        calendarID = adapter.queryCalendarID(CALENDAR_TEST0);
        assertTrue(CALENDAR_TEST0 + " should exist", (calendarID != -1));
        assertTrue(CALENDAR_TEST0 + " should exist", adapter.hasCalendar(CALENDAR_TEST0));

        Calendar dtStart = Calendar.getInstance();
        Calendar dtEnd = Calendar.getInstance();
        int n = 7;
        for (int i=0; i<n; i++)
        {
            dtStart.add(Calendar.DATE, i);
            dtEnd.setTimeInMillis(dtStart.getTimeInMillis() + 1000 * 60 * 60);
            adapter.createCalendarEvent(calendarID, "event " + i, "desc", dtStart, dtEnd);
        }

        Calendar dtStart0 = Calendar.getInstance();
        dtStart0.add(Calendar.DATE, -1);
        int c = adapter.removeCalendarEventsBefore(calendarID, dtStart0.getTimeInMillis());
        assertTrue("0 events should have been removed; " + c, (c == 0));

        dtEnd.add(Calendar.DATE, 1);
        c = adapter.removeCalendarEventsBefore(calendarID, dtEnd.getTimeInMillis());
        assertTrue(n + " events should have been removed; + c", (c == n));

        c = adapter.removeCalendarEventsBefore(calendarID, dtEnd.getTimeInMillis());
        assertTrue("0 events should have been removed; + c", (c == 0));

        assertTrue(CALENDAR_TEST0 + " should be removed", adapter.removeCalendar(CALENDAR_TEST0));
        calendarID = adapter.queryCalendarID(CALENDAR_TEST0);
        assertTrue(CALENDAR_TEST0 + " shouldn't exist", (calendarID == -1));
        assertTrue(CALENDAR_TEST0 + " shouldn't exist", !adapter.hasCalendar(CALENDAR_TEST0));
    }

}
