package com.nextline.rcalendar.data

import android.widget.ImageView

/**
 * Created by fernando on 21/05/18.
 */



data class EventPublication(val day: Int, val month: Int, val title: String, val location: String, val description: String, val information: String, val imageUrl: String?, val eventId: Int, val button1Text: String?, val button2Text: String?)