package com.nextline.rcalendar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.nextline.rcalendar.R;
import com.nextline.rcalendar.data.Day;
import com.nextline.rcalendar.view.LockScrollView;

/**
 * Created by AzureChen on 15/8/9.
 */
public abstract class UICalendar extends LinearLayout {

    // Style
    public static final int STYLE_LIGHT  = 0;
    public static final int STYLE_PINK   = 1;
    public static final int STYLE_ORANGE = 2;
    public static final int STYLE_BLUE   = 3;
    public static final int STYLE_GREEN  = 4;
    // Day of Week
    public static final int SUNDAY    = 0;
    public static final int MONDAY    = 1;
    public static final int TUESDAY   = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY  = 4;
    public static final int FRIDAY    = 5;
    public static final int SATURDAY  = 6;
    // State
    public static final int STATE_EXPANDED   = 0;
    public static final int STATE_COLLAPSED  = 1;
    public static final int STATE_PROCESSING = 2;
    public static final int STATE_LOAD = 3;

    protected Context mContext;
    protected LayoutInflater mInflater;

    // UI
    protected LinearLayout mLayoutRoot;
    protected TextView mTxtTitle;
    protected TableLayout mTableHead;
    protected ImageView mFadeHead;
    protected LockScrollView mScrollViewBody;
    protected TableLayout mTableBody;

    // Attributes
    private int mStyle = STYLE_LIGHT;
    private boolean mShowWeek = true;
    private int mFirstDayOfWeek = SUNDAY;
    private int mState = STATE_EXPANDED;

    private int mTextColor = Color.BLACK;
    private int mPrimaryColor = Color.WHITE;
    private int mTitleBackgroundColor = ResourcesCompat.getColor(getResources(), R.color.title_gray, null);

    private int mTodayItemTextColor = Color.BLACK;
    private Drawable mTodayItemBackgroundDrawable =
            getResources().getDrawable(R.drawable.circle_black_stroke_background);
    private int mSelectedItemTextColor = Color.WHITE;
    private Drawable mSelectedItemBackgroundDrawable =
            getResources().getDrawable(R.drawable.circle_orange_solid_background);

    private Drawable mButtonLeftDrawable =
            getResources().getDrawable(R.drawable.ic_navigate_before_black);
    private Drawable mButtonRightDrawable =
            getResources().getDrawable(R.drawable.ic_navigate_next_black);

    private Day mSelectedItem = null;

    private Boolean mEvents = false;
    private int mShrinkSelect = -1;

    public UICalendar(Context context) {
        this(context, null);
    }

