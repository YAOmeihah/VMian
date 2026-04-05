package com.java.vmian.util

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyUtils {
    fun toAmountCents(amount: Double): Long {
        return BigDecimal.valueOf(amount)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }
}
