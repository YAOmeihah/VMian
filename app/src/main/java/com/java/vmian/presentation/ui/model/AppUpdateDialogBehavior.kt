package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.AppUpdateState

enum class AppUpdateDialogStage {
    None,
    Available,
    Downloading,
    Result
}

object AppUpdateDialogBehavior {
    fun stageOf(state: AppUpdateState): AppUpdateDialogStage = when (state) {
        is AppUpdateState.UpdateAvailable -> AppUpdateDialogStage.Available
        is AppUpdateState.Downloading -> AppUpdateDialogStage.Downloading
        is AppUpdateState.Downloaded,
        is AppUpdateState.Installing,
        is AppUpdateState.Failed -> AppUpdateDialogStage.Result
        AppUpdateState.Idle,
        AppUpdateState.Checking,
        is AppUpdateState.Completed -> AppUpdateDialogStage.None
    }

    fun shouldAutoShowDialog(
        previousState: AppUpdateState?,
        currentState: AppUpdateState,
        suppressDownloadingDialog: Boolean
    ): Boolean {
        val currentStage = stageOf(currentState)
        if (currentStage == AppUpdateDialogStage.None) return false

        val previousStage = previousState?.let(::stageOf) ?: AppUpdateDialogStage.None
        return when (currentStage) {
            AppUpdateDialogStage.Downloading ->
                previousStage != AppUpdateDialogStage.Downloading && !suppressDownloadingDialog

            else -> previousStage != currentStage
        }
    }
}