    public UICalendar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UICalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.UICalendar, defStyleAttr, 0);
        setAttributes(attributes);
        attributes.recycle();
    }

    protected abstract void redraw();
    protected abstract void reload();

    protected void init(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        // load rootView from xml
        View rootView = mInflater.inflate(R.layout.widget_flexible_calendar, this, true);

        // init UI
        mLayoutRoot          = (LinearLayout)   rootView.findViewById(R.id.layout_root);
        mTxtTitle            = (TextView)       rootView.findViewById(R.id.txt_title);
        mTableHead           = (TableLayout)    rootView.findViewById(R.id.table_head);
        mFadeHead            = (ImageView)      rootView.findViewById(R.id.fade_head);
        mScrollViewBody      = (LockScrollView) rootView.findViewById(R.id.scroll_view_body);
        mTableBody           = (TableLayout)    rootView.findViewById(R.id.table_body);
        /*
        mLayoutBtnGroupMonth = (RelativeLayout) rootView.findViewById(R.id.layout_btn_group_month);
        mLayoutBtnGroupWeek  = (RelativeLayout) rootView.findViewById(R.id.layout_btn_group_week);
        mBtnPrevMonth        = (ImageButton)    rootView.findViewById(R.id.btn_prev_month);
        mBtnNextMonth        = (ImageButton)    rootView.findViewById(R.id.btn_next_month);
        mBtnPrevWeek         = (ImageButton)    rootView.findViewById(R.id.btn_prev_week);
        mBtnNextWeek         = (ImageButton)    rootView.findViewById(R.id.btn_next_week);*/
    }

    protected void setAttributes(TypedArray attrs) {
        // set attributes by the values from XML
        setStyle(attrs.getInt(R.styleable.UICalendar_style, mStyle));
        setShowWeek(attrs.getBoolean(R.styleable.UICalendar_showWeek, mShowWeek));
        setFirstDayOfWeek(attrs.getInt(R.styleable.UICalendar_firstDayOfWeek, mFirstDayOfWeek));
        setState(attrs.getInt(R.styleable.UICalendar_state, mState));

        setTextColor(attrs.getColor(R.styleable.UICalendar_textColor, mTextColor));
        setPrimaryColor(attrs.getColor(R.styleable.UICalendar_primaryColor, mPrimaryColor));

        setTodayItemTextColor(attrs.getColor(
                R.styleable.UICalendar_todayItem_textColor, mTodayItemTextColor));
        Drawable todayItemBackgroundDrawable =
                attrs.getDrawable(R.styleable.UICalendar_todayItem_background);
        if (todayItemBackgroundDrawable != null) {
            setTodayItemBackgroundDrawable(todayItemBackgroundDrawable);
        } else {
            setTodayItemBackgroundDrawable(mTodayItemBackgroundDrawable);
        }

        setSelectedItemTextColor(attrs.getColor(
                R.styleable.UICalendar_selectedItem_textColor, mSelectedItemTextColor));
        Drawable selectedItemBackgroundDrawable =
                attrs.getDrawable(R.styleable.UICalendar_selectedItem_background);
        if (selectedItemBackgroundDrawable != null) {
            setSelectedItemBackgroundDrawable(selectedItemBackgroundDrawable);
        } else {
            setSelectedItemBackgroundDrawable(mSelectedItemBackgroundDrawable);
        }

        Drawable buttonLeftDrawable =
                attrs.getDrawable(R.styleable.UICalendar_buttonLeft_drawable);
        if (buttonLeftDrawable != null) {
            setButtonLeftDrawable(buttonLeftDrawable);
        } else {
            setButtonLeftDrawable(mButtonLeftDrawable);
        }

        Drawable buttonRightDrawable =
                attrs.getDrawable(R.styleable.UICalendar_buttonRight_drawable);
        if (buttonRightDrawable != null) {
            setButtonRightDrawable(buttonRightDrawable);
        } else {
            setButtonRightDrawable(mButtonRightDrawable);
        }

        setEvents(attrs.getBoolean(R.styleable.UICalendar_showEvents, mEvents));
        setShrinkSelect(attrs.getInt(R.styleable.UICalendar_shrinkOnSelect, mShrinkSelect));

        Day selectedItem   = null;
    }

    // getters and setters
    public int getStyle() {
        return mStyle;
    }

    public void setStyle(int style) {
        this.mStyle = style;

        Drawable selectedDrawable = ResourcesCompat
                .getDrawable(getResources(), R.drawable.circle_white_solid_background, null);
        if (style == STYLE_LIGHT) {
            setTextColor(Color.BLACK);
            setPrimaryColor(Color.WHITE);
            setTodayItemTextColor(Color.BLACK);
            setTodayItemBackgroundDrawable(
                    getResources().getDrawable(R.drawable.circle_black_stroke_background));
            setSelectedItemTextColor(Color.WHITE);
            selectedDrawable.setColorFilter(0xffe19C00, PorterDuff.Mode.MULTIPLY);
            setSelectedItemBackgroundDrawable(
                    selectedDrawable);
            setButtonLeftDrawable(
                    getResources().getDrawable(R.drawable.ic_navigate_before_black));
            setButtonRightDrawable(
                    getResources().getDrawable(R.drawable.ic_navigate_next_black));
        } else {
            setTextColor(Color.WHITE);
            setTodayItemTextColor(Color.WHITE);
            setTodayItemBackgroundDrawable(
                    getResources().getDrawable(R.drawable.circle_white_stroke_background));
            setButtonLeftDrawable(
                    getResources().getDrawable(R.drawable.ic_navigate_before_white));
            setButtonRightDrawable(
                    getResources().getDrawable(R.drawable.ic_navigate_next_white));

            int color = 0;
            if (style == STYLE_PINK) {
                color = mContext.getResources().getColor(R.color.primary_pink);
                selectedDrawable.setColorFilter(0xffB2090B, PorterDuff.Mode.MULTIPLY);
            }
            if (style == STYLE_ORANGE) {
                color = mContext.getResources().getColor(R.color.primary_orange);
                selectedDrawable.setColorFilter(0xff58FB8C, PorterDuff.Mode.MULTIPLY);
            }
            if (style == STYLE_BLUE) {
                color = mContext.getResources().getColor(R.color.primary_blue);
                selectedDrawable.setColorFilter(0xffD66260, PorterDuff.Mode.MULTIPLY);
            }
            if (style == STYLE_GREEN) {
                color = mContext.getResources().getColor(R.color.primary_green);
                selectedDrawable.setColorFilter(0xff246767, PorterDuff.Mode.MULTIPLY);
            }
            setPrimaryColor(color);
            setSelectedItemTextColor(color);
        }
        setSelectedItemBackgroundDrawable(selectedDrawable);
    }

    public boolean isShowWeek() {
        return mShowWeek;
    }

    public void setShowWeek(boolean showWeek) {
        this.mShowWeek = showWeek;

        if (showWeek) {
            mTableHead.setVisibility(VISIBLE);
        } else {
            mTableHead.setVisibility(GONE);
        }
    }

    public Boolean getEvents() {
        return mEvents;
    }

    public void setEvents(Boolean events) {
        this.mEvents = events;
    }

    public int getShrinkSelect() {
        return mShrinkSelect;
    }

    public void setShrinkSelect(int shrinkSelect) {
        this.mShrinkSelect = shrinkSelect;
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mFirstDayOfWeek = firstDayOfWeek;
        reload();
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        this.mState = state;
/*
        if (mState == STATE_EXPANDED) {
            mLayoutBtnGroupMonth.setVisibility(VISIBLE);
            mLayoutBtnGroupWeek.setVisibility(GONE);
        }
        if (mState == STATE_COLLAPSED) {
            mLayoutBtnGroupMonth.setVisibility(GONE);
            mLayoutBtnGroupWeek.setVisibility(VISIBLE);
        }*/
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        redraw();

        //mTxtTitle.setTextColor(mTextColor);
    }

    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    public void setPrimaryColor(int primaryColor) {
        this.mPrimaryColor = primaryColor;
        redraw();

        mLayoutRoot.setBackgroundColor(mPrimaryColor);
    }

    public int getTitleBackgroundColor() { return mTitleBackgroundColor; }

    public void setTitleBackgroundColor(int titleBackgroundColor) {
        this.mTitleBackgroundColor = titleBackgroundColor;
        redraw();
    }

    public int getTodayItemTextColor() {
        return mTodayItemTextColor;
    }

    public void setTodayItemTextColor(int todayItemTextColor) {
        this.mTodayItemTextColor = todayItemTextColor;
        redraw();
    }

    public Drawable getTodayItemBackgroundDrawable() {
        return mTodayItemBackgroundDrawable;
    }

    public void setTodayItemBackgroundDrawable(Drawable todayItemBackgroundDrawable) {
        this.mTodayItemBackgroundDrawable = todayItemBackgroundDrawable;
        redraw();
    }

    public int getSelectedItemTextColor() {
        return mSelectedItemTextColor;
    }

    public void setSelectedItemTextColor(int selectedItemTextColor) {
        this.mSelectedItemTextColor = selectedItemTextColor;
        redraw();
    }

    public Drawable getSelectedItemBackgroundDrawable() {
        return mSelectedItemBackgroundDrawable;
    }

    public void setSelectedItemBackgroundDrawable(Drawable selectedItemBackground) {
        this.mSelectedItemBackgroundDrawable = selectedItemBackground;
        redraw();
    }

    public Drawable getButtonLeftDrawable() {
        return mButtonLeftDrawable;
    }

    public void setButtonLeftDrawable(Drawable buttonLeftDrawable) {
        this.mButtonLeftDrawable = buttonLeftDrawable;
        //mBtnPrevMonth.setImageDrawable(buttonLeftDrawable);
        //mBtnPrevWeek.setImageDrawable(buttonLeftDrawable);
    }

    public Drawable getButtonRightDrawable() {
        return mButtonRightDrawable;
    }

    public void setButtonRightDrawable(Drawable buttonRightDrawable) {
        this.mButtonRightDrawable = buttonRightDrawable;
        //mBtnNextMonth.setImageDrawable(buttonRightDrawable);
        //mBtnNextWeek.setImageDrawable(buttonRightDrawable);
    }

    public Day getSelectedItem() {
        return mSelectedItem;
    }

    public void setSelectedItem(Day selectedItem) {
        this.mSelectedItem = selectedItem;
    }
}
