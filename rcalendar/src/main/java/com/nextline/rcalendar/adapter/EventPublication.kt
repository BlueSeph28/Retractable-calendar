package com.nextline.rcalendar.adapter

import android.app.Activity
import android.graphics.Color
import android.media.Image
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.nextline.rcalendar.R
import com.nextline.rcalendar.widget.RecyclerCalendar
import kotlin.properties.Delegates
import android.view.animation.AlphaAnimation



/**
 * Created by fernando on 21/05/18.
 */


class EventPublication(private val thisActivity: Activity, val adapterList: MutableList<com.nextline.rcalendar.data.EventPublication>, private val recycler: RecyclerView) : RecyclerView.Adapter<EventPublication.MyViewHolder>() {

    interface Handler{
        fun onClickDetails(position: Int)

        fun onClickShare(position: Int)
    }

    fun getEventByPosition(position: Int): com.nextline.rcalendar.data.EventPublication {
        return adapterList[position]
    }

    var listener : Handler? = null
    set(value) {
        if(value != null)
            field = value
    }

    val monthIds = intArrayOf(
            R.string.january,
            R.string.february,
            R.string.march,
            R.string.april,
            R.string.may,
            R.string.june,
            R.string.july,
            R.string.august,
            R.string.september,
            R.string.october,
            R.string.november,
            R.string.december
    )

    var dateColor: Int? = null
        set(color) {
            if(color != null) field = color
        }

    var primaryTitleColor: Int? = null
        set(color) {
            if(color != null) field = color
        }

    var secondaryTitleColor: Int? = null
        set(color) {
            if(color != null) field = color
        }

    var textColorButtons: Int? = null
        set(color) {
            if(color != null) field = color
        }

    var dateTextColor: Int? = null
        set(color) {
            if(color != null) field = color
        }

    var eventBackgroundColor: Int? = null
        set(color) {
            if(color != null) field = color
        }

    var detailsImage: Int? = null
        set(drawable) {
            if(drawable != null) field = drawable
        }

    var shareImage: Int? = null
        set(drawable) {
            if(drawable != null) field = drawable
        }

    var imageLoader: RecyclerCalendar.ImageLoader? = null
    set(loader) {
        if(loader != null) {
            field = loader
        }
    }

    inner class MyViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
        var parentActivity: Activity by Delegates.notNull()
        var title: TextView by Delegates.notNull()
        var location: TextView by Delegates.notNull()
        var image: ImageView by Delegates.notNull()
        var description: TextView by Delegates.notNull()
        var information: TextView by Delegates.notNull()
        var day: TextView by Delegates.notNull()
        var month: TextView by Delegates.notNull()
        var detailsText: TextView by Delegates.notNull()
        var shareText: TextView by Delegates.notNull()
        var eventLayout: LinearLayout by Delegates.notNull()
        var detailsImage: ImageView by Delegates.notNull()
        var shareImage: ImageView by Delegates.notNull()
        var detailsButton: RelativeLayout by Delegates.notNull()
        var shareButton: RelativeLayout by Delegates.notNull()
        var separatorImage: ImageView by Delegates.notNull()
        var notificationSeparator: ImageView by Delegates.notNull()
        var cardSeparator1: ImageView by Delegates.notNull()
        var cardSeparator2: ImageView by Delegates.notNull()


