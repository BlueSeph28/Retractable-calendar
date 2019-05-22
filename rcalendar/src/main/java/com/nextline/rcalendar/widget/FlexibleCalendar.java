package com.nextline.rcalendar.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nextline.rcalendar.R;
import com.nextline.rcalendar.data.CalendarAdapter;
import com.nextline.rcalendar.data.Day;
import com.nextline.rcalendar.data.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FlexibleCalendar extends UICalendar {

    private CalendarAdapter mAdapter;
    private CalendarListener mListener;
    private Boolean create = true;
    private RecyclerView recycler;
    private RecyclerView.OnItemTouchListener disabler = new RecyclerViewDisabler();

    private int mInitHeight = 0;

    private Handler mHandler = new Handler();
    private boolean mIsWaitingForUpdate = false;

    private int mCurrentWeekIndex;

    public Boolean getCreate() {
        return this.create;
    }

    public void setCreate(Boolean  create) {
        this.create = create;
    }

    public FlexibleCalendar(Context context) {
        super(context);
    }

    public FlexibleCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlexibleCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecyclerView getRecycler(){
        return this.recycler;
    }

    public void setRecycler(RecyclerView recycler){
        this.recycler = recycler;
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        if (isInEditMode()) {
            Calendar cal = Calendar.getInstance();
            CalendarAdapter adapter = new CalendarAdapter(context, cal);
            setAdapter(adapter);
        }

        setStateWithUpdateUI(getState());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mInitHeight = mTableBody.getMeasuredHeight();

        if (mIsWaitingForUpdate) {
            redraw();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    collapseTo(mCurrentWeekIndex);
                }
            });
            mIsWaitingForUpdate = false;
            if (mListener != null) {
                mListener.onDataUpdate();
            }
        }
    }

    @Override
    protected void redraw() {
        // redraw all views of week
        mTableHead.setBackgroundColor(getTitleBackgroundColor());
        mFadeHead.setBackgroundColor(getTitleBackgroundColor());
        TableRow rowWeek = (TableRow) mTableHead.getChildAt(0);
        if (rowWeek != null) {
            for (int i = 0; i < rowWeek.getChildCount(); i++) {

                ((TextView) rowWeek.getChildAt(i)).setTextColor(getTextColor());
            }
        }
        // redraw all views of day
        if (mAdapter != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Day day = mAdapter.getItem(i);
                View view = mAdapter.getView(i);
                TextView txtDay = (TextView) view.findViewById(R.id.txt_day);
                txtDay.setBackgroundColor(Color.TRANSPARENT);
                txtDay.setTextColor(getTextColor());

                // set today's item
                if (isToady(day)) {
                    txtDay.setBackground(getTodayItemBackgroundDrawable());
                    txtDay.setTextColor(getTodayItemTextColor());
                }

                // set the selected item
                if (isSelectedDay(day)) {
                    txtDay.setBackground(getSelectedItemBackgroundDrawable());
                    txtDay.setTextColor(getSelectedItemTextColor());
                }
            }
        }
    }

    @Override
    protected void reload() {
        if (mAdapter != null) {
            mAdapter.refresh();

            // reset UI
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
            dateFormat.setTimeZone(mAdapter.getCalendar().getTimeZone());
            //((TextView) recycler.getRootView().findViewById(R.id.txt_title) ).setText(dateFormat.format(mAdapter.getCalendar().getTime()));
            mTableHead.removeAllViews();
            mTableBody.removeAllViews();

            TableRow rowCurrent;

            // set day of week
            int[] dayOfWeekIds = {
                    R.string.sunday,
                    R.string.monday,
                    R.string.tuesday,
                    R.string.wednesday,
                    R.string.thursday,
                    R.string.friday,
                    R.string.saturday
            };
            rowCurrent = new TableRow(mContext);
            rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            for (int i = 0; i < 7; i++) {
                View view = mInflater.inflate(R.layout.layout_day_of_week, null);
                TextView txtDayOfWeek = (TextView) view.findViewById(R.id.txt_day_of_week);
                txtDayOfWeek.setText(dayOfWeekIds[(i + getFirstDayOfWeek()) % 7]);
                view.setLayoutParams(new TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1));
                rowCurrent.addView(view);
            }
            mTableHead.addView(rowCurrent);

            // set day view
            for (int i = 0; i < mAdapter.getCount(); i++) {
                final int position = i;

                if (position % 7 == 0) {
                    rowCurrent = new TableRow(mContext);
                    rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    mTableBody.addView(rowCurrent);
                }
                final View view = mAdapter.getView(position);
                view.setLayoutParams(new TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1));
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClicked(v, mAdapter.getItem(position));
                    }
                });
                rowCurrent.addView(view);
            }

            redraw();
            mIsWaitingForUpdate = true;
        }
    }

    private int getSuitableRowIndex() {
        if (getSelectedItemPosition() != -1) {
            View view = mAdapter.getView(getSelectedItemPosition());
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else if (getTodayItemPosition() != -1) {
            View view = mAdapter.getView(getTodayItemPosition());
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else {
            return 0;
        }
    }

    private void onItemClicked(View view, Day day) {
        select(day);
        Calendar cal = mAdapter.getCalendar();

        int newYear = day.getYear();
        int newMonth = day.getMonth();
        int oldYear = cal.get(Calendar.YEAR);
        int oldMonth = cal.get(Calendar.MONTH);
        if (newMonth != oldMonth) {

            if (newYear > oldYear || (newMonth > oldMonth && (newMonth != 11 || oldMonth != 0) )) {
                ((ImageButton) ((View) recycler.getParent()).findViewById(R.id.btn_next_month)).performClick();
            }
            else if (newYear < oldYear || newMonth < oldMonth) {
                ((ImageButton) ((View) recycler.getParent()).findViewById(R.id.btn_prev_month)).performClick();
            }

            //select(null);


        }

        if(getShrinkSelect() != -1 && newMonth == oldMonth) {
            View rootView = recycler.getRootView();
            rootView.findViewById(R.id.btn_prev_month).setVisibility(View.GONE);
            rootView.findViewById(R.id.btn_next_month).setVisibility(View.GONE);

            rootView.findViewById(R.id.btn_prev_week).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.btn_next_week).setVisibility(View.VISIBLE);
            collapse(getShrinkSelect());
        }

        if (mListener != null) {
            mListener.onItemClick(view, day);
        }
    }

    // public methods
    public void setAdapter(CalendarAdapter adapter) {
        mAdapter = adapter;
        adapter.setFirstDayOfWeek(getFirstDayOfWeek());

        reload();

        // init week
        mCurrentWeekIndex = getSuitableRowIndex();
    }

    public void addEventTag(int numYear, int numMonth, int numDay) {
        mAdapter.addEvent(new Event(numYear, numMonth, numDay));

        reload();
    }

    public void prevMonth() {
        Log.d("calendar", "prevMonthedew");
        Calendar cal = mAdapter.getCalendar();
        if (cal.get(Calendar.MONTH) == cal.getActualMinimum(Calendar.MONTH)) {
            cal.set((cal.get(Calendar.YEAR) - 1), cal.getActualMaximum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        }
        reload();
        Log.d("calendar", "prevMonth");
        if (mListener != null) {
            Log.d("calendar", "notNull");
            mListener.onMonthChange(false);
        }
    }

    public void nextMonth() {
        Calendar cal = mAdapter.getCalendar();
        if (cal.get(Calendar.MONTH) == cal.getActualMaximum(Calendar.MONTH)) {
            cal.set((cal.get(Calendar.YEAR) + 1), cal.getActualMinimum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        }
        reload();
        if (mListener != null) {
            mListener.onMonthChange(true);
        }
    }

    public void prevWeek() {
        if (mCurrentWeekIndex - 1 < 0) {
            mCurrentWeekIndex = -1;
            prevMonth();
        } else {
            mCurrentWeekIndex--;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public void nextWeek() {
        if (mCurrentWeekIndex + 1 >= mTableBody.getChildCount()) {
            mCurrentWeekIndex = 0;
            nextMonth();
        } else {
            mCurrentWeekIndex++;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public int getYear() {
        return mAdapter.getCalendar().get(Calendar.YEAR);
    }

    public int getMonth() {
        return mAdapter.getCalendar().get(Calendar.MONTH);
    }

    public Day getSelectedDay() {
        if(getSelectedItem() != null) {
            return new Day(
                    getSelectedItem().getYear(),
                    getSelectedItem().getMonth(),
                    getSelectedItem().getDay());
        }
        else
            return null;
    }

    public boolean isSelectedDay(Day day) {
        return day != null
                && getSelectedItem() != null
                && day.getYear() == getSelectedItem().getYear()
                && day.getMonth() == getSelectedItem().getMonth()
                && day.getDay() == getSelectedItem().getDay();
    }

    public boolean isToady(Day day) {
        Calendar todayCal = Calendar.getInstance();
        return day != null
                && day.getYear() == todayCal.get(Calendar.YEAR)
                && day.getMonth() == todayCal.get(Calendar.MONTH)
                && day.getDay() == todayCal.get(Calendar.DAY_OF_MONTH);
    }

    public int getSelectedItemPosition() {
        int position = -1;
        Day selectedDay = getSelectedDay();
        if(selectedDay != null) {
            if( (getSelectedDay().getMonth() < mAdapter.getCalendar().get(Calendar.MONTH))
                    || (getSelectedDay().getMonth() == 11 && mAdapter.getCalendar().get(Calendar.MONTH) == 0) ) {

                if((getSelectedDay().getMonth() == 0 && mAdapter.getCalendar().get(Calendar.MONTH) == 11)){
                    position = 0;
                }
                else {
                    int month = mAdapter.getCalendar().get(Calendar.MONTH);
                    int year = mAdapter.getCalendar().get(Calendar.YEAR);

                    if (month == 1) {
                        month = 12;
                        year--;
                    }

                    Calendar mycal = new GregorianCalendar(year, month, 1);
                    position = mycal.getActualMaximum(Calendar.DAY_OF_MONTH) + 1;
                }
            }
            else if( (getSelectedDay().getMonth() > mAdapter.getCalendar().get(Calendar.MONTH))
                    || (getSelectedDay().getMonth() == 0 && mAdapter.getCalendar().get(Calendar.MONTH) == 11) ) {
                position = 0;
            }
            else {
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    Day day = mAdapter.getItem(i);

                    if (isSelectedDay(day)) {
                        position = i;
                        break;
                    }
                }
            }
        }
        else {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Day day = mAdapter.getItem(i);

                if (isSelectedDay(day)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

    public int getTodayItemPosition() {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Day day = mAdapter.getItem(i);

            if (isToady(day)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void collapse(int duration) {
        if (getState() == STATE_EXPANDED || getState() == STATE_LOAD) {
            if(recycler != null) {
                recycler.addOnItemTouchListener(disabler);
                fadeInAndShow((LinearLayout) recycler.getRootView().findViewById(R.id.scrollable_signifier));
            }
            setState(STATE_PROCESSING);

            int index = getSuitableRowIndex();
            mCurrentWeekIndex = index;

            final int currentHeight = mInitHeight;
            final int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
            int tempHeight = 0;
            Log.d("bum", "" + targetHeight);
            for (int i = 0; i < index; i++) {
                tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
            }
            final int topHeight = tempHeight;
                Animation anim = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {

                        mScrollViewBody.getLayoutParams().height = (interpolatedTime == 1)
                                ? targetHeight
                                : currentHeight - (int) ((currentHeight - targetHeight) * interpolatedTime);
                        mScrollViewBody.requestLayout();

                        if (mScrollViewBody.getMeasuredHeight() < topHeight + targetHeight) {
                            int position = topHeight + targetHeight - mScrollViewBody.getMeasuredHeight();
                            mScrollViewBody.smoothScrollTo(0, position);
                        }

                        if (interpolatedTime == 1) {
                            setState(STATE_COLLAPSED);

                            //mBtnPrevWeek.setClickable(true);
                            //mBtnNextWeek.setClickable(true);
                        }
                    }
                };


                anim.setDuration(duration);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) { mListener.onCollapse(); }
                    @Override
                    public void onAnimationRepeat(Animation arg0) {}

                    @Override
                    public void onAnimationEnd(Animation arg0) {}
                });
                startAnimation(anim);
        }
    }

    private void collapseTo(int index) {
        if (getState() == STATE_COLLAPSED) {
            if (index == -1) {
                index = mTableBody.getChildCount() - 1;
            }
            mCurrentWeekIndex = index;

            final int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
            int tempHeight = 0;
            for (int i = 0; i < index; i++) {
                tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
            }
            final int topHeight = tempHeight;

            mScrollViewBody.getLayoutParams().height = targetHeight;
            mScrollViewBody.requestLayout();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mScrollViewBody.smoothScrollTo(0, topHeight);
                }
            });


            if (mListener != null) {
                mListener.onWeekChange(mCurrentWeekIndex);
            }
        }
    }

    private void fadeOutAndHide(final View img)
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }

    private void fadeInAndShow(final View img)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {
                img.setVisibility(View.VISIBLE);
            }
        });

        img.startAnimation(fadeOut);
    }


    public void expand(int duration) {
        if (getState() == STATE_COLLAPSED || getState() == STATE_LOAD) {

            if(recycler != null) {
                recycler.removeOnItemTouchListener(disabler);
                fadeOutAndHide((LinearLayout) recycler.getRootView().findViewById(R.id.scrollable_signifier));
            }

            if(getEvents())
                select(null);
            setState(STATE_PROCESSING);
            //mLayoutBtnGroupMonth.setVisibility(VISIBLE);
            //mLayoutBtnGroupWeek.setVisibility(GONE);
            //mBtnPrevMonth.setClickable(false);
            //mBtnNextMonth.setClickable(false);

            final int currentHeight = mScrollViewBody.getMeasuredHeight();
            final int targetHeight = mInitHeight;

                Animation anim = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {

                        mScrollViewBody.getLayoutParams().height = (interpolatedTime == 1)
                                ? LayoutParams.WRAP_CONTENT
                                : currentHeight - (int) ((currentHeight - targetHeight) * interpolatedTime);
                        mScrollViewBody.requestLayout();

                        if (interpolatedTime == 1) {
                            setState(STATE_EXPANDED);

                            //mBtnPrevMonth.setClickable(true);
                            //mBtnNextMonth.setClickable(true);
                        }
                    }
                };
                anim.setDuration(duration);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) { mListener.onExpand(); }
                    @Override
                    public void onAnimationRepeat(Animation arg0) {}

                    @Override
                    public void onAnimationEnd(Animation arg0) {}
                });
                startAnimation(anim);
        }
    }

    public void select(Day day) {
        if(day != null) {
            setSelectedItem(new Day(day.getYear(), day.getMonth(), day.getDay()));
        }
        else {
            setSelectedItem(null);
        }

        redraw();

        if (mListener != null) {
            mListener.onDaySelect();
        }
    }

    public void setStateWithUpdateUI(int state) {
        setState(state);

        if (getState() != state) {
            mIsWaitingForUpdate = true;
            requestLayout();
        }
    }

    public CalendarAdapter getCalendarAdapter() {
        return mAdapter;
    }

    // callback
    public void setCalendarListener(CalendarListener listener) {
        mListener = listener;
    }

    public interface CalendarListener {

        // triggered when a day is selected programmatically or clicked by user.
        void onDaySelect();

        // triggered only when the views of day on calendar are clicked by user.
        void onItemClick(View v, Day day);

        // triggered when the data of calendar are updated by changing month or adding events.
        void onDataUpdate();

        // triggered when the month are changed.
        void onMonthChange(Boolean nextMonth);

        // triggered when the week position are changed.
        void onWeekChange(int position);

        void onCollapse();

        void onExpand();
    }

    public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
