package com.java.vmian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.java.vmian.domain.usecase.ConfigUseCase
import com.java.vmian.domain.usecase.CheckForUpdateUseCase
import com.java.vmian.domain.usecase.IgnoreUpdateVersionUseCase
import com.java.vmian.domain.usecase.PaymentUseCase
import com.java.vmian.update.AppUpdateCoordinator
import com.java.vmian.util.LogManager
import com.java.vmian.util.PushLogManager

/**
 * MainViewModel工厂类
 */
class MainViewModelFactory(
    private val configUseCase: ConfigUseCase,
    private val paymentUseCase: PaymentUseCase,
    private val logManager: LogManager,
    private val pushLogManager: PushLogManager,
    private val checkForUpdateUseCase: CheckForUpdateUseCase,
    private val ignoreUpdateVersionUseCase: IgnoreUpdateVersionUseCase,
    private val appUpdateCoordinator: AppUpdateCoordinator
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                configUseCase = configUseCase,
                paymentUseCase = paymentUseCase,
                logManager = logManager,
                pushLogManager = pushLogManager,
                checkForUpdateUseCase = checkForUpdateUseCase,
                ignoreUpdateVersionUseCase = ignoreUpdateVersionUseCase,
                appUpdateCoordinator = appUpdateCoordinator
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
