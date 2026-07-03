/**
    Copyright (C) 2024 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.calendar.ical;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.FragmentManager;
import android.util.Log;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettingsFactory;
import com.forrestguice.suntimeswidget.views.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ICalDialogs
{
    protected WeakReference<Fragment> fragmentRef;
    protected ICalDialogsListener listener;

    public ICalDialogs(Fragment parent, FragmentManager fragmentManager, SuntimesCalendarSettings settings, ICalDialogsListener listener) {
        fragmentRef = new WeakReference<>(parent);
        this.supportFragments = fragmentManager;
        this.settings = settings;
        this.listener = listener;
    }

    private FragmentManager supportFragments;
    public void setSupportFragmentManager(FragmentManager fragments) {
        supportFragments = fragments;
    }
    public FragmentManager getSupportFragmentManager() {
        return supportFragments;
    }

    private SuntimesCalendarSettings settings;
    public SuntimesCalendarSettings getSettings() {
        return ((settings != null) ? settings : SuntimesCalendarSettingsFactory.createSettings());
    }
    public void setSettings(SuntimesCalendarSettings value) {
        settings = value;
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////

    public void onResume()
    {
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments == null) {
            return;
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("DEBUG", "onActivityResult");
        switch (requestCode)
        {
            case SAVE_AS_ICAL_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Fragment fragment = fragmentRef.get();
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null && fragment != null) {
                        saveCalendarToFile(fragment.getActivity(), requests.get(requestCode), uri);
                    }
                }
                return true;

            default:
                return false;
        }
    }

    public void updateDialogs()
    {
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments == null) {
            return;
        }
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////

    public static final int SAVE_AS_ICAL_REQUEST = 100;
    protected HashMap<Integer, String> requests = new HashMap<>();

    public void showSaveToFileDialog(String calendar)
    {
        requests.put(SAVE_AS_ICAL_REQUEST, calendar);
        fragmentRef.get().startActivityForResult(ExportTask.getCreateFileIntent(calendar + ICalFormat.ICS_FILEEXT, ICalFormat.ICS_MIMETYPE), SAVE_AS_ICAL_REQUEST);
    }

    protected boolean saveCalendarToFile(final Context context, final String calendar, Uri uri) {
        if (listener != null) {
            return listener.saveCalendarToFile(context, calendar, uri);
        } else return saveCalendarToFile0(context, calendar, uri);
    }

    protected ICalExportTask exportTask = null;
    protected boolean saveCalendarToFile0(final Context context, final String calendar, Uri uri)
    {
        SuntimesCalendarDescriptor descriptor = SuntimesCalendarDescriptor.getDescriptor(context, calendar);
        if (descriptor == null) {
            Log.e("ICalDialogs", "saveCalendarToFile: null descriptor! " + calendar);
            return false;
        }

        exportTask = new ICalExportTask(context, uri);
        exportTask.setCalendar(new SuntimesCalendarFactory().createCalendar(context, descriptor, getSettings()));
        exportTask.setTaskListener(new ExportTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                if (listener != null) {
                    listener.showProgress(calendar, true);
                }
            }

            @Override
            public void onFinished(ExportTask.ExportResult result)
            {
                if (listener != null) {
                    listener.showProgress(calendar, false);
                }
                Toast.makeText(context, "TODO: finished: " + result.getResult(), Toast.LENGTH_SHORT).show();  // TODO
                // show snackbar completed message
            }
        });
        exportTask.execute();
        return true;
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////

    public interface ICalDialogsListener {
        void showProgress(final String calendar, final boolean value);
        boolean saveCalendarToFile(final Context context, final String calendar, final Uri uri);
    }

}
