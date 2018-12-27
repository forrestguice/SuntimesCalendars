### ~

* adds individual "enabled" prefs for each calendar (Solstices/Equinoxes, Moon Phases). 
* changes the behavior of the "Calendar Integration" pref to match calendar state (vs desired state); integration is "enabled" when the Suntimes local account has one or more calendars; existing calendars (and prefs) should now be preserved when updating (or removing / re-adding) the app. 
* changes the strategy used when initializing calendars; existing calendars are no longer cleared prior to (re)adding; subsequent calls to "add" will instead "update" a calendar (insert or replace entries).       
* misc improvements to permissions handling; more robust; support for actions on individual calendars.   

### v0.1.1 (2018-12-12)
* fixes broken build (jcenter fails to resolve deps).
* updates Android gradle plugin to `com.android.tools.build:gradle:3.0.0` (#2).
* updates the gradle wrapper to `gradle 4.1` (#2).

### v0.1.0 (2018-12-06)
* refactors "Calendar Integration" to use the ContentProvider supplied by Suntimes.
* updates copyright notices ("SuntimesWidget" changed to "SuntimesCalendars"), actual copyright remain unchanged; Copyright (C) 2018 Forrest Guice.
* initial commit; imports existing GPLv3 code from `com.forrestguice.suntimeswidget.calendar` (from Suntimes v0.9.4, commit 1ef4dcd4009b8adedd4538ccbeddb07564111613).
