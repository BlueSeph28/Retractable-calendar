package com.nextline.retractable_calendar_nxt

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.nextline.rcalendar.data.Day
import com.nextline.rcalendar.data.Event
import com.nextline.rcalendar.data.EventPublication
import com.nextline.rcalendar.widget.RecyclerCalendar



class MainActivity : AppCompatActivity(), RecyclerCalendar.RCalendarListener {
    override fun onDaySelect() {}

    override fun onItemClick(v: View?, day: Day) {
        main_calendar.showLoading(true)
        println("Events visbility: " + main_calendar.eventsInflated.visibility + " isVisibile: " + View.VISIBLE)
        val handler = Handler()
        handler.postDelayed(Runnable {
            main_calendar.addEvent(EventPublication(28, 5, "Prueba", "Lugar", "Descripcion", "Información", "",2,"Detalles","Compatir"))
            main_calendar.notifySetChangedEvents()
            main_calendar.showLoading(false)
        }, 1000)
    }

    override fun onDataUpdate() {}

    override fun onMonthChange(nextMonth: Boolean?) {}

    override fun onWeekChange(position: Int) {}

    override fun onClickDetails(position: Int) {

    }

    override fun onClickShare(position: Int) {
        main_calendar.removeEventByPosition(position)
        main_calendar.refreshEvents()
    }

    override fun onCollapse() {}

    override fun onExpand() {}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_calendar.setListener(this)
        main_calendar.imageLoader = object:RecyclerCalendar.ImageLoader(){
            override fun loadImage(image: ImageView, url: String) {
            }

        }
        main_calendar.addEventTag(2018, 6, 28)
    }

}
