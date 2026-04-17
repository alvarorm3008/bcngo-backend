package com.example.bcngo.screens.searchEvents

import android.content.Context
import android.content.Intent
import com.example.bcngo.R
import com.example.bcngo.model.Event
import kotlinx.datetime.toInstant
import kotlinx.datetime.TimeZone

fun shareEvent(context: Context, event: Event) {
    val message = buildString {
        append(context.getString(R.string.hello_share))
        append("\n")
        append(context.getString(R.string.event_invitation, event.name))
        append("\n")
        append(context.getString(R.string.event_date_label))
        append(String.format(context.getString(R.string.event_date),
            event.start_date.dayOfMonth,
            event.start_date.month.name.take(3),
            event.start_date.year))
        append(context.getString(R.string.event_until))
        append(String.format(context.getString(R.string.event_date),
            event.end_date.dayOfMonth,
            event.end_date.month.name.take(3),
            event.end_date.year))
        append("\n")
        if (!event.road_name.isNullOrEmpty() || !event.street_number.isNullOrEmpty() || !event.district.isNullOrEmpty()) {
            append(context.getString(R.string.event_place_label))
            if (!event.road_name.isNullOrEmpty()) {
                append(event.road_name)
                if (!event.street_number.isNullOrEmpty()) {
                    append(", ${event.street_number}")
                }
            } else if (!event.street_number.isNullOrEmpty()) {
                append(event.street_number)
            } else if (!event.district.isNullOrEmpty()) {
                append(event.district)
            }
            append("\n")
        }
        append(context.getString(R.string.dont_miss))
    }

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

fun addToCalendar(context: Context, event: Event) {
    val startEpochMillis = event.start_date
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()

    val endEpochMillis = event.end_date
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()

    val intent = Intent(Intent.ACTION_INSERT).apply {
        type = "vnd.android.cursor.item/event"
        putExtra("title", event.name)
        if (!event.road_name.isNullOrEmpty() || !event.street_number.isNullOrEmpty()) {
            putExtra("eventLocation", "${event.road_name}, ${event.street_number}")
        }
        putExtra("beginTime", startEpochMillis)
        putExtra("endTime", endEpochMillis)
    }
    context.startActivity(intent)
}
