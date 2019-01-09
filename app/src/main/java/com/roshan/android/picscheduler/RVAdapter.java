package com.roshan.android.picscheduler;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.EventViewHolder> {
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder eventViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class EventViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView eventName;
        TextView eventTime;

        EventViewHolder(View itemView) {
            super(itemView);
        }
    }
}
