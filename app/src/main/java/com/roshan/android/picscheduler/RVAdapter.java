package com.roshan.android.picscheduler;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.EventViewHolder> {

    List<ImageDetectActivity.Event> events;
    private Context context;

    RVAdapter(Context context, List<ImageDetectActivity.Event> events) {
        this.context = context;
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

        eventViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Figure out how to create activity from here
                List<Integer> testList = new ArrayList<>();
                testList.add(Calendar.MONDAY);
                testList.add(Calendar.WEDNESDAY);
                testList.add(Calendar.FRIDAY);

                List<Integer> testTwo = new ArrayList<>();
                testTwo.add(Calendar.AM);
                testTwo.add(Calendar.AM);
                createEvent(testList, testTwo);
            }
        });
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

    private void createEvent(List<Integer> days, List<Integer> ampm) {
        Calendar now = Calendar.getInstance();
        int weekday = now.get(Calendar.DAY_OF_WEEK);
        if (weekday != days.get(0)) {
            int targetDay = (Calendar.SATURDAY - weekday + 7 - Math.abs(Calendar.SATURDAY - days.get(0))) % 7;
            now.add(Calendar.DAY_OF_YEAR, targetDay);
        }

        String byDay = "";
        for (int i = 0; i < days.size(); i++) {
            switch (days.get(i)) {
                case Calendar.SUNDAY:
                    byDay += "SU";
                    break;
                case Calendar.MONDAY:
                    byDay += "MO";
                    break;
                case Calendar.TUESDAY:
                    byDay += "TU";
                    break;
                case Calendar.WEDNESDAY:
                    byDay += "WE";
                    break;
                case Calendar.THURSDAY:
                    byDay += "TH";
                    break;
                case Calendar.FRIDAY:
                    byDay += "FR";
                    break;
                case Calendar.SATURDAY:
                    byDay += "SA";
                    break;
            }

            if (i != days.size() - 1) {
                byDay += ",";
            }
        }

        //Start time of event
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Calendar.AM_PM, ampm.get(0));

        //End time of event
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.AM_PM, ampm.get(1));
//        endTime.set(Calendar.HOUR)

        endTime.set(2019, 1, 8, 8, 30);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, now.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "test");
        if (!byDay.isEmpty()) {
            intent.putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY="+byDay);
        }
        context.startActivity(intent);
    }
}
