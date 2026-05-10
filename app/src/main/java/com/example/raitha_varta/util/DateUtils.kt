package com.example.raitha_varta.util

import android.text.format.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    fun getTimeAgo(time: Long): String {
        val now = System.currentTimeMillis()
        return DateUtils.getRelativeTimeSpanString(
            time,
            now,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    fun formatDayAndDate(time: Long, locale: Locale = Locale.getDefault()): String {
        return try {
            val dt = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", locale).format(dt)
        } catch (_: Exception) {
            ""
        }
    }
}
