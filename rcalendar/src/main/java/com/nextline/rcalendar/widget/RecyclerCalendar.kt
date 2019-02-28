package com.nextline.rcalendar.widget

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import com.nextline.rcalendar.R
import com.nextline.rcalendar.adapter.Calendar
import com.nextline.rcalendar.adapter.EventPublication
import com.nextline.rcalendar.data.Day
import com.nextline.rcalendar.data.Event
import com.nextline.rcalendar.widget.UICalendar.*
import kotlin.properties.Delegates

/**
 * Created by fernando on 12/05/18.
 */

class RecyclerCalendar : LinearLayout, Calendar.AdapterListener, EventPublication.Handler {
    internal lateinit var mContext: Context
    internal lateinit var mInflater: LayoutInflater
    var lastPosition = 1
    var position = 1
    var realLastPosition = Integer.MAX_VALUE / 2 + 1
    var selectedDate: Event? = null
    internal lateinit var adapter: Calendar
    internal lateinit var adapterEvents: EventPublication
    var eventAdapterList: MutableList<com.nextline.rcalendar.data.EventPublication> = mutableListOf()
    var imageLoader: ImageLoader? = null
        set(loader) {
            if(loader != null) {
                field = loader
                adapterEvents.imageLoader = field
            }
        }
    lateinit var calendarInflated: RecyclerView
    lateinit var eventsInflated: RecyclerView
    lateinit var mLinearLayoutManager: LinearLayoutManager
    lateinit var mLinearLayoutParams: LinearLayout.LayoutParams
    lateinit var mLinearLayoutEventsManager: LinearLayoutManager
    var loadEvents: Boolean = false
    var shrinkSelect: Int? = null
    val monthIds = intArrayOf(
            R.string.january_short,
            R.string.february_short,
            R.string.march_short,
            R.string.april_short,
            R.string.may_short,
            R.string.june_short,
            R.string.july_short,
            R.string.august_short,
            R.string.september_short,
            R.string.october_short,
            R.string.november_short,
            R.string.december_short
    )
    var collapsed: Boolean by Delegates.notNull()
    var hiddenArrows: Boolean = false

    fun addEvent(item: com.nextline.rcalendar.data.EventPublication) {
        if(loadEvents)
            eventAdapterList.add(item)
    }

    fun removeEventByPosition(position: Int) {
        eventAdapterList.removeAt(position)
        if(selectedDate != null && eventAdapterList.size == 0) {
            removeEventTag(selectedDate!!.year, selectedDate!!.month, selectedDate!!.day)
            expand(500)
        }
    }

    fun refreshEvents() {
        adapterEvents.notifyDataSetChanged()
    }

    override fun onClickDetails(position: Int) {
        if(mListener != null)
            mListener!!.onClickDetails(position)

    }

    override fun onClickShare(position: Int) {
        if(mListener != null)
            mListener!!.onClickShare(position)
    }

