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
        List<Integer> ampm = events.get(i).ampm;

        eventViewHolder.eventStartTime.setText(events.get(i).start);

        if (endTime != null) {
            eventViewHolder.eventEndTime.setText(events.get(i).end);
        }

        if (!ampm.isEmpty()) {
            switch (ampm.get(0)) {
                case 0:
                    eventViewHolder.eventStartTime.append(" AM");
                    break;
                case 1:
                    eventViewHolder.eventStartTime.append(" PM");
                    break;
            }
            if (ampm.size() == 2) {
                switch (ampm.get(1)) {
                    case 0:
                        eventViewHolder.eventEndTime.append(" AM");
                        break;
                    case 1:
                        eventViewHolder.eventEndTime.append(" PM");
                        break;
                }
            }
        }

        final int item = i;

        eventViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> testTwo = new ArrayList<>();
                createEvent(events.get(item).name, events.get(item).days, events.get(item).ampm, events.get(item).startHour,
                        events.get(item).startMinutes, events.get(item).endHour, events.get(item).endMinutes);
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

    private void createEvent(String title, List<Integer> days, List<Integer> ampm, int startHour, int startMinutes, int endHour, int endMinutes) {

        //TODO: Configure times
        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        int weekday = beginTime.get(Calendar.DAY_OF_WEEK);
        String byDay = "";

        if (!days.isEmpty()) {
            if (weekday != days.get(0)) {
                int targetDay = (Calendar.SATURDAY - weekday + 7 - Math.abs(Calendar.SATURDAY - days.get(0))) % 7;
                beginTime.add(Calendar.DAY_OF_YEAR, targetDay);
                endTime.add(Calendar.DAY_OF_YEAR, targetDay);
            }
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
        }

        if (startHour == 12) {
            beginTime.set(Calendar.HOUR, 0);
        } else {
            beginTime.set(Calendar.HOUR, startHour);
        }
        if (!ampm.isEmpty()) {
            beginTime.set(Calendar.AM_PM, ampm.get(0));
        }

        beginTime.set(Calendar.MINUTE, startMinutes);


        if (endHour != -1 && endMinutes != -1) {
            if (ampm.size() != 2) {
                endTime.set(Calendar.AM_PM, ampm.get(0));
            } else {
                endTime.set(Calendar.AM_PM, ampm.get(1));
            }

            if (endHour == 12) {
                endTime.set(Calendar.HOUR, 0);
            } else {
                endTime.set(Calendar.HOUR, endHour);

            }
            endTime.set(Calendar.MINUTE, endMinutes);
        } else {
            endTime.set(Calendar.HOUR, startHour + 1);
            endTime.set(Calendar.HOUR, startMinutes);
        }


        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
        if (!byDay.isEmpty()) {
            intent.putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY="+byDay);
        }
        context.startActivity(intent);
    }
}
