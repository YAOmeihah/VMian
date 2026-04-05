# App Push Replay Protection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move the Android monitor app's `/appPush` flow to an HTTPS-only signed POST contract that uses `ts + nonce + eventId + sign` and aligns with the backend `MonitorReplayGuard`.

**Architecture:** Keep notification recognition and amount-based order matching unchanged. Generate a stable `eventId` when a payment notification is recognized, generate a fresh `nonce` on each push attempt, convert the amount to integer cents, and sign `type|amountCents|ts|nonce|eventId` with HMAC-SHA256 using a dedicated `monitorKey`. Treat backend replay protection as a release dependency: `MonitorReplayGuard` must validate the signature, enforce a 300-second timestamp window, reject reused nonces, and return an idempotent duplicate response for previously processed `eventId` values.

**Tech Stack:** Kotlin, Android DataStore, Retrofit, Gson, JUnit4, kotlinx.coroutines

---

## External Contract Required Before App Release

The backend must accept this request on `POST /appPush` before the Android changes are released:

```json
{
  "type": 2,
  "amountCents": 1023,
  "ts": 1710000000123,
  "nonce": "nonce_manual_001",
  "eventId": "evt_manual_001",
  "sign": "c4ec0e6b8fdaea8f8dc99cbe345718b3c78f8e23bb754d7b670f18f293978ef1"
}
```

`MonitorReplayGuard` must implement these exact rules:

1. Require all fields: `type`, `amountCents`, `ts`, `nonce`, `eventId`, `sign`.
2. Verify `sign == HMAC_SHA256(type|amountCents|ts|nonce|eventId, monitorKey)`.
3. Reject requests where `abs(server_now_ms - ts) > 300000`.
4. Reject reused `nonce` values within the replay window as a replay attack.
5. Treat `eventId` as the business idempotency key: same `eventId` with a new `nonce` must not credit again.
6. Return an idempotent duplicate response for repeated `eventId` values so the app can stop retrying.
7. Preserve the existing `code/msg/data` response shape consumed by `VmqApiResponse<T>`.

`eventId` and `nonce` have different responsibilities and must stay separate:

- `eventId`: one payment event, stable across retries
- `nonce`: one HTTP request attempt, regenerated on each retry

## File Map

**Create**
- `app/src/main/java/com/java/vmian/domain/model/PaymentPushPayload.kt` - domain payload passed from use case to repository.
- `app/src/main/java/com/java/vmian/data/remote/dto/PushPaymentRequestDto.kt` - Retrofit request body for `/appPush`.
- `app/src/main/java/com/java/vmian/data/remote/SecureEndpointBuilder.kt` - HTTPS-only URL normalization for dynamic endpoints.
- `app/src/main/java/com/java/vmian/util/MoneyUtils.kt` - amount-to-cents normalization shared by app push signing and tests.
- `app/src/test/java/com/java/vmian/util/CryptoUtilsTest.kt`
- `app/src/test/java/com/java/vmian/util/MoneyUtilsTest.kt`
- `app/src/test/java/com/java/vmian/domain/usecase/PaymentUseCaseTest.kt`
- `app/src/test/java/com/java/vmian/domain/usecase/ConfigUseCaseTest.kt`
- `app/src/test/java/com/java/vmian/data/remote/SecureEndpointBuilderTest.kt`

**Modify**
- `app/src/main/java/com/java/vmian/util/CryptoUtils.kt`
- `app/src/main/java/com/java/vmian/domain/model/PaymentConfig.kt`
- `app/src/main/java/com/java/vmian/domain/model/PaymentNotification.kt`
- `app/src/main/java/com/java/vmian/domain/repository/PaymentRepository.kt`
- `app/src/main/java/com/java/vmian/data/repository/ConfigRepositoryImpl.kt`
- `app/src/main/java/com/java/vmian/domain/usecase/PaymentUseCase.kt`
- `app/src/main/java/com/java/vmian/data/remote/PaymentApiService.kt`
- `app/src/main/java/com/java/vmian/data/repository/PaymentRepositoryImpl.kt`
- `app/src/main/java/com/java/vmian/domain/usecase/ConfigUseCase.kt`
- `app/src/main/java/com/java/vmian/presentation/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/ManualConfigDialog.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/ConfigInfoCard.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/ScanningOverlay.kt`
- `app/src/main/java/com/java/vmian/service/PaymentNotificationService.kt`

