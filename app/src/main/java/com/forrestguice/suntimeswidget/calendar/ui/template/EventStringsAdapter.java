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
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.forrestguice.suntimescalendars.R;
import com.forrestguice.suntimeswidget.calendar.CalendarEventStrings;

import java.lang.ref.WeakReference;

/**
 * EventStringsAdapter
 */
public class EventStringsAdapter extends RecyclerView.Adapter<EventStringsAdapter.EventStringViewHolder>
{
    protected final WeakReference<Context> contextRef;
    protected String calendar;
    protected CalendarEventStrings data;

    public EventStringsAdapter(Context context, String calendar, CalendarEventStrings data) {
        this.contextRef = new WeakReference<>(context);
        this.calendar = calendar;
        this.data = data;
    }

    @Override
    public EventStringViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(EventStringViewHolder.getSuggestedLayoutID(), parent, false);
        return new EventStringViewHolder(contextRef.get(), view);
    }

    @Override
    public void onBindViewHolder(EventStringViewHolder holder, int position) {

        Context context = (contextRef != null ? contextRef.get() : null);
        if (context == null) {
            Log.w("ReminderViewHolder", "onBindViewHolder: null context!");
            return;
        }
        if (holder == null) {
            Log.w("ReminderViewHolder", "onBindViewHolder: null view holder!");
            return;
        }

        String value = data.getValue(position);
        holder.bindDataToPosition(context, position, value);
        attachListeners(holder, position);
    }

    @Override
    public void onViewRecycled(EventStringViewHolder holder) {
        detachListeners(holder);
    }

    @Override
    public int getItemCount() {
        return data.getCount();
    }

    private void attachListeners(final EventStringViewHolder holder, final int position)
    {
        holder.button_clear.setOnClickListener(onClearStringClicked(position));
        holder.edit_string.addTextChangedListener(holder.textWatcher = onStringValueChanged(holder, position));
    }
    private void detachListeners(final EventStringViewHolder holder)
    {
        holder.button_clear.setOnClickListener(null);
        holder.edit_string.removeTextChangedListener(holder.textWatcher);
    }

    private TextWatcher onStringValueChanged(final EventStringViewHolder holder, final int position)
    {
        return new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                triggerOnItemChanged(position, s.toString());
            }
        };
    }

    private View.OnClickListener onClearStringClicked(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                triggerOnItemClear(position);
            }
        };
    }

    /**
     * AdapterListener
     */
    public static abstract class AdapterListener
    {
        public void onItemChanged(int position, String value) {}
        public void onItemCleared(int position) {}
    }

    public AdapterListener adapterListener = null;
    public void setAdapterListener( AdapterListener listener ) {
        this.adapterListener = listener;
    }

    protected void triggerOnItemChanged(int position, String value)
    {
        if (adapterListener != null) {
            adapterListener.onItemChanged(position, value);
        }
    }

    protected void triggerOnItemClear(int position) {
        if (adapterListener != null) {
            adapterListener.onItemCleared(position);
        }
    }

    /**
     * EventStringViewHolder
     */
    public static class EventStringViewHolder extends RecyclerView.ViewHolder
    {
        public EditText edit_string;
        public ImageButton button_clear;
        public TextWatcher textWatcher = null;

        public static int getSuggestedLayoutID() {
            return R.layout.layout_item_string;
        }

        public EventStringViewHolder(Context context, View itemView)
        {
            super(itemView);
            edit_string = (EditText) itemView.findViewById(R.id.edit_string);
            button_clear = (ImageButton) itemView.findViewById(R.id.btn_clear);
        }

        public void bindDataToPosition(Context context, int position, String value) {
            edit_string.setText(value);
            edit_string.setHint(value);
        }
    }

}


