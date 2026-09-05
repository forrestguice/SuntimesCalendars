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

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import com.forrestguice.suntimescalendars.BuildConfig;
import com.forrestguice.suntimescalendars.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewAssertionHelper.assertShown;
import static org.hamcrest.CoreMatchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CalendarActivityTest extends TestBase
{
    @Rule
    public ActivityTestRule<SuntimesCalendarActivity> activityRule = new ActivityTestRule<>(SuntimesCalendarActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
    }

    /**
     * CalendarActivityTestCase
     */
    public static abstract class CalendarActivityTestCase extends BaseActivityTest implements ActivityTest
    {
        private final ActivityTestRule<SuntimesCalendarActivity> activityRule;
        public CalendarActivityTestCase(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            this.activityRule = activityRule;
        }

        @Override
        public abstract void runTest(Activity activity, String tag);

        @Override
        public ActivityTestRule<?> activityRule() {
            return activityRule;
        }
    }

    public static String tag() {
        return TAG + "_v" + BuildConfig.VERSION_NAME;
    }

    @Test
    public void test_CalendarActivity_about() {
        runTests(getContext(), testAppThemeBase(), testAppTheme(), testTextSize(), testLocale(),
                new Test_CalendarActivity_About(activityRule));
    }
    public static class Test_CalendarActivity_About extends CalendarActivityTestCase
    {
        public Test_CalendarActivity_About(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            super(activityRule);
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            CalendarActivityRobot robot = new CalendarActivityRobot()
                    .assertActivityShown(activity)
                    .captureScreenshot(activity, tag, "activity")

                    .clickOverflowMenu(activity)
                    .assertOverflowMenuShown()
                    .captureScreenshot(activity, tag, "menu")

                    .clickOverflowMenu_about()
                    .captureScreenshot(activity, tag, "dialog_about")
                    .assertAboutShown();
        }
    }

    @Test
    public void test_CalendarActivity_menu_options() {
        runTests(getContext(), testAppThemeBase(), testAppTheme(), testTextSize(), testLocale(),
                new Test_CalendarActivity_Menu_Options(activityRule));
    }
    public static class Test_CalendarActivity_Menu_Options extends CalendarActivityTestCase
    {
        public Test_CalendarActivity_Menu_Options(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            super(activityRule);
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            CalendarActivityRobot robot = new CalendarActivityRobot()
                    .assertActivityShown(activity)
                    .captureScreenshot(activity, tag, "activity")

                    .clickCalendarIcon(R.string.calendar_daylight_displayName)
                    .captureScreenshot(activity, tag, "menu_options_daylight")
                    .assertCalendarOptionsMenuShown();
        }
    }

    @Test
    public void test_CalendarActivity_preview() {
        runTests(getContext(), testAppThemeBase(), testAppTheme(), testTextSize(), testLocale(),
                new Test_CalendarActivity_Preview(activityRule));
    }
    public static class Test_CalendarActivity_Preview extends CalendarActivityTestCase
    {
        public Test_CalendarActivity_Preview(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            super(activityRule);
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            CalendarActivityRobot robot = new CalendarActivityRobot()
                    .assertActivityShown(activity)
                    .captureScreenshot(activity, tag, "activity")

                    .clickCalendarIcon(R.string.calendar_daylight_displayName)
                    .clickCalendarOptionsMenu_preview()
                    .captureScreenshot(activity, tag, "dialog_preview_daylight")
                    .assertPreviewDialogShown_daylight()
                    .clickDialogButton_back();
        }
    }

    @Test
    public void test_CalendarActivity_title() {
        runTests(getContext(), testAppThemeBase(), testAppTheme(), testTextSize(), testLocale(),
                new Test_CalendarActivity_Title(activityRule));
    }
    public static class Test_CalendarActivity_Title extends CalendarActivityTestCase
    {
        public Test_CalendarActivity_Title(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            super(activityRule);
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            CalendarActivityRobot robot = new CalendarActivityRobot()
                    .assertActivityShown(activity)
                    .captureScreenshot(activity, tag, "activity")

                    .clickCalendarIcon(R.string.calendar_daylight_displayName)
                    .assertCalendarOptionsMenuShown()

                    .clickCalendarOptionsMenu_title()
                    .captureScreenshot(activity, tag, "dialog_title_daylight")
                    .assertTitleDialogShown();
        }
    }

    @Test
    public void test_CalendarActivity_reminders() {
        runTests(getContext(), testAppThemeBase(), testAppTheme(), testTextSize(), testLocale(),
                new Test_CalendarActivity_Reminders(activityRule));
    }
    public static class Test_CalendarActivity_Reminders extends CalendarActivityTestCase
    {
        public Test_CalendarActivity_Reminders(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            super(activityRule);
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            CalendarActivityRobot robot = new CalendarActivityRobot()
                    .assertActivityShown(activity)
                    .captureScreenshot(activity, tag, "activity")

                    .clickCalendarIcon(R.string.calendar_daylight_displayName)
                    .assertCalendarOptionsMenuShown()

                    .clickCalendarOptionsMenu_reminders()
                    .captureScreenshot(activity, tag, "dialog_reminders_daylight")
                    .assertReminderDialogShown();
        }
    }

    @Test
    public void test_CalendarActivity_flags() {
        runTests(getContext(), testAppThemeBase(), testAppTheme(), testTextSize(), testLocale(),
                new Test_CalendarActivity_Flags(activityRule));
    }
    public static class Test_CalendarActivity_Flags extends CalendarActivityTestCase
    {
        public Test_CalendarActivity_Flags(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            super(activityRule);
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            CalendarActivityRobot robot = new CalendarActivityRobot()
                    .assertActivityShown(activity)
                    .captureScreenshot(activity, tag, "activity")

                    .clickCalendarIcon(R.string.calendar_daylight_displayName)
                    .assertCalendarOptionsMenuShown()

                    .clickCalendarOptionsMenu_flags()
                    .captureScreenshot(activity, tag, "dialog_flags_daylight")
                    .assertFlagsDialogShown()
                    .clickDialogButton_back();
        }
    }

    @Test
    public void test_CalendarActivity_template() {
        runTests(getContext(), testAppThemeBase(), testAppTheme(), testTextSize(), testLocale(),
                new Test_CalendarActivity_Template(activityRule));
    }
    public static class Test_CalendarActivity_Template extends CalendarActivityTestCase
    {
        public Test_CalendarActivity_Template(ActivityTestRule<SuntimesCalendarActivity> activityRule) {
            super(activityRule);
        }

        @Override
        public void runTest(Activity activity, String tag)
        {
            CalendarActivityRobot robot = new CalendarActivityRobot()
                    .assertActivityShown(activity)
                    .captureScreenshot(activity, tag, "activity")

                    .clickCalendarIcon(R.string.calendar_daylight_displayName)
                    .assertCalendarOptionsMenuShown()

                    .clickCalendarOptionsMenu_template()
                    .captureScreenshot(activity, tag, "dialog_template_daylight")
                    .assertTemplateDialogShown()

                    .clickDialogButton_eventStrings()
                    .captureScreenshot(activity, tag, "dialog_strings_daylight")
                    .assertStringsDialogShown();
        }
    }

    /**
     * CalendarActivityRobot
     */
    public static class CalendarActivityRobot extends ActivityRobot<CalendarActivityRobot>
    {
        public CalendarActivityRobot() {
            setRobot(this);
        }

        public CalendarActivityRobot assertActivityShown(Context context) {
            assertActionBar_homeButtonShown(true);
            onView(withText(R.string.configLabel_calendars_enabled)).check(assertShown);
            return this;
        }

        public CalendarActivityRobot clickCalendar(int textResId)
        {
            onView(withText(textResId)).perform(click());
            return robot;
        }

        public CalendarActivityRobot clickCalendarIcon(int textResId)
        {
            onView(allOf(
                    withId(R.id.button_options),
                    isDescendantOfA(allOf(
                            withId(R.id.icon_frame),
                            hasSibling(hasDescendant(withText(textResId))))
                    ))
            ).perform(click());
            return robot;
        }

        public CalendarActivityRobot assertCalendarDialogShown_daylight() {
            assertCalendarDialogShown();
            //onView(withText(R.string.calendar_daylight_displayName)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot clickDialogButton_options() {
            onView(withText(R.string.configLabel_options)).perform(click());
            return robot;
        }

        public CalendarActivityRobot clickCalendarOptionsMenu_preview() {
            onView(withText(R.string.action_preview)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public CalendarActivityRobot clickCalendarOptionsMenu_title() {
            onView(withText(R.string.title_dialog_msg)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public CalendarActivityRobot clickCalendarOptionsMenu_color() {
            onView(withText(R.string.color_dialog_msg)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public CalendarActivityRobot clickCalendarOptionsMenu_reminders() {
            onView(withText(R.string.reminder_dialog_msg)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public CalendarActivityRobot clickCalendarOptionsMenu_flags() {
            onView(withText(R.string.flag_dialog_msg)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public CalendarActivityRobot clickCalendarOptionsMenu_template() {
            onView(withText(R.string.template_dialog_msg)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public CalendarActivityRobot clickDialogButton_eventStrings() {
            onView(withText(R.string.templatestrings_dialog_msg)).perform(click());
            return robot;
        }
        public CalendarActivityRobot clickDialogButton_addReminder() {
            onView(withText(R.string.action_add_reminder)).perform(click());
            return robot;
        }

        public CalendarActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.about_dialog_title)).perform(pressBack());
            return this;
        }

        public CalendarActivityRobot clickOverflowMenu_about() {
            onView(withText(R.string.about_dialog_title)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }

        public CalendarActivityRobot assertAboutShown() {
            onView(withText(R.string.app_name)).check(assertShown);
            //onView(withText(R.string.app_legal1)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertCalendarDialogShown()
        {
            onView(withText(R.string.dialog_ok)).check(assertShown);
            onView(withText(R.string.dialog_cancel)).check(assertShown);
            onView(withText(R.string.configLabel_options)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertCalendarOptionsMenuShown()
        {
            onView(withText(R.string.action_preview)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.title_dialog_msg)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.color_dialog_msg)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.reminder_dialog_msg)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.flag_dialog_msg)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.template_dialog_msg)).inRoot(isPlatformPopup()).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertOverflowMenuShown() {
            onView(withText(R.string.action_openCalendar1)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.about_dialog_title)).inRoot(isPlatformPopup()).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertPreviewDialogShown_daylight()
        {
            assertPreviewDialogShown();
            onView(withText(R.string.calendar_daylight_displayName)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertPreviewDialogShown() {
            onView(withId(R.id.back_button)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertTitleDialogShown() {
            onView(withText(R.string.title_dialog_msg)).check(assertShown);
            onView(withText(R.string.dialog_ok)).check(assertShown);
            onView(withText(R.string.dialog_cancel)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertReminderDialogShown() {
            onView(withText(R.string.reminder_dialog_msg)).check(assertShown);
            onView(withText(R.string.action_add_reminder)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertTemplateDialogShown() {
            onView(withText(R.string.template_dialog_msg)).check(assertShown);
            onView(withText(R.string.templatestrings_dialog_msg)).check(assertShown);
            onView(withId(R.id.accept_button)).check(assertShown);
            onView(withId(R.id.back_button)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertStringsDialogShown() {
            onView(withText(R.string.templatestrings_dialog_msg)).check(assertShown);
            onView(withId(R.id.accept_button)).check(assertShown);
            onView(withId(R.id.back_button)).check(assertShown);
            return robot;
        }

        public CalendarActivityRobot assertFlagsDialogShown() {
            onView(withText(R.string.flag_dialog_msg)).check(assertShown);
            onView(withText(R.string.templatestrings_dialog_msg)).check(assertShown);
            onView(withId(R.id.accept_button)).check(assertShown);
            onView(withId(R.id.back_button)).check(assertShown);
            return robot;
        }
    }
}
