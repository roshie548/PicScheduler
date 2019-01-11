package com.roshan.android.picscheduler;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.EventViewHolder> {

    List<ImageDetectActivity.Event> events;

    RVAdapter(List<ImageDetectActivity.Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_item, viewGroup, false);
        EventViewHolder eventViewHolder = new EventViewHolder(view);
        return eventViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder eventViewHolder, int i) {
        eventViewHolder.eventName.setText(events.get(i).name);

        String endTime = events.get(i).end;

        eventViewHolder.eventStartTime.setText(events.get(i).start);
        if (endTime != null) {
            eventViewHolder.eventEndTime.setText(events.get(i).end);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView eventName;
        TextView eventStartTime;
        TextView eventEndTime;

        EventViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            eventName = (TextView) itemView.findViewById(R.id.event_name);
            eventStartTime = (TextView) itemView.findViewById(R.id.event_start_time);
            eventEndTime = (TextView) itemView.findViewById(R.id.event_end_time);
        }
    }
}
