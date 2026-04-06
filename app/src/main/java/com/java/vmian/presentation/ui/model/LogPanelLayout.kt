package com.java.vmian.presentation.ui.model

object LogPanelLayout {
    private const val MIN_CARD_HEIGHT_DP = 320
    private const val MAX_CARD_HEIGHT_DP = 520
    private const val BODY_MIN_HEIGHT_DP = 220
    private const val HEADER_RESERVED_HEIGHT_DP = 120

    fun resolveCardHeightDp(screenHeightDp: Int): Int {
        return (screenHeightDp * 0.46f).toInt().coerceIn(MIN_CARD_HEIGHT_DP, MAX_CARD_HEIGHT_DP)
    }

    fun resolveBodyHeightDp(cardHeightDp: Int): Int {
        return (cardHeightDp - HEADER_RESERVED_HEIGHT_DP).coerceAtLeast(BODY_MIN_HEIGHT_DP)
    }
}
