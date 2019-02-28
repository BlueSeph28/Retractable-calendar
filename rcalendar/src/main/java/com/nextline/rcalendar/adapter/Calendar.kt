package com.nextline.rcalendar.adapter

import android.app.Activity
import android.graphics.drawable.Drawable
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nextline.rcalendar.R
import com.nextline.rcalendar.data.CalendarAdapter
import com.nextline.rcalendar.data.Day
import com.nextline.rcalendar.widget.FlexibleCalendar
import kotlin.properties.Delegates

/**
 * Created by fernando on 12/05/18.
 */

class Calendar(private val thisActivity: Activity, private val recycler: RecyclerView) : RecyclerView.Adapter<Calendar.MyViewHolder>(), FlexibleCalendar.CalendarListener {
    override fun onCollapse() {
        if(mListener != null)
            mListener!!.onCollapse()
    }

    override fun onExpand() {
        if(mListener != null)
            mListener!!.onExpand()
    }

    override fun onDaySelect() {
        if(mListener != null)
            mListener!!.onDaySelect()
    }

    override fun onItemClick(v: View?, day: Day) {
        if(mListener != null)
            mListener!!.onItemClick(v, day)
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


    // Style
    val STYLE_LIGHT = 0
    val STYLE_PINK = 1
    val STYLE_ORANGE = 2
    val STYLE_BLUE = 3
    val STYLE_GREEN = 4
    private var mListener: AdapterListener? = null

    var calendarHash : HashMap<Int, CalendarAdapter> = HashMap()

    var style: Int? = null
    var showWeek: Boolean? = null
    var firstDayOfWeek: Int? = null
    var state: Int? = null

    var textColor: Int? = null
    var primaryColor: Int? = null
    var titleBackgroundColor: Int? = null

    var todayItemTextColor: Int? = null
    var todayItemBackgroundDrawable: Drawable? = null
    var selectedItemTextColor: Int? = null
    var selectedItemBackgroundDrawable: Drawable? = null

    var buttonLeftDrawable: Drawable? = null
    var buttonRightDrawable: Drawable? = null

    var mSelectedItem: Day? = null
    var listener: FlexibleCalendar.CalendarListener? = null
    var shrinkSelect: Int? = null
    var events: Boolean? = null
    var dayToSet: Day? = null

    init{
        val calTemp = java.util.Calendar.getInstance()
        var adapterTemp = CalendarAdapter(thisActivity, calTemp)

        calendarHash.set(1, adapterTemp)

        val calTemp2 = java.util.Calendar.getInstance()
        calTemp2.add(java.util.Calendar.MONTH, 1)
        var adapterTemp2 = CalendarAdapter(thisActivity, calTemp2)

        calendarHash.set(2, adapterTemp2)

        val calTemp0 = java.util.Calendar.getInstance()
        calTemp0.add(java.util.Calendar.MONTH, -1)
        var adapterTemp0 = CalendarAdapter(thisActivity, calTemp0)

        calendarHash.set(0, adapterTemp0)
    }


    inner class MyViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
        var parentActivity: Activity by Delegates.notNull()
        var calendar: FlexibleCalendar by Delegates.notNull()


        init {
            super.itemView
            parentActivity = thisActivity
            calendar = parent.findViewById(R.id.calendar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Calendar.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.calendar_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var position = position % 3
        var adapter: CalendarAdapter
        adapter = calendarHash[position]!!

        if(holder.calendar.create) {
            holder.calendar.style = style ?: holder.calendar.style
            holder.calendar.isShowWeek = showWeek ?: holder.calendar.isShowWeek
            holder.calendar.firstDayOfWeek = firstDayOfWeek ?: holder.calendar.firstDayOfWeek
            holder.calendar.state = state ?: holder.calendar.state
            holder.calendar.textColor = textColor ?: holder.calendar.textColor
            holder.calendar.primaryColor = primaryColor ?: holder.calendar.primaryColor
            holder.calendar.titleBackgroundColor = titleBackgroundColor ?: holder.calendar.titleBackgroundColor
            holder.calendar.todayItemTextColor = todayItemTextColor ?: holder.calendar.todayItemTextColor
            holder.calendar.todayItemBackgroundDrawable = todayItemBackgroundDrawable ?: holder.calendar.todayItemBackgroundDrawable
            holder.calendar.selectedItemTextColor = selectedItemTextColor ?: holder.calendar.selectedItemTextColor
            holder.calendar.selectedItemBackgroundDrawable = selectedItemBackgroundDrawable ?: holder.calendar.selectedItemBackgroundDrawable
            holder.calendar.buttonLeftDrawable = buttonLeftDrawable ?: holder.calendar.buttonLeftDrawable
            holder.calendar.buttonRightDrawable = buttonRightDrawable ?: holder.calendar.buttonRightDrawable
            holder.calendar.create = false
            holder.calendar.shrinkSelect = shrinkSelect ?: holder.calendar.shrinkSelect
            holder.calendar.events = events ?: holder.calendar.events
            holder.calendar.recycler = recycler
            (holder.calendar.recycler.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            holder.calendar.setCalendarListener(this)
        }

        holder.calendar.setAdapter(adapter)
    }

    override fun getItemCount(): Int {
        return Integer.MAX_VALUE
    }

    interface AdapterListener {

        // triggered when a day is selected programmatically or clicked by user.
        fun onDaySelect()

        // triggered only when the views of day on calendar are clicked by user.
        fun onItemClick(v: View?, day: Day)

        // triggered when the data of calendar are updated by changing month or adding events.
        fun onDataUpdate()

        // triggered when the month are changed.
        fun onMonthChange(nextMonth: Boolean)

        // triggered when the week position are changed.
        fun onWeekChange(position: Int)

        fun onExpand()

        fun onCollapse()

    }

    fun setCalendarListener(listener: AdapterListener) {
        mListener = listener
    }

}