### Task 1: Add deterministic signing and amount normalization helpers

**Files:**
- Create: `app/src/main/java/com/java/vmian/util/MoneyUtils.kt`
- Modify: `app/src/main/java/com/java/vmian/util/CryptoUtils.kt`
- Test: `app/src/test/java/com/java/vmian/util/CryptoUtilsTest.kt`
- Test: `app/src/test/java/com/java/vmian/util/MoneyUtilsTest.kt`

- [ ] **Step 1: Write the failing helper tests**

```kotlin
package com.java.vmian.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoUtilsTest {
    @Test
    fun generateHmacSha256_returnsExpectedHexDigest() {
        val digest = CryptoUtils.generateHmacSha256(
            input = "2|123|1710000000000|nonce_123|evt_123",
            key = "secret"
        )

        assertEquals(
            "4c953a2a71c3bc222652b18e6b28db64f0b1a2bf86a354e54689d3a3977236bb",
            digest
        )
    }
}
```

```kotlin
package com.java.vmian.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyUtilsTest {
    @Test
    fun toAmountCents_roundsToFenUsingHalfUp() {
        assertEquals(1L, MoneyUtils.toAmountCents(0.01))
        assertEquals(100L, MoneyUtils.toAmountCents(1.00))
        assertEquals(1023L, MoneyUtils.toAmountCents(10.23))
        assertEquals(1024L, MoneyUtils.toAmountCents(10.235))
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.util.CryptoUtilsTest" --tests "com.java.vmian.util.MoneyUtilsTest"
```

Expected: FAIL because `generateHmacSha256` and `MoneyUtils` do not exist yet.

- [ ] **Step 3: Implement the helpers**

```kotlin
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
```

```kotlin
package com.java.vmian.util

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    fun generateMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun generateHmacSha256(input: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.util.CryptoUtilsTest" --tests "com.java.vmian.util.MoneyUtilsTest"
```

