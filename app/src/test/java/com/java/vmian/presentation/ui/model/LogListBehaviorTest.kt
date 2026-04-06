package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.LogType
import com.java.vmian.domain.model.PushLogEntry
import com.java.vmian.domain.model.PushLogType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class LogListBehaviorTest {

    @Test
    fun shouldScrollToLatestOnUpdate_returnsTrue_whenCurrentTabHasContent() {
        assertTrue(LogListBehavior.shouldScrollToLatestOnUpdate(hasContent = true))
    }

    @Test
    fun buildRunningLogLine_includesTimeTypeAndMessage() {
        val timestamp = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val line = LogListBehavior.buildRunningLogLine(
            LogEntry(
                timestamp = timestamp,
                type = LogType.NETWORK,
                message = "连接成功"
            )
        )

        assertEquals("[00:00:00] [网络] 连接成功", line)
    }

    @Test
    fun buildPushLogLine_includesPaymentSummary() {
        val timestamp = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val line = LogListBehavior.buildPushLogLine(
            PushLogEntry(
                timestamp = timestamp,
                type = PushLogType.SUCCESS,
                paymentType = "微信",
                amount = 12.34,
                message = "推送完成",
                isSuccess = true
            )
        )

        assertEquals("[00:00:00] [成功] 微信 ¥12.34 - 推送完成", line)
    }
}
