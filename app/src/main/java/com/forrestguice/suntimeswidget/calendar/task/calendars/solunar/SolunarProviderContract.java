// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020 Forrest Guice
    This file is part of SolunarPeriods.

    SolunarPeriods is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SolunarPeriods is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SolunarPeriods.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.calendar.task.calendars.solunar;

public interface SolunarProviderContract
{
    String AUTHORITY = "solunarperiods.calculator.provider";
    String READ_PERMISSION = "suntimes.permission.READ_CALCULATOR";
    String VERSION_NAME = "v0.0.0";
    int VERSION_CODE = 0;

    /**
     * CONFIG
     */
    String COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION = "provider_version";             // String (provider version string)
    String COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE = "provider_version_code";   // int (provider version code)
    String COLUMN_SOLUNAR_CONFIG_APP_VERSION = "app_version";                       // String (app version string)
    String COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE = "app_version_code";             // int (app version code)

    String QUERY_SOLUNAR_CONFIG = "config";
    String[] QUERY_SOLUNAR_CONFIG_PROJECTION = new String[] {
            COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION, COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE,
            COLUMN_SOLUNAR_CONFIG_APP_VERSION, COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE
    };

    /**
     * SOLUNAR INFO
     */
    String COLUMN_SOLUNAR_LOCATION = "location";            // String (locationName)
    String COLUMN_SOLUNAR_LATITUDE = "latitude";            // String (dd)
    String COLUMN_SOLUNAR_LONGITUDE = "longitude";          // String (dd)
    String COLUMN_SOLUNAR_ALTITUDE = "altitude";            // String (meters)
    String COLUMN_SOLUNAR_TIMEZONE = "timezone";            // String (timezoneID)

    String COLUMN_SOLUNAR_DATE = "date";                        // long (timestamp)
    String COLUMN_SOLUNAR_RATING = "rating";                    // double [0,1] percent
    String COLUMN_SOLUNAR_MOON_ILLUMINATION = "moonpos_illum";  // double [0,1]
    String COLUMN_SOLUNAR_MOON_PHASE = "moonphase";             // String (localized display string)

    String COLUMN_SOLUNAR_SUNRISE = "sunrise";                  // long (timestamp) sunrise millis
    String COLUMN_SOLUNAR_SUNSET = "sunset";                    // long (timestamp) sunset millis

    String COLUMN_SOLUNAR_PERIOD_MOONRISE = "moonrise";                     // long (timestamp) moonrise millis; minor period start
    String COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP = "moonrise_overlap";     // int (enum); OVERLAP_NONE(0), OVERLAP_SUNRISE(1), OVERLAP_SUNSET(2)

    String COLUMN_SOLUNAR_PERIOD_MOONSET = "moonset";                       // long (timestamp) moonset millis; minor period start
    String COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP = "moonset_overlap";       // int (enum); OVERLAP_NONE(0), OVERLAP_SUNRISE(1), OVERLAP_SUNSET(2)

    String COLUMN_SOLUNAR_PERIOD_MOONNOON = "moonnoon";                     // long (timestamp) lunar noon millis; major period start
    String COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP = "moonnoon_overlap";     // int (enum); OVERLAP_NONE(0), OVERLAP_SUNRISE(1), OVERLAP_SUNSET(2)

    String COLUMN_SOLUNAR_PERIOD_MOONNIGHT = "moonnight";                   // long (timestamp) lunar midnight millis; major period start
    String COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP = "moonnight_overlap";   // int (enum); OVERLAP_NONE(0), OVERLAP_SUNRISE(1), OVERLAP_SUNSET(2)

    String COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH = "majorlength";     // long (duration) major period millis
    String COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH = "minorlength";     // long (duration) minor period millis

    int OVERLAP_NONE = 0;
    int OVERLAP_SUNRISE = 1;
    int OVERLAP_SUNSET = 2;

    String QUERY_SOLUNAR = "solunar";
    String[] QUERY_SOLUNAR_PROJECTION = new String[] {
            COLUMN_SOLUNAR_DATE, COLUMN_SOLUNAR_RATING,
            COLUMN_SOLUNAR_SUNRISE, COLUMN_SOLUNAR_SUNSET,
            COLUMN_SOLUNAR_MOON_ILLUMINATION, COLUMN_SOLUNAR_MOON_PHASE,
            COLUMN_SOLUNAR_PERIOD_MOONRISE, COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP,
            COLUMN_SOLUNAR_PERIOD_MOONSET, COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP,
            COLUMN_SOLUNAR_PERIOD_MOONNOON, COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP,
            COLUMN_SOLUNAR_PERIOD_MOONNIGHT, COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP,
            COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH, COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH,
            COLUMN_SOLUNAR_LOCATION, COLUMN_SOLUNAR_LATITUDE, COLUMN_SOLUNAR_LONGITUDE, COLUMN_SOLUNAR_ALTITUDE, COLUMN_SOLUNAR_TIMEZONE
    };
}
