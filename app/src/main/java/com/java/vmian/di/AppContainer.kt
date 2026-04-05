package com.java.vmian.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.java.vmian.data.remote.PaymentApiService
import com.java.vmian.data.repository.ConfigRepositoryImpl
import com.java.vmian.data.repository.LogRepositoryImpl
import com.java.vmian.data.repository.PaymentRepositoryImpl
import com.java.vmian.domain.repository.ConfigRepository
import com.java.vmian.domain.repository.LogRepository
import com.java.vmian.domain.repository.PaymentRepository
import com.java.vmian.domain.usecase.ConfigUseCase
import com.java.vmian.domain.usecase.PaymentUseCase
import com.java.vmian.util.LogManager
import com.java.vmian.util.PushLogManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 简单的依赖注入容器
 */
class AppContainer(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vmq_settings")
    
    // Network
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/") // 占位符，实际URL在API调用时动态指定
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val paymentApiService: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }
    
    // Repositories
    val configRepository: ConfigRepository by lazy {
        ConfigRepositoryImpl(context.dataStore)
    }

    val logRepository: LogRepository by lazy {
        LogRepositoryImpl(context.dataStore)
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepositoryImpl(paymentApiService, configRepository)
    }
    
    // Log Managers
    val logManager: LogManager by lazy {
        LogManager.getInstance(logRepository)
    }

    val pushLogManager: PushLogManager by lazy {
        PushLogManager.getInstance(logRepository)
    }

    // Use Cases
    val configUseCase: ConfigUseCase by lazy {
        ConfigUseCase(configRepository)
    }

    val paymentUseCase: PaymentUseCase by lazy {
        PaymentUseCase(paymentRepository, configRepository)
    }
}
