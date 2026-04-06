package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo

enum class PermissionCardStatus {
    Granted,
    NeedsSetup,
    Required,
    Recommended,
    Optional
}

enum class PermissionCardAction {
    OpenGuide,
    ReRequest,
    Manage,
    Request,
    OpenSettings
}

data class PermissionCardUiModel(
    val status: PermissionCardStatus,
    val action: PermissionCardAction,
    val showDescription: Boolean
) {
    companion object {
        fun from(permission: PermissionInfo): PermissionCardUiModel {
            val isAutoStartGuide = permission.id.contains("auto_start")

            return PermissionCardUiModel(
                status = when {
                    permission.isGranted -> PermissionCardStatus.Granted
                    isAutoStartGuide -> PermissionCardStatus.NeedsSetup
                    permission.importance == PermissionImportance.REQUIRED -> PermissionCardStatus.Required
                    permission.importance == PermissionImportance.RECOMMENDED -> PermissionCardStatus.Recommended
                    else -> PermissionCardStatus.Optional
                },
                action = when {
                    isAutoStartGuide -> PermissionCardAction.OpenGuide
                    permission.isGranted && permission.canRequest -> PermissionCardAction.ReRequest
                    permission.isGranted -> PermissionCardAction.Manage
                    permission.canRequest -> PermissionCardAction.Request
                    else -> PermissionCardAction.OpenSettings
                },
                showDescription = true
            )
        }
    }
}
