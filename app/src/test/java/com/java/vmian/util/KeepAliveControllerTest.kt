package com.java.vmian.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeepAliveControllerTest {

    @Test
    fun matchesTaskBaseActivity_matchesTheCurrentActivityComponent() {
        assertTrue(
            KeepAliveController.matchesTaskBaseActivity(
                taskPackageName = "com.java.vmian",
                taskClassName = "com.java.vmian.MainActivity",
                currentPackageName = "com.java.vmian",
                currentClassName = "com.java.vmian.MainActivity"
            )
        )
        assertFalse(
            KeepAliveController.matchesTaskBaseActivity(
                taskPackageName = "com.java.vmian",
                taskClassName = "com.java.vmian.OtherActivity",
                currentPackageName = "com.java.vmian",
                currentClassName = "com.java.vmian.MainActivity"
            )
        )
    }
}
