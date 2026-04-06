package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.PushLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogListBehavior {
    fun shouldScrollToLatestOnUpdate(hasContent: Boolean): Boolean {
        return hasContent
    }

    fun buildRunningLogLine(log: LogEntry): String {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
        return "[$time] [${log.type.prefix}] ${log.message}"
    }

    fun buildPushLogLine(log: PushLogEntry): String {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
        return buildString {
            append("[$time] [${log.type.prefix}] ${log.paymentType} ¥${log.amount}")
            if (log.message.isNotEmpty()) {
                append(" - ${log.message}")
            }
        }
    }
}
