/*
    Copyright (C) 2023 Forrest Guice
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
package com.forrestguice.suntimeswidget.calendar.ui.reminders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;

import java.lang.ref.WeakReference;

/**
 * ReminderViewAdapter
 */
public class ReminderViewAdapter extends RecyclerView.Adapter<ReminderViewAdapter.ReminderViewHolder>
{
    protected final WeakReference<Context> contextRef;
    protected String calendar;

    public ReminderViewAdapter(Context context, String calendar) {
        this.contextRef = new WeakReference<>(context);
        this.calendar = calendar;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(ReminderViewHolder.getSuggestedLayoutID(), parent, false);
        return new ReminderViewHolder(contextRef.get(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {

        Context context = (contextRef != null ? contextRef.get() : null);
        if (context == null) {
            Log.w("ReminderViewHolder", "onBindViewHolder: null context!");
            return;
        }
        if (holder == null) {
            Log.w("ReminderViewHolder", "onBindViewHolder: null view holder!");
            return;
        }

        Reminder reminder = SuntimesCalendarSettings.loadPrefCalendarReminder(context, calendar, position);
        holder.bindDataToPosition(context, reminder, position);
        attachListeners(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull ReminderViewHolder holder)
    {
        detachListeners(holder);
    }

    @Override
    public int getItemCount() {
        return SuntimesCalendarSettings.loadPrefCalendarReminderCount(contextRef.get(), calendar);
    }

    private void attachListeners(final ReminderViewHolder holder, final int position)
    {
        holder.button_delete.setOnClickListener(onDeleteReminderClicked(position));
        holder.spin_method.setOnItemSelectedListener(onReminderMethodChanged(holder, position));
        holder.spin_minutes.setOnItemSelectedListener(onReminderMinutesChanged(holder, position));
        holder.check_afterEvent.setOnCheckedChangeListener(onReminderAfterChanged(holder, position));
    }
    private void detachListeners(final ReminderViewHolder holder) {
        holder.button_delete.setOnClickListener(null);
        holder.spin_method.setOnItemSelectedListener(null);
        holder.spin_minutes.setOnItemSelectedListener(null);
        holder.check_afterEvent.setOnCheckedChangeListener(null);
    }

    private CompoundButton.OnCheckedChangeListener onReminderAfterChanged(final ReminderViewHolder holder, final int position)
    {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                Context context = contextRef.get();
                boolean after = holder.check_afterEvent.isChecked();
                int minutes = holder.getSelectedMinutes(context);
                int method = holder.getSelectedMethod(context);
                triggerOnItemChanged(position, after, minutes, method);
            }
        };
    }

    private AdapterView.OnItemSelectedListener onReminderMethodChanged(final ReminderViewHolder holder, final int position)
    {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int p, long id)
            {
                Context context = contextRef.get();
                boolean after = holder.check_afterEvent.isChecked();
                int minutes = holder.getSelectedMinutes(context);
                int method = ReminderViewHolder.getSelectedMethod(context, p);
                triggerOnItemChanged(position, after, minutes, method);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    private AdapterView.OnItemSelectedListener onReminderMinutesChanged(final ReminderViewHolder holder, final int position)
    {
        return new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
                Context context = contextRef.get();
                boolean after = holder.check_afterEvent.isChecked();
                int minutes = ReminderViewHolder.getSelectedMinutes(context, p);
                int method = holder.getSelectedMethod(context);
                triggerOnItemChanged(position, after, minutes, method);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }


    private View.OnClickListener onDeleteReminderClicked(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                triggerOnItemDelete(position);
            }
        };
    }

    /**
     * AdapterListener
     */
    public static abstract class AdapterListener
    {
        public void onItemChanged(int position, int minutes, int method) {}
        public void onItemDelete(int position) {}
    }

    public AdapterListener adapterListener = null;
    public void setAdapterListener( AdapterListener listener ) {
        this.adapterListener = listener;
    }

    protected void triggerOnItemChanged(int position, boolean after, int minutes, int method)
    {
        if (adapterListener != null) {
            adapterListener.onItemChanged(position, minutes * (after ? -1 : 1), method);
        }
    }

    protected void triggerOnItemDelete(int position) {
        if (adapterListener != null) {
            adapterListener.onItemDelete(position);
        }
    }

    /**
     * ReminderViewHolder
     */
    public static class ReminderViewHolder extends RecyclerView.ViewHolder
    {
        public Spinner spin_method;
        public Spinner spin_minutes;
        public CheckBox check_afterEvent;
        public ImageButton button_delete;

        public static int getSuggestedLayoutID() {
            return R.layout.layout_item_reminder;
        }

        public ReminderViewHolder(Context context, View itemView)
        {
            super(itemView);

            ArrayAdapter<CharSequence> method_adapter = ArrayAdapter.createFromResource(context, R.array.reminder_method_display, android.R.layout.simple_spinner_item);
            method_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_method = (Spinner) itemView.findViewById(R.id.spin_method);
            spin_method.setAdapter(method_adapter);

            ArrayAdapter<CharSequence> minutes_adapter = ArrayAdapter.createFromResource(context, R.array.reminder_minutes_display, android.R.layout.simple_spinner_item);
            minutes_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_minutes = (Spinner) itemView.findViewById(R.id.spin_minutes);
            spin_minutes.setAdapter(minutes_adapter);

            check_afterEvent = (CheckBox) itemView.findViewById(R.id.check_afterEvent);
            button_delete = (ImageButton) itemView.findViewById(R.id.btn_delete);
        }

        public void bindDataToPosition(Context context, Reminder data, int position)
        {
            setSelectedMinutes(context, data.getMinutes());
            setSelectedMethod(context, data.getMethod());
        }

        public int getSelectedMinutes(Context context) {
            return getSelectedMinutes(context, spin_minutes.getSelectedItemPosition());
        }
        public static int getSelectedMinutes(Context context, int p)
        {
            String[] values = context.getResources().getStringArray(R.array.reminder_minutes_values);
            if (p >= 0 && p < values.length) {
                return Integer.parseInt(values[p]);
            } else return 0;
        }
        public void setSelectedMinutes(Context context, int minutes)
        {
            int p = 0;
            String[] values = context.getResources().getStringArray(R.array.reminder_minutes_values);
            String m = Math.abs(minutes) + "";
            for (int i=0; i<values.length; i++) {
                if (values[i].equals(m)) {
                    p = i;
                    break;
                }
            }
            spin_minutes.setSelection(p, false);
            check_afterEvent.setChecked(minutes < 0);
        }

        public int getSelectedMethod(Context context) {
            return getSelectedMethod(context, spin_method.getSelectedItemPosition());
        }
        public static int getSelectedMethod(Context context, int p)
        {
            String[] values = context.getResources().getStringArray(R.array.reminder_method_values);
            if (p >= 0 && p < values.length) {
                return Integer.parseInt(values[p]);
            } else return 0;
        }
        public void setSelectedMethod(Context context, int method)
        {
            int p = 0;
            String[] values = context.getResources().getStringArray(R.array.reminder_method_values);
            for (int i=0; i<values.length; i++) {
                if (values[i].equals(method + "")) {
                    p = i;
                    break;
                }
            }
            spin_method.setSelection(p, false);
        }

    }

}


