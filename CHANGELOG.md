### ~

### v0.6.0 (2023-12-30)
* adds support for reminder notifications (#51).
* adds support for event selection; include all or omit some calendar events.
* adds support for templates; customize event text and formatting.
* adds "Golden Hour", "Blue Hour", and "Daylight" calendars (#55).
* adds "cross-quarter days" to the Solstice calendar (#56).
* adds themed icon (Android 13+), and other miscellaneous UI improvements.
* adds permission `android.permission.FOREGROUND_SERVICE`; calendar updates rely on a foreground service. [permission]
* updates build; targetSdkVersion from 25 to 28; minSdkVersion from 11 to 14; support libraries to v28.0.0.

### v0.5.7 (2023-02-10)
* adds support for high contrast themes and text size (Suntimes v0.15.0+).
* updates Suntimes CalculatorProviderContract; from v2 to v5.
* updates translation to Norwegian (nb) (#52 by FTno).

### v0.5.6 (2022-12-05)
* adds support for system dark mode (night mode).
* fixes bug where Toasts are unreadable (white-on-white); Android 13 (api33+).
* updates build; gradle wrapper to `gradle-5.0`.
* updates translation to Norwegian (nb) (#49 by FTno).

### v0.5.5 (2021-11-15)
* updates translations to Polish (pl) and Esperanto (eo) (#44 by Verdulo).
* fixes "app crash when clicking the `Open Calendar` button" (#43).

### v0.5.4 (2021-06-16)
* updates translation to German (de) (#41 by wolkenschieber).

### v0.5.3 (2021-03-09)
* adds translation to Russian (ru) (contributed by ddanilov) (#39).
* updates translations to Catalan (ca) and Spanish (es) (#37, #38 by gnusuari0).

### v0.5.2 (2021-02-09)
* updates translation to Dutch (nl) (#33 by Joppla).
* updates translations to Catalan (ca) and Spanish (es) (#34, #35 by gnusuari0).

### v0.5.1 (2021-01-30)
* adds translation to Dutch (nl) (contributed by Joppla) (#30).

### v0.5.0 (2020-11-20)
* adds support for add-on calendars; add-on apps may now declare their own calendars (e.g. https://github.com/forrestguice/SolunarPeriods). These calendars will show up in the list automatically when available.
* misc UI changes; enhances "Calendar Window" preference, adds ActionBar, adds "Open Calendar" menu item, moves "About" to overflow menu.
* refactors CalendarTask and supporting classes (rewrite).

### v0.4.2 (2020-09-24)
* enhances color selection; use external color picker if available (#25) (requires Suntimes v0.13.0+).
* fixes bug that prevents adding Moon Apsis calendar. 

### v0.4.1 (2020-04-22)
* updates translations to Polish (pl) and Esperanto (eo) (#23 by Verdulo).

### v0.4.0 (2020-02-14)
* adds "Moon Apsis" calendar (apogee, perigee).
* adds ability to change a calendar's color.
* adds labels for "super full moon", "super new moon", "micro full moon", and "micro new moon".
* adds moon distance (km) to event description for new and full moons.
* changes event descriptions to show the location label only (latitude and longitude are now omitted for privacy reasons).
* adds location labels when adding or listing existing calendars.
* adds a "cancel" button to the progress dialog (stops update early).
* improves performance when adding calendars (bulkInsert) (#9).
* improves language resolution for Spanish locales (`es-rES` moved to `es`).
* updates build; Android gradle plugin version updated to `com.android.tools.build:gradle:3.1.2`, gradle wrapper to `gradle-4.4`, and buildToolsVersion to `27.0.3`.

### v0.3.2 (2019-08-01)
* improves support for polar regions (polar night). 
* fixes bug "Twilight calendars are added with entries back to 1 Jan 1970" (#16).

### v0.3.1 (2019-04-30)
* adds translation to Brazilian Portuguese (pt-br) (contributed by Neto Silva) (#14).
* updates translations to Polish (pl) and Esperanto (eo) (#13 by Verdulo).
* improves the About Dialog (better translation credits).

### v0.3.0 (2019-04-09)
* adds "moon" calendar (moonrise, moonset) (#9).
* adds "astronomical twilight", "nautical twilight", and "civil twilight" calendars (sunrise, sunset) (#9).
* enhances the progress UI; improved dialog and notifications.
* updates translations to Polish (pl) and Esperanto (eo) (#12 by Verdulo).
* now requires Suntimes v0.10.3+ (previously v0.10.0).

### v0.2.0 (2019-01-02)
* adds individual "enabled" prefs for each calendar (Solstices/Equinoxes, Moon Phases) (#3).
* fixes bug "missing calendars/events when closing app while task is still running" (#4); moves calendar notifications into a foreground service. 
* changes the "Calendar Integration" pref to match calendar state (vs desired state); existing calendars (and prefs) should be preserved when updating (or removing / re-adding) the app. 
* changes the strategy used when initializing calendars; existing calendars are no longer cleared prior to (re)adding; subsequent calls to "add" instead "update" a calendar (insert, replace).          
* changes when permissions are requested; adds a request on first launch (or if data is cleared) before allowing access to remaining UI; permissions are used to recover calendars from previous installations.
* misc improvements to permissions handling; more robust; support for actions on individual calendars.

### v0.1.1 (2018-12-12)
* fixes broken build (jcenter fails to resolve deps).
* updates Android gradle plugin to `com.android.tools.build:gradle:3.0.0` (#2).
* updates the gradle wrapper to `gradle 4.1` (#2).

### v0.1.0 (2018-12-06)
* refactors "Calendar Integration" to use the ContentProvider supplied by Suntimes.
* updates copyright notices ("SuntimesWidget" changed to "SuntimesCalendars"), actual copyright remain unchanged; Copyright (C) 2018 Forrest Guice.
* initial commit; imports existing GPLv3 code from `com.forrestguice.suntimeswidget.calendar` (from Suntimes v0.9.4, commit 1ef4dcd4009b8adedd4538ccbeddb07564111613).