Expected: PASS with 2 executed tests and no failures.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/java/vmian/util/CryptoUtils.kt app/src/main/java/com/java/vmian/util/MoneyUtils.kt app/src/test/java/com/java/vmian/util/CryptoUtilsTest.kt app/src/test/java/com/java/vmian/util/MoneyUtilsTest.kt
git commit -m "feat: add monitor signing helpers"
```

### Task 2: Build a replay-safe payment payload with stable event IDs and per-attempt nonces

**Files:**
- Create: `app/src/main/java/com/java/vmian/domain/model/PaymentPushPayload.kt`
- Modify: `app/src/main/java/com/java/vmian/domain/model/PaymentNotification.kt`
- Modify: `app/src/main/java/com/java/vmian/domain/repository/PaymentRepository.kt`
- Modify: `app/src/main/java/com/java/vmian/domain/usecase/PaymentUseCase.kt`
- Modify: `app/src/main/java/com/java/vmian/service/PaymentNotificationService.kt`
- Test: `app/src/test/java/com/java/vmian/domain/usecase/PaymentUseCaseTest.kt`

- [ ] **Step 1: Write the failing use case test**

```kotlin
package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.model.PaymentNotification
import com.java.vmian.domain.model.PaymentType
import com.java.vmian.domain.model.PaymentPushPayload
import com.java.vmian.domain.repository.ConfigRepository
import com.java.vmian.domain.repository.PaymentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentUseCaseTest {

    @Test
    fun pushPayment_buildsSignedPayloadWithNonceAndEventId() = runBlocking {
        val paymentRepository = RecordingPaymentRepository()
        val configRepository = FixedConfigRepository(
            PaymentConfig("https://example.com", "secret", true)
        )
        val useCase = PaymentUseCase(
            paymentRepository = paymentRepository,
            configRepository = configRepository,
            currentTimeMillis = { 1710000000000L },
            nonceFactory = { "nonce_123" }
        )

        val notification = PaymentNotification(
            type = PaymentType.ALIPAY,
            amount = 10.23,
            timestamp = 1709999999000L,
            packageName = "com.eg.android.AlipayGphone",
            title = "收款成功",
            content = "收款10.23元",
            eventId = "evt_123"
        )

        useCase.pushPayment(notification)

        assertEquals(
            PaymentPushPayload(
                type = 2,
                amountCents = 1023L,
                timestamp = 1710000000000L,
                nonce = "nonce_123",
                eventId = "evt_123",
                sign = "725b13fe5235b39bb0051647a4e9f1edf3732839002303b3f71f06423974744a"
            ),
            paymentRepository.lastPayload
        )
    }

    private class RecordingPaymentRepository : PaymentRepository {
        var lastPayload: PaymentPushPayload? = null

        override suspend fun sendHeartbeat(timestamp: Long, sign: String): ApiResponse<String> {
            return ApiResponse.Success("ok")
        }

        override suspend fun pushPayment(payload: PaymentPushPayload): ApiResponse<String> {
            lastPayload = payload
            return ApiResponse.Success("ok")
        }
    }

    private class FixedConfigRepository(
        private val config: PaymentConfig
    ) : ConfigRepository {
        override suspend fun saveConfig(config: PaymentConfig) = Unit
        override suspend fun getConfig(): PaymentConfig = config
        override suspend fun clearConfig() = Unit
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.domain.usecase.PaymentUseCaseTest"
```

Expected: FAIL because `PaymentPushPayload.nonce`, `nonceFactory`, and the new signature format do not exist yet.

- [ ] **Step 3: Implement payload creation**

```kotlin
package com.java.vmian.domain.model

data class PaymentPushPayload(
    val type: Int,
    val amountCents: Long,
    val timestamp: Long,
    val nonce: String,
    val eventId: String,
    val sign: String
)
```

```kotlin
package com.java.vmian.domain.model

data class PaymentNotification(
    val type: PaymentType,
    val amount: Double,
    val timestamp: Long,
    val packageName: String,
    val title: String,
    val content: String,
    val eventId: String
)
```

```kotlin
class PaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val configRepository: ConfigRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val nonceFactory: () -> String = { java.util.UUID.randomUUID().toString() }
) {
    suspend fun pushPayment(notification: PaymentNotification): ApiResponse<String> {
        val config = configRepository.getConfig() ?: return ApiResponse.Error("配置未设置")
        val timestamp = currentTimeMillis()
        val nonce = nonceFactory()
        val amountCents = MoneyUtils.toAmountCents(notification.amount)
        val signingText =
            "${notification.type.value}|$amountCents|$timestamp|$nonce|${notification.eventId}"
        val sign = CryptoUtils.generateHmacSha256(signingText, config.monitorKey)

        return paymentRepository.pushPayment(
            PaymentPushPayload(
                type = notification.type.value,
                amountCents = amountCents,
                timestamp = timestamp,
                nonce = nonce,
                eventId = notification.eventId,
                sign = sign
            )
        )
    }
}
```

```kotlin
interface PaymentRepository {
    suspend fun sendHeartbeat(timestamp: Long, sign: String): ApiResponse<String>
    suspend fun pushPayment(payload: PaymentPushPayload): ApiResponse<String>
}
```

```kotlin
val paymentNotification = PaymentNotification(
    type = paymentType,
    amount = amount,
    timestamp = System.currentTimeMillis(),
    packageName = packageName,
    title = title,
    content = content,
    eventId = java.util.UUID.randomUUID().toString()
)
```

- [ ] **Step 4: Run the test to verify it passes**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.domain.usecase.PaymentUseCaseTest"
```

Expected: PASS with `PaymentUseCaseTest` green.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/java/vmian/domain/model/PaymentPushPayload.kt app/src/main/java/com/java/vmian/domain/model/PaymentNotification.kt app/src/main/java/com/java/vmian/domain/repository/PaymentRepository.kt app/src/main/java/com/java/vmian/domain/usecase/PaymentUseCase.kt app/src/main/java/com/java/vmian/service/PaymentNotificationService.kt app/src/test/java/com/java/vmian/domain/usecase/PaymentUseCaseTest.kt
git commit -m "feat: build nonce-based app push payloads"
```

### Task 3: Switch `/appPush` to HTTPS-only POST JSON

**Files:**
- Create: `app/src/main/java/com/java/vmian/data/remote/SecureEndpointBuilder.kt`
- Create: `app/src/main/java/com/java/vmian/data/remote/dto/PushPaymentRequestDto.kt`
- Modify: `app/src/main/java/com/java/vmian/data/remote/PaymentApiService.kt`
- Modify: `app/src/main/java/com/java/vmian/data/repository/PaymentRepositoryImpl.kt`
- Test: `app/src/test/java/com/java/vmian/data/remote/SecureEndpointBuilderTest.kt`

- [ ] **Step 1: Write the failing HTTPS builder tests**

```kotlin
package com.java.vmian.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class SecureEndpointBuilderTest {

    @Test
    fun buildAppPushUrl_acceptsExplicitHttpsHost() {
        assertEquals(
            "https://example.com/appPush",
            SecureEndpointBuilder.build(host = "https://example.com", path = "/appPush")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun buildAppPushUrl_rejectsHttpHost() {
        SecureEndpointBuilder.build(host = "http://example.com", path = "/appPush")
    }

    @Test(expected = IllegalArgumentException::class)
    fun buildAppPushUrl_rejectsHostWithoutScheme() {
        SecureEndpointBuilder.build(host = "example.com", path = "/appPush")
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.data.remote.SecureEndpointBuilderTest"
```

Expected: FAIL because `SecureEndpointBuilder` does not exist.

- [ ] **Step 3: Implement HTTPS endpoint building and POST transport**

```kotlin
package com.java.vmian.data.remote

object SecureEndpointBuilder {
    fun build(host: String, path: String): String {
        require(host.startsWith("https://")) { "服务器地址必须使用 HTTPS" }
        val normalizedHost = host.trimEnd('/')
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return normalizedHost + normalizedPath
    }
}
```

```kotlin
package com.java.vmian.data.remote.dto

data class PushPaymentRequestDto(
    val type: Int,
    val amountCents: Long,
    val ts: Long,
    val nonce: String,
    val eventId: String,
    val sign: String
)
```

```kotlin
interface PaymentApiService {
    @GET
    suspend fun sendHeartbeat(
        @Url url: String,
        @Query("t") timestamp: Long,
        @Query("sign") sign: String
    ): Response<VmqApiResponse<Any?>>

    @POST
    suspend fun pushPayment(
        @Url url: String,
        @Body body: PushPaymentRequestDto
    ): Response<VmqApiResponse<Any?>>
}
```

```kotlin
override suspend fun pushPayment(payload: PaymentPushPayload): ApiResponse<String> {
    return try {
        val config = configRepository.getConfig() ?: return ApiResponse.Error("配置未设置")
        val url = SecureEndpointBuilder.build(config.host, "/appPush")
        val request = PushPaymentRequestDto(
            type = payload.type,
            amountCents = payload.amountCents,
            ts = payload.timestamp,
            nonce = payload.nonce,
            eventId = payload.eventId,
            sign = payload.sign
        )

        val response = apiService.pushPayment(url, request)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.isSuccess()) {
                ApiResponse.Success(body.message)
            } else {
                ApiResponse.Error("推送失败: ${body?.getErrorMessage() ?: "未知错误"}")
            }
        } else {
            ApiResponse.Error("推送失败: HTTP ${response.code()}")
        }
    } catch (e: IllegalArgumentException) {
        ApiResponse.Error(e.message ?: "服务器地址必须使用 HTTPS")
    } catch (e: Exception) {
        ApiResponse.Error("网络错误: ${e.message}")
    }
}
```

- [ ] **Step 4: Run the transport tests and full unit suite**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.data.remote.SecureEndpointBuilderTest"
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS. The second command should finish with `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/java/vmian/data/remote/SecureEndpointBuilder.kt app/src/main/java/com/java/vmian/data/remote/dto/PushPaymentRequestDto.kt app/src/main/java/com/java/vmian/data/remote/PaymentApiService.kt app/src/main/java/com/java/vmian/data/repository/PaymentRepositoryImpl.kt app/src/test/java/com/java/vmian/data/remote/SecureEndpointBuilderTest.kt
git commit -m "feat: send app push through monitor replay guard contract"
```

### Task 4: Rename the app config to `monitorKey` and enforce HTTPS

**Files:**
- Modify: `app/src/main/java/com/java/vmian/domain/model/PaymentConfig.kt`
- Modify: `app/src/main/java/com/java/vmian/data/repository/ConfigRepositoryImpl.kt`
- Modify: `app/src/main/java/com/java/vmian/domain/usecase/ConfigUseCase.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/viewmodel/MainViewModel.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/ManualConfigDialog.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/ConfigInfoCard.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/ScanningOverlay.kt`
- Test: `app/src/test/java/com/java/vmian/domain/usecase/ConfigUseCaseTest.kt`

- [ ] **Step 1: Write the failing config validation tests**

```kotlin
package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.repository.ConfigRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigUseCaseTest {

    @Test
    fun saveConfig_rejectsHttpHost() = runBlocking {
        val result = ConfigUseCase(InMemoryConfigRepository())
            .saveConfig("http://example.com", "secret")

        assertTrue(result.isFailure)
    }

    @Test
    fun saveConfig_persistsMonitorKey() = runBlocking {
        val repository = InMemoryConfigRepository()
        val result = ConfigUseCase(repository)
            .saveConfig("https://example.com", "secret")

        assertTrue(result.isSuccess)
        assertEquals(
            PaymentConfig("https://example.com", "secret", true),
            repository.savedConfig
        )
    }

    private class InMemoryConfigRepository : ConfigRepository {
        var savedConfig: PaymentConfig? = null

        override suspend fun saveConfig(config: PaymentConfig) {
            savedConfig = config
        }

        override suspend fun getConfig(): PaymentConfig? = savedConfig
        override suspend fun clearConfig() = Unit
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.domain.usecase.ConfigUseCaseTest"
```

Expected: FAIL because `saveConfig` currently accepts any host string and the config model still uses the old key naming.

- [ ] **Step 3: Implement the config rename and validation**

```kotlin
package com.java.vmian.domain.model

data class PaymentConfig(
    val host: String,
    val monitorKey: String,
    val isConfigured: Boolean = false
)
```

```kotlin
class ConfigRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ConfigRepository {

    private val hostKey = stringPreferencesKey("host")
    private val monitorKeyKey = stringPreferencesKey("monitor_key")

    override suspend fun saveConfig(config: PaymentConfig) {
        dataStore.edit { preferences ->
            preferences[hostKey] = config.host
            preferences[monitorKeyKey] = config.monitorKey
        }
    }

    override suspend fun getConfig(): PaymentConfig? {
        return dataStore.data.map { preferences ->
            val host = preferences[hostKey] ?: ""
            val monitorKey = preferences[monitorKeyKey] ?: ""
            if (host.isNotEmpty() && monitorKey.isNotEmpty()) {
                PaymentConfig(host, monitorKey, true)
            } else null
        }.first()
    }
}
```

```kotlin
class ConfigUseCase(
    private val configRepository: ConfigRepository
) {
    suspend fun saveConfig(host: String, monitorKey: String): Result<Unit> {
        return try {
            require(host.startsWith("https://")) { "服务器地址必须以 https:// 开头" }
            require(monitorKey.isNotBlank()) { "监控密钥不能为空" }
            configRepository.saveConfig(
                PaymentConfig(host.trim(), monitorKey.trim(), true)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

```kotlin
fun saveConfig(host: String, monitorKey: String) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        val result = configUseCase.saveConfig(host, monitorKey)
        if (result.isSuccess) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    config = PaymentConfig(host.trim(), monitorKey.trim(), true),
                    isConfigured = true,
                    message = "配置保存成功"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = result.exceptionOrNull()?.message ?: "配置保存失败"
                )
            }
        }
    }
}
```

```kotlin
OutlinedTextField(
    value = key,
    onValueChange = { key = it },
    label = { Text("监控密钥") },
    placeholder = { Text("请输入 monitorKey") },
    modifier = Modifier.fillMaxWidth()
)
```

```kotlin
Text(text = "监控地址: ${config?.host ?: "请扫码配置"}")
Text(text = "监控密钥: ${if (config?.monitorKey?.isNotEmpty() == true) "已配置" else "请扫码配置"}")
```

```kotlin
text = "支持格式: host/monitorKey"
```

- [ ] **Step 4: Run the config validation test and the full unit suite**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.java.vmian.domain.usecase.ConfigUseCaseTest"
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/java/vmian/domain/model/PaymentConfig.kt app/src/main/java/com/java/vmian/data/repository/ConfigRepositoryImpl.kt app/src/main/java/com/java/vmian/domain/usecase/ConfigUseCase.kt app/src/main/java/com/java/vmian/presentation/viewmodel/MainViewModel.kt app/src/main/java/com/java/vmian/presentation/ui/components/ManualConfigDialog.kt app/src/main/java/com/java/vmian/presentation/ui/components/ConfigInfoCard.kt app/src/main/java/com/java/vmian/presentation/ui/components/ScanningOverlay.kt app/src/test/java/com/java/vmian/domain/usecase/ConfigUseCaseTest.kt
git commit -m "feat: store monitor key in app configuration"
```

### Task 5: Verify `MonitorReplayGuard` behavior against the backend

**Files:**
- Modify: none in this repo
- Test: backend integration environment that exposes `POST /appPush`

- [ ] **Step 1: Confirm the backend accepts the new JSON contract**

Run:

```powershell
if (-not $env:VMQ_MONITOR_KEY) { throw "Set VMQ_MONITOR_KEY before running this check." }
if (-not $env:VMQ_SERVER_URL) { throw "Set VMQ_SERVER_URL before running this check." }

$monitorKey = $env:VMQ_MONITOR_KEY
$signingText = "2|1023|1710000000123|nonce_manual_001|evt_manual_001"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($monitorKey))
$sign = ($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($signingText)) | ForEach-Object { $_.ToString("x2") }) -join ""

$body = @{
  type = 2
  amountCents = 1023
  ts = 1710000000123
  nonce = "nonce_manual_001"
  eventId = "evt_manual_001"
  sign = $sign
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$($env:VMQ_SERVER_URL.TrimEnd('/'))/appPush" -ContentType "application/json" -Body $body
```

Expected: JSON response in the same `code/msg/data` format consumed by `VmqApiResponse`.

- [ ] **Step 2: Verify nonce replay rejection**

Run the Step 1 command twice without changing any field.

Expected: first call succeeds, second call is rejected as a replay because `nonce_manual_001` is reused.

- [ ] **Step 3: Verify event idempotency with a fresh nonce**

Run:

```powershell
if (-not $env:VMQ_MONITOR_KEY) { throw "Set VMQ_MONITOR_KEY before running this check." }
if (-not $env:VMQ_SERVER_URL) { throw "Set VMQ_SERVER_URL before running this check." }

$monitorKey = $env:VMQ_MONITOR_KEY
$signingText = "2|1023|1710000000123|nonce_manual_002|evt_manual_001"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($monitorKey))
$sign = ($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($signingText)) | ForEach-Object { $_.ToString("x2") }) -join ""

$body = @{
  type = 2
  amountCents = 1023
  ts = 1710000000123
  nonce = "nonce_manual_002"
  eventId = "evt_manual_001"
  sign = $sign
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$($env:VMQ_SERVER_URL.TrimEnd('/'))/appPush" -ContentType "application/json" -Body $body
```

Expected: backend returns an idempotent duplicate response for `evt_manual_001` and does not credit again.

- [ ] **Step 4: Verify expired timestamp rejection**

Run:

```powershell
if (-not $env:VMQ_MONITOR_KEY) { throw "Set VMQ_MONITOR_KEY before running this check." }
if (-not $env:VMQ_SERVER_URL) { throw "Set VMQ_SERVER_URL before running this check." }

$monitorKey = $env:VMQ_MONITOR_KEY
$signingText = "2|1023|1700000000000|nonce_manual_003|evt_manual_003"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($monitorKey))
$sign = ($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($signingText)) | ForEach-Object { $_.ToString("x2") }) -join ""

$body = @{
  type = 2
  amountCents = 1023
  ts = 1700000000000
  nonce = "nonce_manual_003"
  eventId = "evt_manual_003"
  sign = $sign
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$($env:VMQ_SERVER_URL.TrimEnd('/'))/appPush" -ContentType "application/json" -Body $body
```

Expected: backend rejects the request because the timestamp is outside the 300-second window.

- [ ] **Step 5: Run the Android unit suite one last time**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Confirm the Android repo is ready for handoff**

```powershell
git status
```

Expected: clean working tree in the Android repo after the implementation tasks above are committed.

## Self-Review

- Spec coverage: this plan now covers `monitorKey`, `nonce`, `eventId`, HTTPS-only transport, UI/config updates, and backend `MonitorReplayGuard` checks.
- Placeholder scan: manual backend verification uses real environment variables instead of placeholder strings.
- Type consistency: the plan consistently uses `amountCents: Long`, `ts/timestamp: Long`, `nonce: String`, `eventId: String`, and `PaymentPushPayload` as the repository boundary type.
