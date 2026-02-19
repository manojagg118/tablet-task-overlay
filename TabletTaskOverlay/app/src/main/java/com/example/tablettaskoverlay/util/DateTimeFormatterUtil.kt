package com.example.tablettaskoverlay.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatterUtil {
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun format(epochMillis: Long?): String {
        if (epochMillis == null) return "-"
        return format.format(Date(epochMillis))
    }
}
