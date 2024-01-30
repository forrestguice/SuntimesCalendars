/*
    Copyright (C) 2020-2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.calendar.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.forrestguice.suntimescalendars.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Utils
{
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source)
    {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else return Html.fromHtml(source);
    }

    protected static String strDegSymbol = "°";
    protected static String strAltSymbol = "∠";
    protected static String strRaSymbol = "α";
    protected static String strDecSymbol = "δ";
    protected static String strDegreesFormat = "%1$s\u00B0";
    protected static String strDirectionFormat = "%1$s\u00A0%2$s";
    protected static String strElevationFormat = "%1$s%2$s";
    protected static String strDeclinationFormat = "%1$s %2$s";
    protected static String strRaFormat = "%1$s %2$s";
    protected static String strDistanceFormatKm = "%1$s km";
    protected static String strPercentFormat = "%1$s %%";

    private static NumberFormat formatter = NumberFormat.getInstance();

    public static String formatAsDegrees(@Nullable Double value, int places)
    {
        if (value == null) {
            return "";
        }
        formatter.setMinimumFractionDigits(places);
        formatter.setMaximumFractionDigits(places);
        return String.format(strDegreesFormat, formatter.format(value));
    }

    public static String formatAsRightAscension(Double degrees, int places)
    {
        if (degrees == null) {
            return "";
        }
        return String.format(strRaFormat, formatAsDegrees(degrees, places), strRaSymbol);
    }

    public static String formatAsDeclination(Double degrees, int places)
    {
        if (degrees == null) {
            return "";
        }
        return String.format(strDeclinationFormat, formatAsDegrees(degrees, places), strDecSymbol);
    }

    public static String formatAsDirection(Double degrees, int places)
    {
        if (degrees == null) {
            return "";
        }
        String degreeString = formatAsDegrees(degrees, places);
        Utils.CardinalDirection direction = Utils.CardinalDirection.getDirection(degrees);
        return formatAsDirection(degreeString, direction.getShortDisplayString());
    }
    public static String formatAsDirection(String degreeString, String directionString) {
        return String.format(strDirectionFormat, degreeString, directionString);
    }

    public static String formatAsElevation(Double degrees, int places) {
        return String.format(strElevationFormat, formatAsDegrees(degrees, places), strAltSymbol);
    }

    public static String formatAsDistanceKm(Double value, int places)
    {
        if (value == null) {
            return "";
        }
        formatter.setMinimumFractionDigits(places);
        formatter.setMaximumFractionDigits(places);
        return String.format(strDistanceFormatKm, formatter.format(value));
    }

    public static String formatAsPercent(Double value, int places)
    {
        if (value == null) {
            return "";
        }
        formatter.setMinimumFractionDigits(places);
        formatter.setMaximumFractionDigits(places);
        return String.format(strPercentFormat, formatter.format(value * 100));
    }

    /**
     * CardinalDirection
     */
    public static enum CardinalDirection
    {
        NORTH(1,      "N",   "North"              , 0.0),
        NORTH_NE(2,   "NNE", "North North East"   , 22.5),
        NORTH_E(3,    "NE",  "North East"         , 45.0),

        EAST_NE(4,    "ENE", "East North East"    , 67.5),
        EAST(5,       "E",   "East"               , 90.0),
        EAST_SE(6,    "ESE", "East South East"    , 112.5),

        SOUTH_E(7,    "SE",  "South East"         , 135.0),
        SOUTH_SE(8,   "SSE", "South South East"   , 157.5),
        SOUTH(9,      "S",   "South"              , 180.0),
        SOUTH_SW(10,  "SSW", "South South West"   , 202.5),
        SOUTH_W(11,   "SW",  "South West"         , 225.0),

        WEST_SW(12,   "WSW", "West South West"    , 247.5),
        WEST(13,      "W",   "West"               , 270.0),
        WEST_NW(14,   "WNW", "West North West"    , 292.5),

        NORTH_W(15,   "NW",  "North West"         , 315.0),
        NORTH_NW(16,  "NNW", "North North West"   , 337.5),
        NORTH2(1,     "N",   "North"              , 360.0);

        private int pointNum;
        private String shortDisplayString;
        private String longDisplayString;
        private double degrees;

        private CardinalDirection(int pointNum, String shortDisplayString, String longDisplayString, double degrees)
        {
            this.pointNum = pointNum;
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
            this.degrees = degrees;
        }

        public static CardinalDirection getDirection(double degrees)
        {
            if (degrees > 360)
                degrees = degrees % 360;

            while (degrees < 0)
                degrees += 360;

            CardinalDirection result = NORTH;
            double least = Double.MAX_VALUE;
            for (CardinalDirection direction : values())
            {
                double directionDegrees = direction.getDegress();
                double diff = Math.abs(directionDegrees - degrees);
                if (diff < least)
                {
                    least = diff;
                    result = direction;
                }
            }
            return result;
        }

        public String toString()
        {
            return shortDisplayString;
        }

        public double getDegress()
        {
            return degrees;
        }

        public int getPoint()
        {
            return pointNum;
        }

        public String getShortDisplayString()
        {
            return shortDisplayString;
        }

        public String getLongDisplayString()
        {
            return longDisplayString;
        }

        public void setDisplayStrings(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public static void initDisplayStrings( Context context )
        {
            Resources res = context.getResources();
            String[] modes_short = res.getStringArray(R.array.directions_short);
            String[] modes_long = res.getStringArray(R.array.directions_long);
            if (modes_long.length != modes_short.length)
            {
                Log.e("initDisplayStrings", "The size of directions_short and solarevents_long DOES NOT MATCH!");
                return;
            }

            CardinalDirection[] values = values();
            if (modes_long.length != values.length)
            {
                Log.e("initDisplayStrings", "The size of directions_long and SolarEvents DOES NOT MATCH!");
                return;
            }

            for (int i = 0; i < values.length; i++)
            {
                values[i].setDisplayStrings(modes_short[i], modes_long[i]);
            }
        }
    }

    public static void initDisplayStrings( Context context )
    {
        CardinalDirection.initDisplayStrings(context);

        strDegSymbol = context.getString(R.string.degrees_symbol);                // "%";
        strAltSymbol = context.getString(R.string.altitude_symbol);               // "∠";
        strRaSymbol = context.getString(R.string.rightascension_symbol);          // "α";
        strDecSymbol = context.getString(R.string.declination_symbol);            //"δ";
        strDegreesFormat = context.getString(R.string.degrees_format);            // "%1$s\u00B0";
        strDirectionFormat = context.getString(R.string.direction_format);        // "%1$s\u00A0%2$s";
        strElevationFormat = context.getString(R.string.elevation_format);        // "%1$s%2$s";
        strDeclinationFormat = context.getString(R.string.declination_format);    // "%1$s %2$s";
        strRaFormat = context.getString(R.string.rightascension_format);          //"%1$s %2$s";
        strDistanceFormatKm = context.getString(R.string.distance_format);
        strPercentFormat = context.getString(R.string.percent_format);            // "%1$s %%";
    }

}