        init {
            super.itemView

            parentActivity = thisActivity
            title = parent.findViewById(R.id.title_principal)
            location = parent.findViewById(R.id.title_location)
            image = parent.findViewById(R.id.event_image)
            description = parent.findViewById(R.id.description)
            information = parent.findViewById(R.id.information)
            day = parent.findViewById(R.id.day_date)
            month = parent.findViewById(R.id.month_date)
            detailsText = parent.findViewById(R.id.details_text)
            shareText = parent.findViewById(R.id.share_text)
            eventLayout = parent.findViewById(R.id.event_layout)
            detailsButton = parent.findViewById<RelativeLayout>(R.id.details_button)
            shareButton = parent.findViewById<RelativeLayout>(R.id.share_button)
            separatorImage = parent.findViewById<ImageView>(R.id.image_separator2)
            notificationSeparator = parent.findViewById<ImageView>(R.id.notification_separator)
            cardSeparator1 = parent.findViewById<ImageView>(R.id.card_separator1)
            cardSeparator2 = parent.findViewById<ImageView>(R.id.card_separator2)

            if(imageLoader == null){
                Log.w("RCalendar", "ImageLoader for events is not defined, if you not define one, the image for the events will not appear")
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventPublication.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if(dateColor != null)
            (holder.day.parent as RelativeLayout).setBackgroundColor(dateColor!!)
        if(dateTextColor != null){
            holder.day.setTextColor(dateTextColor!!)
            holder.month.setTextColor(dateTextColor!!)
        }
        if(primaryTitleColor != null) {
            holder.title.setTextColor(primaryTitleColor!!)
            holder.description.setTextColor(primaryTitleColor!!)
        }
        if(secondaryTitleColor != null) {
            holder.location.setTextColor(secondaryTitleColor!!)
            holder.information.setTextColor(secondaryTitleColor!!)
        }
        if(textColorButtons != null) {
            holder.detailsText.setTextColor(textColorButtons!!)
            holder.shareText.setTextColor(textColorButtons!!)
        }
        if(eventBackgroundColor != null) {
            holder.eventLayout.setBackgroundColor(eventBackgroundColor!!)
            if(eventBackgroundColor != Color.WHITE) {
                holder.cardSeparator1.visibility = View.GONE
                holder.cardSeparator2.visibility = View.GONE
            }
            else{
                holder.cardSeparator1.visibility = View.VISIBLE
                holder.cardSeparator2.visibility = View.VISIBLE
            }
        }

        if(detailsImage != null)
            holder.detailsImage.setImageResource(detailsImage!!)
        if(shareImage != null)
            holder.shareImage.setImageResource(shareImage!!)

        holder.title.setText(adapterList[position].title)
        holder.location.setText(adapterList[position].location)

        if(adapterList[position].imageUrl != null && imageLoader != null) {
            imageLoader!!.loadImage(holder.image, adapterList[position].imageUrl!!)
            holder.image.visibility = View.VISIBLE
            holder.separatorImage.visibility = View.VISIBLE
        }
        else {
            holder.image.visibility = View.GONE
            holder.separatorImage.visibility = View.GONE
        }

        holder.description.setText(adapterList[position].description)
        holder.information.setText(adapterList[position].information)
        holder.day.setText(adapterList[position].day.toString())
        holder.month.setText(holder.parentActivity.getString(monthIds[adapterList[position].month]))

        var noButtons = true

        if(adapterList[position].button1Text != null && adapterList[position].button1Text != "") {
            holder.detailsText.text = adapterList[position].button1Text
            noButtons = false
            holder.detailsButton.visibility = View.VISIBLE
        }
        else {
            holder.detailsButton.visibility = View.GONE
        }

        if(adapterList[position].button2Text != null && adapterList[position].button2Text != "") {
            holder.shareText.text = adapterList[position].button2Text
            noButtons = false
            holder.shareButton.visibility = View.VISIBLE
        }
        else {
            holder.shareButton.visibility = View.GONE
        }

        if(noButtons) {
            holder.notificationSeparator.visibility = View.GONE
        }
        else {
            holder.notificationSeparator.visibility = View.VISIBLE
        }

        if(listener != null) {
            setOnClickFeedback(holder.detailsButton, {
                listener!!.onClickDetails(position)
            })
            setOnClickFeedback(holder.shareButton, {
                listener!!.onClickShare(position)
            })
        }

    }

    private fun setOnClickFeedback(view: View, callback: () -> Unit){
        view.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    val alpha = AlphaAnimation(0.5f, 0.5f)
                    alpha.duration = 0
                    alpha.fillAfter = true
                    view.startAnimation(alpha)
                }

                MotionEvent.ACTION_CANCEL -> {
                    val alpha = AlphaAnimation(1f, 1f)
                    alpha.duration = 0
                    alpha.fillAfter = true
                    view.startAnimation(alpha)
                }

                MotionEvent.ACTION_UP -> {
                    val alpha = AlphaAnimation(1f, 1f)
                    alpha.duration = 0
                    alpha.fillAfter = true
                    view.startAnimation(alpha)
                    callback()
                }
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return adapterList.size
    }
}