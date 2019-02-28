package com.nextline.rcalendar.data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nextline.rcalendar.R;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter {

    private int mFirstDayOfWeek = 0;
    private Calendar mCal;
    private LayoutInflater mInflater;

    ArrayList<Day> mItemList = new ArrayList<>();
    ArrayList<View> mViewList = new ArrayList<>();
    ArrayList<Event> mEventList = new ArrayList<>();
    Drawable eventDrawable = null;

    public CalendarAdapter(Context context, Calendar cal) {
        this.mCal = (Calendar) cal.clone();
        this.mCal.set(Calendar.DAY_OF_MONTH, 1);

        mInflater = LayoutInflater.from(context);

        refresh();
    }

    // public methods
    public int getCount() {
        return mItemList.size();
    }

    public Day getItem(int position) {
        return mItemList.get(position);
    }

    public View getView(final int position) {
        return mViewList.get(position);
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek;
    }

    public Calendar getCalendar() {
        return mCal;
    }

    public void setCalendar(Calendar calendar) {
        this.mCal = (Calendar) calendar.clone();
        refresh();
    }

    public void setEventDrawable(Drawable drawable) {
        this.eventDrawable = drawable;
    }

    public Drawable getEventDrawable(){
        return this.eventDrawable;
    }

    public void addEvent(Event event) {
        if(!mEventList.contains(event))
            mEventList.add(event);
    }

    public void removeEvent(Event event) {
        Log.d("removeEvent", "EventInside: year: " + mEventList.get(0).getYear() + " Month: " + + mEventList.get(0).getMonth() + " day: " + + mEventList.get(0).getDay());
        Log.d("removeEvent", "EventToDelete: year: " + event.getYear() + " Month: " + + event.getMonth() + " day: " + + event.getDay());
        Log.d("removeEvent", "Boolean" + mEventList.contains(event));
            for(int i = 0; i < mEventList.size(); i++){
                Event tempEvent = mEventList.get(i);
                if(tempEvent.getDay() == event.getDay() && tempEvent.getMonth() == event.getMonth() && tempEvent.getYear() == event.getYear())
                    mEventList.remove(i);
            }
    }

    public void refresh() {
        // clear data
        mItemList.clear();
        mViewList.clear();

        // set calendar
        int year = mCal.get(Calendar.YEAR);
        int month = mCal.get(Calendar.MONTH);

        mCal.set(year, month, 1);

        int lastDayOfMonth = mCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = mCal.get(Calendar.DAY_OF_WEEK) - 1;

        // generate day list
        int offset = 0 - (firstDayOfWeek - mFirstDayOfWeek) + 1;
        int length = (int) Math.ceil((float) (lastDayOfMonth - offset + 1) / 7) * 7;
        for (int i = offset; i < length + offset; i++) {
            int numYear;
            int numMonth;
            int numDay;

            Calendar tempCal = Calendar.getInstance();
            if (i <= 0) { // prev month
                if (month == 0) {
                    numYear = year - 1;
                    numMonth = 11;
                } else {
                    numYear = year;
                    numMonth = month - 1;
                }
                tempCal.set(numYear, numMonth, 1);
                numDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH) + i;
            } else if (i > lastDayOfMonth) { // next month
                if (month == 11) {
                    numYear = year + 1;
                    numMonth = 0;
                } else {
                    numYear = year;
                    numMonth = month + 1;
                }
                tempCal.set(numYear, numMonth, 1);
                numDay = i - lastDayOfMonth;
            } else {
                numYear = year;
                numMonth = month;
                numDay = i;
            }

            Day day = new Day(numYear, numMonth, numDay);

            View view = mInflater.inflate(R.layout.layout_day, null);
            TextView txtDay = (TextView) view.findViewById(R.id.txt_day);
            TextView eventDay = (TextView) view.findViewById(R.id.event_day);
            RelativeLayout relativeDay = (RelativeLayout) view.findViewById(R.id.relative_day);

            txtDay.setText(String.valueOf(day.getDay()));
            if (day.getMonth() != mCal.get(Calendar.MONTH)) {
                txtDay.setAlpha(0.3f);
            }

            for (int j = 0; j < mEventList.size(); j++) {
                Event event = mEventList.get(j);
                if (day.getYear() == event.getYear()
                        && day.getMonth() == event.getMonth()
                        && day.getDay() == event.getDay()) {
                    if(eventDrawable != null)
                        relativeDay.setBackground(eventDrawable);
                    else
                        relativeDay.setBackground(ResourcesCompat.getDrawable(txtDay.getContext().getResources(), R.drawable.circle_black_solid_background, null));
                    eventDay.setVisibility(View.VISIBLE);
                    eventDay.setText(txtDay.getText());
                    eventDay.setTextColor(Color.WHITE);
                }
            }

            mItemList.add(day);
            mViewList.add(view);
        }
    }

}
