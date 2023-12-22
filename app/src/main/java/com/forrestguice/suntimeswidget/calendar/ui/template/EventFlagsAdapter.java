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
package com.forrestguice.suntimeswidget.calendar.ui.template;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.CalendarEventFlags;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarDescriptor;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarFactory;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.task.SuntimesCalendar;

import java.lang.ref.WeakReference;

/**
 * EventFlagsAdapter
 */
public class EventFlagsAdapter extends RecyclerView.Adapter<EventFlagsAdapter.EventFlagViewHolder>
{
    protected final WeakReference<Context> contextRef;
    protected String calendar;
    protected SuntimesCalendar calendarObj;
    protected CalendarEventFlags data;

    public EventFlagsAdapter(Context context, String calendar, CalendarEventFlags data)
    {
        this.contextRef = new WeakReference<>(context);
        this.calendar = calendar;
        this.calendarObj = new SuntimesCalendarFactory().createCalendar(context, SuntimesCalendarDescriptor.getDescriptor(context, calendar));
        this.data = data;
    }

    @NonNull
    @Override
    public EventFlagViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(EventFlagViewHolder.getSuggestedLayoutID(), parent, false);
        return new EventFlagViewHolder(contextRef.get(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventFlagViewHolder holder, int position)
    {
        Context context = (contextRef != null ? contextRef.get() : null);
        if (context == null) {
            Log.w("EventsViewHolder", "onBindViewHolder: null context!");
            return;
        }
        if (holder == null) {
            Log.w("EventsViewHolder", "onBindViewHolder: null view holder!");
            return;
        }

        holder.bindDataToPosition(context, position, getLabel(position), data.getValue(position));
        attachListeners(holder, position);
    }

    protected String getLabel(int position) {
        return calendarObj.flagLabel(position);
    }

    @Override
    public void onViewRecycled(@NonNull EventFlagViewHolder holder) {
        detachListeners(holder);
    }

    @Override
    public int getItemCount() {
        return (data != null ? data.getCount() : 0);
    }

    private void attachListeners(final EventFlagViewHolder holder, final int position)
    {
        holder.check_flag.setOnCheckedChangeListener(onEventValueChanged(holder, position));
    }
    private void detachListeners(final EventFlagViewHolder holder)
    {
        holder.check_flag.setOnClickListener(null);
    }

    private CompoundButton.OnCheckedChangeListener onEventValueChanged(final EventFlagViewHolder holder, final int position)
    {
        return new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                triggerOnItemChanged(position, isChecked);
            }
        };
    }

    /**
     * AdapterListener
     */
    public static abstract class AdapterListener
    {
        public void onItemChanged(int position, boolean value) {}
    }

    public AdapterListener adapterListener = null;
    public void setAdapterListener( AdapterListener listener ) {
        this.adapterListener = listener;
    }

    protected void triggerOnItemChanged(int position, boolean value)
    {
        if (adapterListener != null) {
            adapterListener.onItemChanged(position, value);
        }
    }

    /**
     * EventFlagViewHolder
     */
    public static class EventFlagViewHolder extends RecyclerView.ViewHolder
    {
        public CheckBox check_flag;

        public static int getSuggestedLayoutID() {
            return R.layout.layout_item_flag;
        }

        public EventFlagViewHolder(Context context, View itemView)
        {
            super(itemView);
            check_flag = (CheckBox) itemView.findViewById(R.id.check_flag);
        }

        public void bindDataToPosition(Context context, int position, String label, Boolean isChecked) {
            check_flag.setText(label);
            check_flag.setChecked(isChecked);
        }
    }

}


