package com.samueljuma.firebaseinaction.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFullTimestampString(): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy h:mm a", Locale.getDefault())
    return sdf.format(Date(this)).replace("AM", "am").replace("PM", "pm")
}

fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(this))
        }
    }
}