    //Params
    var mTextColor: Int = Color.BLACK
    var mPrimaryColor: Int = Color.WHITE
    var mButtonLeftDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_navigate_before_black, null)
    var mButtonRightDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_navigate_next_black, null)
    var mShowEvents = false
    var mListener: RCalendarListener? = null

    abstract class ImageLoader {
        abstract fun loadImage(image: ImageView, url: String)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs!!, 0) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mInflater = LayoutInflater.from(context)
        val rootView = mInflater.inflate(R.layout.widget_swipe_calendar, this, true)

        calendarInflated = rootView.findViewById<RecyclerView>(R.id.infinite_calendar)
        eventsInflated = rootView.findViewById<RecyclerView>(R.id.event_recycler)


        mLinearLayoutManager = object:LinearLayoutManager(rootView.context) {
            override fun requestChildRectangleOnScreen(parent:RecyclerView, child:View, rect: Rect, immediate:Boolean):Boolean {
                return true
            }
        }
        val snapHelper = PagerSnapHelper()

        calendarInflated.setHasFixedSize(true)
        calendarInflated.isNestedScrollingEnabled = false


        mLinearLayoutParams = calendarInflated.layoutParams as LayoutParams

        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL)
        mLinearLayoutManager.scrollToPosition(realLastPosition)
        calendarInflated.setLayoutManager(mLinearLayoutManager)
        calendarInflated.setItemViewCacheSize(0)
        snapHelper.attachToRecyclerView(calendarInflated)

        adapterEvents = EventPublication(rootView.context as Activity, eventAdapterList, eventsInflated)
        adapterEvents.listener = this

        mLinearLayoutEventsManager = LinearLayoutManager(rootView.context)
        mLinearLayoutEventsManager.orientation = LinearLayoutManager.VERTICAL

        eventsInflated.adapter = adapterEvents
        eventsInflated.layoutManager = mLinearLayoutEventsManager
        adapter = Calendar(rootView.context as Activity, calendarInflated)
        calendarInflated.adapter = adapter
        adapter.setCalendarListener(this)

        initTitle()

        val scrollSignifier = rootView.findViewById<LinearLayout>(R.id.scrollable_signifier)

            scrollSignifier.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        eventAdapterList.clear()
                        adapterEvents.notifyDataSetChanged()
                        expand(shrinkSelect!!)
                        loadEvents = false
                    }
                }
                true
            }

        val attributes = context.theme.obtainStyledAttributes(
                attrs, R.styleable.UICalendar, defStyleAttr, 0)
        setAttributes(attributes, adapter)
        attributes.recycle()

        // bind events
        rootView.findViewById<ImageButton>(R.id.btn_prev_month).setOnClickListener {
            if(mListener != null)
                mListener!!.onMonthChange(false)
            val selectedDay: Day? = mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).selectedItem
            mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).select(null)

            realLastPosition -= 1
            mLinearLayoutManager.scrollToPosition(realLastPosition)
            position = realLastPosition % 3
            var actualCalendar = adapter.calendarHash[position]!!.calendar.clone() as java.util.Calendar

            actualCalendar.add(java.util.Calendar.MONTH, -1)
            when (position) {
                0 -> {
                    adapter.calendarHash[2]!!.calendar = actualCalendar
                }
                1 -> {
                    adapter.calendarHash[0]!!.calendar = actualCalendar
                }
                2 -> {
                    adapter.calendarHash[1]!!.calendar = actualCalendar
                }
            }

                Handler().postDelayed(Runnable {
                    if(selectedDay != null) {
                        mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).select(selectedDay)
                        if (shrinkSelect != null)
                            collapse(shrinkSelect!!)
                    }
                    setTitle()
                    calendarInflated.layoutParams = mLinearLayoutParams

                }, 200)
        }

        rootView.findViewById<ImageButton>(R.id.btn_next_month).setOnClickListener(OnClickListener {
            if(mListener != null)
                mListener!!.onMonthChange(true)
            val selectedDay: Day? = mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).selectedItem
            mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).select(null)

            realLastPosition += 1
            mLinearLayoutManager.scrollToPosition(realLastPosition)
            position = realLastPosition % 3
            var actualCalendar = adapter.calendarHash[position]!!.calendar.clone() as java.util.Calendar
            actualCalendar.add(java.util.Calendar.MONTH, 1)
            when (position) {
                0 -> {
                    adapter.calendarHash[1]!!.calendar = actualCalendar
                }
                1 -> {
                    adapter.calendarHash[2]!!.calendar = actualCalendar
                }
                2 -> {
                    adapter.calendarHash[0]!!.calendar = actualCalendar
                }
            }

                Handler().postDelayed(Runnable {
                    if(selectedDay != null) {
                        mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).select(selectedDay)
                        if (shrinkSelect != null)
                            collapse(shrinkSelect!!)
                    }

                    setTitle()
                    calendarInflated.layoutParams = mLinearLayoutParams
                }, 200)
        })

        rootView.findViewById<ImageButton>(R.id.btn_prev_week).setOnClickListener(OnClickListener {
            mLinearLayoutManager
                    .findViewByPosition(realLastPosition)
                    .findViewById<FlexibleCalendar>(R.id.calendar)
                    .prevWeek()
        })

        rootView.findViewById<ImageButton>(R.id.btn_next_week).setOnClickListener(OnClickListener {
            mLinearLayoutManager
                    .findViewByPosition(realLastPosition)
                    .findViewById<FlexibleCalendar>(R.id.calendar)
                    .nextWeek()
        })


        calendarInflated.addOnItemTouchListener(object:RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv:RecyclerView, e:MotionEvent):Boolean {
                val action = e.getAction()
                when (action) {
                    MotionEvent.ACTION_MOVE -> rv.getParent().requestDisallowInterceptTouchEvent(true)
                }
                return false
            }
            override fun onTouchEvent(rv:RecyclerView, e:MotionEvent) {
            }
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept:Boolean) {
            }
        })

        calendarInflated.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(mLinearLayoutManager.findFirstVisibleItemPosition() < realLastPosition && dx < 0) {
                    position = mLinearLayoutManager.findFirstVisibleItemPosition() % 3
                    realLastPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
                }
                else if(mLinearLayoutManager.findLastVisibleItemPosition() > realLastPosition && dx > 0) {
                    position = mLinearLayoutManager.findLastVisibleItemPosition() % 3
                    realLastPosition = mLinearLayoutManager.findLastVisibleItemPosition()
                }

                if(lastPosition != position) {
                    val added: Boolean
                    if (lastPosition + 1 == position || (lastPosition == 2 && position == 0) ) {
                        added = true
                    }
                    else{
                        added = false
                    }

                    var actualCalendar = adapter.calendarHash[position]!!.calendar.clone() as java.util.Calendar

                    when (position) {
                        0 -> {
                            if (added) {
                                actualCalendar.add(java.util.Calendar.MONTH, 1)
                                adapter.calendarHash[1]!!.calendar = actualCalendar
                                if(mListener != null)
                                    mListener!!.onMonthChange(true)
                            }
                            else {
                                actualCalendar.add(java.util.Calendar.MONTH, -1)
                                adapter.calendarHash[2]!!.calendar = actualCalendar
                                if(mListener != null)
                                    mListener!!.onMonthChange(false)
                            }
                        }
                        1 -> {
                            if (added) {
                                actualCalendar.add(java.util.Calendar.MONTH, 1)
                                adapter.calendarHash[2]!!.calendar = actualCalendar
                                if(mListener != null)
                                    mListener!!.onMonthChange(true)
                            }
                            else {
                                actualCalendar.add(java.util.Calendar.MONTH, -1)
                                adapter.calendarHash[0]!!.calendar = actualCalendar
                                if(mListener != null)
                                    mListener!!.onMonthChange(false)
                            }
                        }
                        2 -> {
                            if (added) {
                                actualCalendar.add(java.util.Calendar.MONTH, 1)
                                adapter.calendarHash[0]!!.calendar = actualCalendar
                                if(mListener != null)
                                    mListener!!.onMonthChange(true)
                            }
                            else {
                                actualCalendar.add(java.util.Calendar.MONTH, -1)
                                adapter.calendarHash[1]!!.calendar = actualCalendar
                                if(mListener != null)
                                    mListener!!.onMonthChange(false)
                            }
                        }
                    }
                    setTitle()
                    lastPosition = position
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if(hiddenArrows) {
                            if (collapsed) {
                                fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_prev_week))
                                fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_next_week))
                            } else {
                                fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_prev_month))
                                fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_next_month))
                            }
                        }
                        hiddenArrows = false
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                        if (!hiddenArrows) {
                            if (collapsed) {
                                fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_prev_week))
                                fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_next_week))
                            } else {
                                fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_prev_month))
                                fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_next_month))
                            }
                            hiddenArrows = true
                        }
                    }
                }

            }
        })
    }

    fun setListener(listener: RCalendarListener) {
        this.mListener = listener
    }

    fun getEventByPosition(position: Int): com.nextline.rcalendar.data.EventPublication {
        return (eventsInflated.adapter as EventPublication).getEventByPosition(position)
    }

    protected fun setAttributes(attrs: TypedArray, adapter: Calendar) {
        adapter.style = attrsGetIntDefNull(attrs, R.styleable.UICalendar_style)
        setStyle(attrsGetIntDefNull(attrs, R.styleable.UICalendar_style))

        adapter.showWeek = attrs.getBoolean(R.styleable.UICalendar_showWeek, true)
        adapter.firstDayOfWeek = attrsGetIntDefNull(attrs, R.styleable.UICalendar_firstDayOfWeek)
        adapter.state = attrsGetIntDefNull(attrs, R.styleable.UICalendar_state)

        if(adapter.state == STATE_COLLAPSED) {
            collapsed = true
            rootView.findViewById<ImageButton>(R.id.btn_prev_month).visibility = View.GONE
            rootView.findViewById<ImageButton>(R.id.btn_next_month).visibility = View.GONE
        }
        else{
            collapsed = false
            rootView.findViewById<ImageButton>(R.id.btn_prev_week).visibility = View.GONE
            rootView.findViewById<ImageButton>(R.id.btn_next_week).visibility = View.GONE
        }

        setTextColor(attrsGetColorDefNull(attrs, R.styleable.UICalendar_textColor))

        adapter.primaryColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_primaryColor)
        setPrimaryColor(attrsGetColorDefNull(attrs, R.styleable.UICalendar_primaryColor))

        adapterEvents.dateColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_primaryColor)

        adapterEvents.primaryTitleColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_primaryTitleColor)
        adapterEvents.secondaryTitleColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_secondaryTitleColor)
        adapterEvents.dateTextColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_dateTextColor)
        adapterEvents.dateColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_dateBackgroundColor)
        adapterEvents.eventBackgroundColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_eventBackgroundColor)
        adapterEvents.detailsImage = attrsGetIntDefNull(attrs, R.styleable.UICalendar_detailsImage)
        adapterEvents.shareImage = attrsGetIntDefNull(attrs, R.styleable.UICalendar_shareImage)

        setEventDrawable(attrs.getDrawable(R.styleable.UICalendar_eventDrawable))

        adapter.todayItemTextColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_todayItem_textColor)
        adapter.todayItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_todayItem_background)
        adapter.selectedItemTextColor = attrsGetColorDefNull(attrs, R.styleable.UICalendar_selectedItem_textColor)
        adapter.selectedItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_selectedItem_background)
        setButtonLeftDrawable(attrs.getDrawable(R.styleable.UICalendar_buttonLeft_drawable))
        setButtonRightDrawable(attrs.getDrawable(R.styleable.UICalendar_buttonRight_drawable))
        adapter.shrinkSelect = attrsGetIntDefNull(attrs, R.styleable.UICalendar_shrinkOnSelect)
        shrinkSelect = attrsGetIntDefNull(attrs, R.styleable.UICalendar_shrinkOnSelect)

        setShowEvents(attrs.getBoolean(R.styleable.UICalendar_showEvents, false))

        adapter.events = attrs.getBoolean(R.styleable.UICalendar_showEvents, false)
    }

    fun setEventDrawable(drawable: Drawable?) {
            adapter.calendarHash[0]!!.eventDrawable = drawable
            adapter.calendarHash[1]!!.eventDrawable = drawable
            adapter.calendarHash[2]!!.eventDrawable = drawable
    }

    private fun attrsGetIntDefNull(attrs: TypedArray, index: Int) : Int? {
        val result = attrs.getInt(index, Integer.MAX_VALUE)
        if(result == Integer.MAX_VALUE)
            return null
        else
            return result
    }

    private fun attrsGetColorDefNull(attrs: TypedArray, index: Int) : Int? {
        val result = attrs.getColor(index, Integer.MAX_VALUE)
        if(result == Integer.MAX_VALUE)
            return null
        else {
            return result
        }
    }

    fun setShowEvents(show: Boolean) {
        mShowEvents = show
        if(show)
            rootView.findViewById<RecyclerView>(R.id.event_recycler).visibility = View.VISIBLE
        else
            rootView.findViewById<RecyclerView>(R.id.event_recycler).visibility = View.GONE
    }

    fun collapse(duration: Int) {
        //adapter.state = STATE_COLLAPSED
        rootView.findViewById<ImageButton>(R.id.btn_prev_month).visibility = View.GONE
        rootView.findViewById<ImageButton>(R.id.btn_next_month).visibility = View.GONE

        rootView.findViewById<ImageButton>(R.id.btn_prev_week).visibility = View.VISIBLE
        rootView.findViewById<ImageButton>(R.id.btn_next_week).visibility = View.VISIBLE

        loadEvents = true

        mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).collapse(duration)
    }

    fun expand(duration: Int) {
        //adapter.state = STATE_EXPANDED

        rootView.findViewById<ImageButton>(R.id.btn_prev_month).visibility = View.VISIBLE
        rootView.findViewById<ImageButton>(R.id.btn_next_month).visibility = View.VISIBLE

        rootView.findViewById<ImageButton>(R.id.btn_prev_week).visibility = View.GONE
        rootView.findViewById<ImageButton>(R.id.btn_next_week).visibility = View.GONE

        loadEvents = false
        showLoading(false)

        mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).expand(duration)
    }

    fun initTitle() {
        val id = adapter.calendarHash[1]!!.calendar.get(java.util.Calendar.MONTH)
        val year = adapter.calendarHash[1]!!.calendar.get(java.util.Calendar.YEAR)
        val title = resources.getString(monthIds[id]) + " " + year
        rootView.findViewById<TextView>(R.id.txt_title).setText(title)
    }

    fun setTitle() {
        val id = mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).calendarAdapter.calendar.get(java.util.Calendar.MONTH)
        val year = mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).calendarAdapter.calendar.get(java.util.Calendar.YEAR)
        val title = resources.getString(monthIds[id]) + " " + year
        rootView.findViewById<TextView>(R.id.txt_title).setText(title)
    }

    fun setTextColor(textColor: Int?) {
        if(textColor != null) {
            mTextColor = textColor
            adapter.textColor = textColor
            rootView.findViewById<TextView>(R.id.txt_title).setTextColor(textColor)
        }
    }

    fun setTitleBackgroundColor(color: Int?) {
        if(color != null) {
            rootView.findViewById<RelativeLayout>(R.id.controls_layout).setBackgroundColor(color)
        }
    }

    fun setTextColorButtons(color: Int?) {
        if(color != null)
            adapterEvents.textColorButtons = color
    }

    fun setPrimaryColor(primaryColor: Int?) {
        if(primaryColor != null) {
            mPrimaryColor = primaryColor
            rootView.findViewById<RelativeLayout>(R.id.controls_layout).setBackgroundColor(mPrimaryColor)
            rootView.findViewById<LinearLayout>(R.id.scrollable_signifier).setBackgroundColor(mPrimaryColor)
            if(primaryColor != ResourcesCompat.getColor(resources, R.color.white, null)){
                rootView.findViewById<ImageView>(R.id.scrollable_item)
                        .setImageResource(R.drawable.scrollable_white)

                rootView.findViewById<TextView>(R.id.txt_title)
                        .setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))

                rootView.findViewById<ImageView>(R.id.btn_prev_month)
                        .setImageResource(R.drawable.ic_navigate_before_white)

                rootView.findViewById<ImageView>(R.id.btn_next_month)
                        .setImageResource(R.drawable.ic_navigate_next_white)

                rootView.findViewById<ImageView>(R.id.btn_prev_week)
                        .setImageResource(R.drawable.ic_navigate_before_white)

                rootView.findViewById<ImageView>(R.id.btn_next_week)
                        .setImageResource(R.drawable.ic_navigate_next_white)
            }
            else {
                rootView.findViewById<ImageView>(R.id.scrollable_item)
                        .setImageResource(R.drawable.scrollable_black)

                rootView.findViewById<TextView>(R.id.txt_title)
                        .setTextColor(ResourcesCompat.getColor(resources, android.R.color.black, null))

                rootView.findViewById<ImageView>(R.id.btn_prev_month)
                        .setImageResource(R.drawable.ic_navigate_before_black)

                rootView.findViewById<ImageView>(R.id.btn_next_month)
                        .setImageResource(R.drawable.ic_navigate_next_black)

                rootView.findViewById<ImageView>(R.id.btn_prev_week)
                        .setImageResource(R.drawable.ic_navigate_before_black)

                rootView.findViewById<ImageView>(R.id.btn_next_week)
                        .setImageResource(R.drawable.ic_navigate_next_black)
            }
        }
    }

    fun setStyle(style: Int?) {

        adapterEvents.eventBackgroundColor = Color.WHITE
        if(style == STYLE_LIGHT) {
            setPrimaryColor(Color.WHITE)

            setTitleBackgroundColor(ResourcesCompat.getColor(resources, R.color.title_gray, null))
            setTextColor(Color.BLACK)
            adapterEvents.dateColor = ResourcesCompat.getColor(resources, R.color.selected_orange, null);
            adapterEvents.dateTextColor = Color.WHITE
            adapterEvents.primaryTitleColor = Color.BLACK
            adapterEvents.secondaryTitleColor = Color.parseColor("#ccc5c5")

            setButtonLeftDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_navigate_before_black, null)!!)
            setButtonRightDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_navigate_next_black, null)!!)
        }
        else if(style != null) {
            setTextColor(Color.WHITE)
            setButtonLeftDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_navigate_before_white, null)!!)
            setButtonRightDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_navigate_next_white, null)!!)

            setEventDrawable(ResourcesCompat.getDrawable(resources, R.drawable.circle_white_solid_background, null))

            adapterEvents.dateTextColor = Color.WHITE
            adapterEvents.primaryTitleColor = Color.BLACK
            adapterEvents.secondaryTitleColor = Color.parseColor("#ccc5c5")
            when (style) {
                STYLE_PINK -> {
                    setPrimaryColor(ResourcesCompat.getColor(resources, R.color.primary_pink, null))
                    setTitleBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary_pink, null))
                    adapterEvents.dateColor = ResourcesCompat.getColor(resources, R.color.primary_pink, null)
                }
                STYLE_ORANGE -> {
                    setPrimaryColor(ResourcesCompat.getColor(resources, R.color.primary_orange, null))
                    setTitleBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary_orange, null))
                    adapterEvents.dateColor = ResourcesCompat.getColor(resources, R.color.primary_orange, null)
                }

                STYLE_BLUE -> {
                    setPrimaryColor(ResourcesCompat.getColor(resources, R.color.primary_blue, null))
                    setTitleBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary_blue, null))
                    adapterEvents.dateColor = ResourcesCompat.getColor(resources, R.color.primary_blue, null)
                }

                STYLE_GREEN -> {
                    setPrimaryColor(ResourcesCompat.getColor(resources, R.color.primary_green, null))
                    setTitleBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary_green, null))
                    adapterEvents.dateColor = ResourcesCompat.getColor(resources, R.color.primary_green, null)
                }
            }
        }
    }

    fun getActualDay(): Int {
        val principalView = mLinearLayoutManager.findViewByPosition(realLastPosition)
        if(principalView!= null)
            return mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).calendarAdapter.calendar.get(java.util.Calendar.DAY_OF_MONTH)
        else
            return adapter.calendarHash[1]!!.calendar.get(java.util.Calendar.DAY_OF_MONTH)
    }

    fun getActualMonth(): Int {
        val principalView = mLinearLayoutManager.findViewByPosition(realLastPosition)
        if(principalView!= null)
            return mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).calendarAdapter.calendar.get(java.util.Calendar.MONTH)
        else
            return adapter.calendarHash[1]!!.calendar.get(java.util.Calendar.MONTH)
    }

    fun getActualYear(): Int {
        val principalView = mLinearLayoutManager.findViewByPosition(realLastPosition)
        if(principalView!= null)
            return mLinearLayoutManager.findViewByPosition(realLastPosition).findViewById<FlexibleCalendar>(R.id.calendar).calendarAdapter.calendar.get(java.util.Calendar.YEAR)
        else
            return adapter.calendarHash[1]!!.calendar.get(java.util.Calendar.YEAR)
    }

    fun setButtonLeftDrawable(drawable: Drawable?) {
        if(drawable != null) {
            this.mButtonLeftDrawable = drawable
            rootView.findViewById<ImageButton>(R.id.btn_prev_month).setImageDrawable(drawable)
            rootView.findViewById<ImageButton>(R.id.btn_prev_week).setImageDrawable(drawable)
        }
    }

    fun setButtonRightDrawable(drawable: Drawable?) {
        if(drawable != null) {
            this.mButtonRightDrawable = drawable
            rootView.findViewById<ImageButton>(R.id.btn_next_month).setImageDrawable(drawable)
            rootView.findViewById<ImageButton>(R.id.btn_next_week).setImageDrawable(drawable)
        }
    }

    fun fadeInAndShow(img: View) {
        val fadeOut = AlphaAnimation(0f, 1f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 200

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {
                img.visibility = View.VISIBLE
            }
        })

        img.startAnimation(fadeOut)
    }

    fun fadeOutAndHide(img: View) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 200

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                img.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {}
        })

        img.startAnimation(fadeOut)
    }

    override fun onExpand() {
        fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_prev_week))
        fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_next_week))
        fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_prev_month))
        fadeInAndShow(rootView.findViewById<ImageButton>(R.id.btn_next_month))
        if(mListener != null)
            mListener!!.onExpand()
    }

    override fun onCollapse() {
        fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_prev_week))
        fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_next_week))
        fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_prev_month))
        fadeOutAndHide(rootView.findViewById<ImageButton>(R.id.btn_next_month))
        if(mListener != null)
            mListener!!.onCollapse()
    }

    override fun onDaySelect() {
        if(mListener != null)
            mListener!!.onDaySelect()
    }

    override fun onItemClick(v: View?, day: Day) {
        selectedDate = Event(day.year, day.month, day.day)
        if(mShowEvents) {
            loadEvents = true
            eventAdapterList.clear()
            adapterEvents.notifyDataSetChanged()
        }

        if(mListener != null)
            mListener!!.onItemClick(v,day)
    }

    override fun onDataUpdate() {
        if(mListener != null)
            mListener!!.onDataUpdate()
    }

    override fun onMonthChange(nextMonth: Boolean) {
        if(mListener != null)
            mListener!!.onMonthChange(nextMonth)
    }

    override fun onWeekChange(position: Int) {
        if(mListener != null)
            mListener!!.onWeekChange(position)
    }

    fun addEventTag(year: Int, month: Int, day: Int) {
        adapter.calendarHash[(realLastPosition - 1) % 3]!!.addEvent(Event(year, month, day))
        adapter.calendarHash[realLastPosition % 3]!!.addEvent(Event(year, month, day))
        adapter.calendarHash[(realLastPosition + 1) % 3]!!.addEvent(Event(year, month, day))

        adapter.notifyItemChanged(realLastPosition)
    }

    fun removeEventTag(year: Int, month: Int, day: Int){
        adapter.calendarHash[(realLastPosition - 1) % 3]!!.removeEvent(Event(year, month, day))
        adapter.calendarHash[realLastPosition % 3]!!.removeEvent(Event(year, month, day))
        adapter.calendarHash[(realLastPosition + 1) % 3]!!.removeEvent(Event(year, month, day))

        adapter.notifyItemChanged(realLastPosition)
    }

    fun notifySetChangedEvents() {
        adapterEvents.notifyDataSetChanged()
        rootView.findViewById<ProgressBar>(R.id.progress_events).visibility = View.GONE
    }

    fun showLoading(show: Boolean) {
        if(show)
            rootView.findViewById<ProgressBar>(R.id.progress_events).visibility = View.VISIBLE
        else
            rootView.findViewById<ProgressBar>(R.id.progress_events).visibility = View.GONE
    }

    interface RCalendarListener {

        // triggered when a day is selected programmatically or clicked by user.
        fun onDaySelect()

        // triggered only when the views of day on calendar are clicked by user.
        fun onItemClick(v: View?, day: Day)

        // triggered when the data of calendar are updated by changing month or adding events.
        fun onDataUpdate()

        // triggered when the month are changed.
        fun onMonthChange(nextMonth: Boolean?)

        // triggered when the week position are changed.
        fun onWeekChange(position: Int)

        fun onClickDetails(position: Int)

        fun onClickShare(position: Int)

        fun onCollapse()

        fun onExpand()
    }

}