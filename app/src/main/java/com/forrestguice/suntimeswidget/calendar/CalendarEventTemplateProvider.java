/**
    Copyright (C) 2022-2023 Forrest Guice
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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;

import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_CALENDAR;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_DESCRIPTION;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_FLAGS;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_FLAG_LABELS;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_LOCATION;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_STRINGS;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_TITLE;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_CONFIG;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_CONFIG_PROJECTION;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_FLAGS;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_FLAGS_PROJECTION;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_STRINGS;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_STRINGS_PROJECTION;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_TEMPLATE;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_TEMPLATES;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.QUERY_TEMPLATES_PROJECTION;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.VERSION_CODE;
import static com.forrestguice.suntimeswidget.calendar.CalendarEventTemplateContract.VERSION_NAME;

/**
 * CalendarEventTemplateProvider
 */
public class CalendarEventTemplateProvider extends ContentProvider
{
    public static final String TAG = "TemplateProvider";

    private static final int URIMATCH_CONFIG = 0;
    private static final int URIMATCH_TEMPLATES = 10;
    private static final int URIMATCH_TEMPLATE_FOR_CALENDAR = 20;
    private static final int URIMATCH_STRINGS_FOR_CALENDAR = 30;
    private static final int URIMATCH_FLAGS_FOR_CALENDAR = 40;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_CONFIG, URIMATCH_CONFIG);
        uriMatcher.addURI(AUTHORITY, QUERY_TEMPLATES, URIMATCH_TEMPLATES);
        uriMatcher.addURI(AUTHORITY, QUERY_TEMPLATE + "/*", URIMATCH_TEMPLATE_FOR_CALENDAR);
        uriMatcher.addURI(AUTHORITY, QUERY_STRINGS + "/*", URIMATCH_STRINGS_FOR_CALENDAR);
        uriMatcher.addURI(AUTHORITY, QUERY_FLAGS + "/*", URIMATCH_FLAGS_FOR_CALENDAR);
    }

    @Override
    public boolean onCreate()
    {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        Cursor cursor = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_CONFIG:
                Log.i(TAG, "URIMATCH_CONFIG");
                cursor = queryConfig(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_TEMPLATES:
                Log.i(TAG, "URIMATCH_TEMPLATES");
                cursor = queryTemplates(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_TEMPLATE_FOR_CALENDAR:
                Log.i(TAG, "URIMATCH_TEMPLATE");
                cursor = queryTemplate(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_STRINGS_FOR_CALENDAR:
                Log.i(TAG, "URIMATCH_STRINGS");
                cursor = queryStrings(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_FLAGS_FOR_CALENDAR:
                Log.i(TAG, "URIMATCH_FLAGS");
                cursor = queryFlags(uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                Log.e(TAG, "Unrecognized URI! " + uri);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        return 0;
    }

    /**
     * queryConfig
     * @param uri ../config
     * @param projection @see CalendarEventTemplateContract.QUERY_CONFIG_PROJECTION
     * @param selection unused
     * @param selectionArgs unused
     * @param sortOrder unused
     * @return one row of config elements
     */
    public Cursor queryConfig(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_CONFIG_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case COLUMN_CONFIG_PROVIDER_VERSION:
                        row[i] = VERSION_NAME;
                        break;

                    case COLUMN_CONFIG_PROVIDER_VERSION_CODE:
                        row[i] = VERSION_CODE;
                        break;

                    default:
                        row[i] = null;
                        break;
                }
            }
            cursor.addRow(row);

        } else Log.w(TAG, "context is null!");
        return cursor;
    }

    /**
     * queryTemplates
     * @param uri ../templates
     * @param projection @see CalendarEventTemplateContract.QUERY_TEMPLATES_PROJECTION
     * @param selection unused
     * @param selectionArgs unused
     * @param sortOrder unused
     * @return multiple rows (one row per calendar)
     */
    public Cursor queryTemplates(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_TEMPLATES_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            String[] calendars = SuntimesCalendarDescriptor.getCalendars(context);
            for (int i=0; i<calendars.length; i++)
            {
                Object[] row = new Object[columns.length];
                for (int j=0; j<columns.length; j++)
                {
                    switch (columns[j])
                    {
                        case COLUMN_TEMPLATE_CALENDAR:
                            row[j] = calendars[i];
                            break;

                        default:
                            row[j] = null;
                            break;
                    }
                }
                cursor.addRow(row);
            }

        } else Log.w(TAG, "context is null!");
        return cursor;
    }

    /**
     * queryTemplate
     * @param uri ../template/[calendarName]
     * @param projection @see CalendarEventTemplateContract.QUERY_TEMPLATE_PROJECTION
     * @param selection unused
     * @param selectionArgs unused
     * @param sortOrder unused
     * @return a single row of template elements
     */
    public Cursor queryTemplate(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String calendar = uri.getLastPathSegment();
        String[] columns = (projection != null ? projection : QUERY_TEMPLATES_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            Object[] row = new Object[columns.length];
            for (int j=0; j<columns.length; j++)
            {
                switch (columns[j])
                {
                    case COLUMN_TEMPLATE_CALENDAR:
                        row[j] = calendar;
                        break;

                    case COLUMN_TEMPLATE_TITLE:
                        row[j] = SuntimesCalendarSettings.loadPrefCalendarTemplateTitle(context, calendar);
                        break;

                    case COLUMN_TEMPLATE_DESCRIPTION:
                        row[j] = SuntimesCalendarSettings.loadPrefCalendarTemplateDesc(context, calendar);
                        break;

                    case COLUMN_TEMPLATE_LOCATION:
                        row[j] = SuntimesCalendarSettings.loadPrefCalendarTemplateLocation(context, calendar);
                        break;

                    default:
                        row[j] = null;
                        break;
                }
            }
            cursor.addRow(row);

        } else Log.w(TAG, "context is null!");
        return cursor;
    }

    /**
     * queryStrings
     * @param uri ../strings/[calendarName]
     * @param projection @see CalendarEventTemplateContract.QUERY_STRINGS_PROJECTION
     * @param selection unused
     * @param selectionArgs unused
     * @param sortOrder unused
     * @return multiple rows, ordered (one row per string), or an empty cursor is undefined
     */
    public Cursor queryStrings(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_STRINGS_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            String calendar = uri.getLastPathSegment();
            String[] strings = SuntimesCalendarSettings.loadPrefCalendarStrings(context, calendar, new CalendarEventStrings()).getValues();

            for (int i=0; i<strings.length; i++)
            {
                Object[] row = new Object[columns.length];
                for (int j=0; j<columns.length; j++)
                {
                    switch (columns[j])
                    {
                        case COLUMN_TEMPLATE_STRINGS:
                            row[j] = strings[i];
                            break;

                        default:
                            row[j] = null;
                            break;
                    }
                }
                cursor.addRow(row);
            }

        } else Log.w(TAG, "context is null!");
        return cursor;
    }

    /**
     * queryFlags
     * @param uri ../flags/[calendarName]
     * @param projection @see CalendarEventTemplateContract.QUERY_FLAGS_PROJECTION
     * @param selection unused
     * @param selectionArgs unused
     * @param sortOrder unused
     * @return multiple rows, ordered (one row per flag), or an empty cursor is undefined
     */
    public Cursor queryFlags(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_FLAGS_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            String calendar = uri.getLastPathSegment();
            SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
            if (descriptor != null)
            {
                SuntimesCalendar calendarObj = new SuntimesCalendarFactory().createCalendar(context, descriptor);
                if (calendarObj != null)
                {
                    boolean[] flags = SuntimesCalendarSettings.loadPrefCalendarFlags(context, calendar, calendarObj.defaultFlags()).getValues();
                    for (int i=0; i<flags.length; i++)
                    {
                        Object[] row = new Object[columns.length];
                        for (int j=0; j<columns.length; j++)
                        {
                            switch (columns[j])
                            {
                                case COLUMN_TEMPLATE_FLAGS:
                                    row[j] = flags[i];
                                    break;

                                case COLUMN_TEMPLATE_FLAG_LABELS:
                                    row[j] = calendarObj.flagLabel(i);
                                    break;

                                default:
                                    row[j] = null;
                                    break;
                            }
                        }
                        cursor.addRow(row);
                    }
                } else Log.w(TAG, "failed to initialize calendar \"" + calendar + "\"!");
            } else Log.w(TAG, "calendar \"" + calendar + "\" not found!");
        } else Log.w(TAG, "context is null!");
        return cursor;
    }

}
