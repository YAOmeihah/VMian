package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionCardUiModelTest {

    @Test
    fun from_showsDescription_forGrantedStandardPermission() {
        val model = PermissionCardUiModel.from(
            permission(
                id = "camera",
                isGranted = true,
                canRequest = true,
                importance = PermissionImportance.REQUIRED
            )
        )

        assertEquals(PermissionCardStatus.Granted, model.status)
        assertEquals(PermissionCardAction.ReRequest, model.action)
        assertTrue(model.showDescription)
    }

    @Test
    fun from_showsDescription_forMissingRequiredPermission() {
        val model = PermissionCardUiModel.from(
            permission(
                id = "battery",
                isGranted = false,
                canRequest = false,
                importance = PermissionImportance.REQUIRED
            )
        )

        assertEquals(PermissionCardStatus.Required, model.status)
        assertEquals(PermissionCardAction.OpenSettings, model.action)
        assertTrue(model.showDescription)
    }

    @Test
    fun from_usesGuideAction_forAutoStartPermission() {
        val model = PermissionCardUiModel.from(
            permission(
                id = "manufacturer_auto_start",
                isGranted = false,
                canRequest = false,
                importance = PermissionImportance.RECOMMENDED
            )
        )

        assertEquals(PermissionCardStatus.NeedsSetup, model.status)
        assertEquals(PermissionCardAction.OpenGuide, model.action)
        assertTrue(model.showDescription)
    }

    private fun permission(
        id: String,
        isGranted: Boolean,
        canRequest: Boolean,
        importance: PermissionImportance
    ) = PermissionInfo(
        id = id,
        name = "示例权限",
        description = "示例说明",
        importance = importance,
        isGranted = isGranted,
        canRequest = canRequest
    )
}
