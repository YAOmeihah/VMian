# VMian UI/UX Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the VMian main screen and permission screen into a layered modern control center without changing any feature logic or business behavior.

**Architecture:** Keep all domain, repository, service, and ViewModel logic intact and concentrate the redesign inside the Compose presentation layer. Use a small set of presentation-only theme tokens, hero cards, grouped action surfaces, and summary models so the new hierarchy is testable and reusable while preserving current wiring.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, AndroidX Navigation Compose, existing `MainViewModel`/`MainUiState`, JUnit unit tests, Gradle

---

## File Structure

### Existing files to modify

- `app/src/main/java/com/java/vmian/ui/theme/Color.kt`
- `app/src/main/java/com/java/vmian/ui/theme/Theme.kt`
- `app/src/main/java/com/java/vmian/ui/theme/Type.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/MainScreen.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/PermissionScreen.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/AppCardDefaults.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/MainStatusCard.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/ConfigInfoCard.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/UnifiedLogDisplayCard.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionOverviewCard.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionItemCard.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/ManualConfigDialog.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/model/MainScreenUiModel.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModel.kt`
- `app/src/test/java/com/java/vmian/presentation/ui/model/MainScreenUiModelTest.kt`
- `app/src/test/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModelTest.kt`
- `app/src/main/res/values/strings.xml`

### Optional presentation-only files to create if the modified files become too large

- `app/src/main/java/com/java/vmian/presentation/ui/components/MainControlHeader.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/QuickActionsCard.kt`
- `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionSummaryHero.kt`

The implementation should prefer extracting one of the optional files over letting `MainScreen.kt` or `PermissionScreen.kt` grow further.

### Responsibilities

- Theme files own palette, elevation, shape, and typography decisions
- UI model files own text, badge, and display-only state mapping
- `MainScreen.kt` owns page composition and existing action wiring
- `PermissionScreen.kt` owns page composition and existing permission action wiring
- Component files own visual sections only and must not introduce business decisions

### Safety constraints

- Do not edit any file under `domain/`, `data/`, `service/`, `receiver/`, `util/`, or `viewmodel/` unless blocked by a presentation-only type interface mismatch
- If a mismatch appears, stop and re-check whether it can be solved in the presentation layer first
- Do not rename existing business methods or change event order

### Task 1: Lock presentation-only state mapping and visual language

**Files:**
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/model/MainScreenUiModel.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModel.kt`
- Modify: `app/src/test/java/com/java/vmian/presentation/ui/model/MainScreenUiModelTest.kt`
- Modify: `app/src/test/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModelTest.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Expand the failing UI model tests for the new control-center copy and summary expectations**

```kotlin
@Test
fun from_returnsSetupState_whenConfigMissing() {
    val model = MainScreenUiModel.from(
        uiState = MainUiState(config = null, isConfigured = false),
        missingPermissionCount = 0
    )

    assertEquals(MainScreenStage.Setup, model.stage)
    assertEquals("先完成监控配置", model.headline)
    assertEquals("还没有完成监控配置，先扫码或手动填写服务器信息。", model.supportingText)
    assertEquals("立即配置", model.primaryActionLabel)
}

@Test
fun from_prioritizesRequiredPermissions_whenIncomplete() {
    val model = PermissionOverviewUiModel.from(status)

    assertEquals("已完成 1/3 项", model.progressText)
    assertEquals("优先完成必需权限", model.headline)
    assertEquals("还差 1 项必需权限会直接影响收款监听。", model.supportingText)
}
```

- [ ] **Step 2: Run the targeted tests to verify the baseline before changing copy or fields**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.MainScreenUiModelTest" --tests "com.java.vmian.presentation.ui.model.PermissionOverviewUiModelTest"`

Expected: PASS if the current mapper contract already matches, or FAIL with assertion differences that identify the exact copy/field mismatch to update.

- [ ] **Step 3: Implement the minimal mapper or string updates required by the approved design**

```kotlin
data class MainScreenUiModel(
    val stage: MainScreenStage,
    val headline: String,
    val supportingText: String,
    val primaryActionLabel: String
)

data class PermissionOverviewUiModel(
    val progressText: String,
    val headline: String,
    val supportingText: String
)
```

```xml
<string name="main_title">VMian 控制台</string>
<string name="permission_title">权限与诊断</string>
<string name="logs_title">运行工作区</string>
```

- [ ] **Step 4: Re-run the targeted tests and confirm the display-only mapping still passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.MainScreenUiModelTest" --tests "com.java.vmian.presentation.ui.model.PermissionOverviewUiModelTest"`

Expected: PASS with no changes outside the presentation layer.

- [ ] **Step 5: Commit the mapper-and-copy baseline**

```bash
git add app/src/main/java/com/java/vmian/presentation/ui/model/MainScreenUiModel.kt app/src/main/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModel.kt app/src/test/java/com/java/vmian/presentation/ui/model/MainScreenUiModelTest.kt app/src/test/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModelTest.kt app/src/main/res/values/strings.xml
git commit -m "refactor: lock vmian ui state copy"
```

### Task 2: Build the shared control-center theme and card system

**Files:**
- Modify: `app/src/main/java/com/java/vmian/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/java/vmian/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/java/vmian/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/AppCardDefaults.kt`
- Test: `app/src/test/java/com/java/vmian/presentation/ui/model/MainScreenUiModelTest.kt`

- [ ] **Step 1: Add a failing visual-contract assertion by extending an existing mapper test to reflect new hierarchy language**

```kotlin
@Test
fun from_returnsReadyState_whenConfiguredAndPermissionsReady() {
    val model = MainScreenUiModel.from(readyUiState, missingPermissionCount = 0)

    assertEquals(MainScreenStage.Ready, model.stage)
    assertEquals("监控端已准备就绪", model.headline)
    assertEquals("检测心跳", model.primaryActionLabel)
}
```

- [ ] **Step 2: Run the ready-state test before touching theme files**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.MainScreenUiModelTest.from_returnsReadyState_whenConfiguredAndPermissionsReady"`

Expected: PASS. This protects the main CTA wording while the visual system changes underneath it.

- [ ] **Step 3: Implement the approved palette, type rhythm, and card elevation system**

```kotlin
val PrimaryNavy = Color(0xFF12304A)
val PrimaryNavyContainer = Color(0xFFDCEBFF)
val SurfaceSoft = Color(0xFFF4F7FB)
val SurfaceRaised = Color(0xFFFFFFFF)
val AccentBlue = Color(0xFF0E7490)
```

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    primaryContainer = PrimaryNavyContainer,
    background = SurfaceSoft,
    surface = SurfaceRaised,
    surfaceContainerHigh = Color(0xFFE9EEF5),
    surfaceContainerHighest = Color(0xFFDDE5EE)
)
```

```kotlin
object AppCardDefaults {
    @Composable
    fun heroColors() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    fun infoColors() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
}
```

- [ ] **Step 4: Re-run the ready-state mapper test to verify the theme pass did not alter display logic**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.MainScreenUiModelTest.from_returnsReadyState_whenConfiguredAndPermissionsReady"`

Expected: PASS.

- [ ] **Step 5: Commit the shared design system**

```bash
git add app/src/main/java/com/java/vmian/ui/theme/Color.kt app/src/main/java/com/java/vmian/ui/theme/Theme.kt app/src/main/java/com/java/vmian/ui/theme/Type.kt app/src/main/java/com/java/vmian/presentation/ui/components/AppCardDefaults.kt
git commit -m "refactor: add vmian control center theme"
```

### Task 3: Recompose the main screen into header, hero, operations, and workspace

**Files:**
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/MainScreen.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/MainStatusCard.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/ConfigInfoCard.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/UnifiedLogDisplayCard.kt`
- Create: `app/src/main/java/com/java/vmian/presentation/ui/components/MainControlHeader.kt`
- Create: `app/src/main/java/com/java/vmian/presentation/ui/components/QuickActionsCard.kt`

- [ ] **Step 1: Add a focused mapper test that protects stage-driven CTA routing before rearranging the page**

```kotlin
@Test
fun from_returnsPermissionState_whenPermissionsMissing() {
    val model = MainScreenUiModel.from(configuredUiState, missingPermissionCount = 2)

    assertEquals(MainScreenStage.PermissionsRequired, model.stage)
    assertEquals("去完成权限配置", model.primaryActionLabel)
}
```

- [ ] **Step 2: Run the permission-state test to lock the action contract**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.MainScreenUiModelTest.from_returnsPermissionState_whenPermissionsMissing"`

Expected: PASS.

- [ ] **Step 3: Implement the layered page composition while preserving all existing callbacks**

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
) {
    item { MainControlHeader(onNavigateToPermissions = onNavigateToPermissions) }
    item { MainStatusCard(model = screenModel, onPrimaryAction = primaryAction) }
    item { QuickActionsCard(onTestListener = { PermissionUtils.sendTestNotification(context) }, onOpenPermissions = onNavigateToPermissions, onEditConfig = { showConfigMethodSheet = true }) }
    item { ConfigInfoCard(config = uiState.config) }
    item { UnifiedLogDisplayCard(...) }
}
```

```kotlin
@Composable
fun MainStatusCard(
    model: MainScreenUiModel,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(colors = AppCardDefaults.heroColors(), modifier = modifier.fillMaxWidth()) {
        // headline, supporting text, status badge, primary CTA
    }
}
```

```kotlin
@Composable
fun QuickActionsCard(
    onTestListener: () -> Unit,
    onOpenPermissions: () -> Unit,
    onEditConfig: () -> Unit
) {
    // keep existing three entry points, regrouped visually as secondary actions
}
```

- [ ] **Step 4: Run the two mapper test classes after the main-screen refactor**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.MainScreenUiModelTest" --tests "com.java.vmian.presentation.ui.model.PermissionOverviewUiModelTest"`

Expected: PASS. These tests confirm the refactor stayed presentation-only.

- [ ] **Step 5: Build the debug app to catch Compose or resource regressions**

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL. If the environment fails due to an external file lock, record the exact locked file path and stop before touching business logic.

- [ ] **Step 6: Commit the main-screen redesign**

```bash
git add app/src/main/java/com/java/vmian/presentation/ui/MainScreen.kt app/src/main/java/com/java/vmian/presentation/ui/components/MainStatusCard.kt app/src/main/java/com/java/vmian/presentation/ui/components/ConfigInfoCard.kt app/src/main/java/com/java/vmian/presentation/ui/components/UnifiedLogDisplayCard.kt app/src/main/java/com/java/vmian/presentation/ui/components/MainControlHeader.kt app/src/main/java/com/java/vmian/presentation/ui/components/QuickActionsCard.kt
git commit -m "refactor: redesign main control center"
```

### Task 4: Recompose the permission screen into summary guidance plus full diagnostics

**Files:**
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/PermissionScreen.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionOverviewCard.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionItemCard.kt`
- Create: `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionSummaryHero.kt`

- [ ] **Step 1: Extend the permission overview test to protect the top-summary wording**

```kotlin
@Test
fun from_returnsHealthySummary_whenAllPermissionsGranted() {
    val model = PermissionOverviewUiModel.from(status)

    assertEquals("权限状态良好", model.headline)
    assertEquals("当前权限已经满足运行要求，后续只需要按需复查。", model.supportingText)
}
```

- [ ] **Step 2: Run the permission overview tests before changing the page layout**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.PermissionOverviewUiModelTest"`

Expected: PASS.

- [ ] **Step 3: Implement the summary hero and clearer section rhythm without changing any action handlers**

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    item { PermissionSummaryHero(status = status, model = PermissionOverviewUiModel.from(status)) }
    item { PermissionSectionHeader(title = requiredTitle, importance = PermissionImportance.REQUIRED) }
    items(requiredPermissions) { permission -> PermissionItemCard(permission = permission, onSettingsClick = { /* existing handler */ }) }
    item { AccessibilityPermissionCard(...) }
}
```

```kotlin
@Composable
fun PermissionOverviewCard(
    status: PermissionStatus,
    modifier: Modifier = Modifier
) {
    ElevatedCard(colors = AppCardDefaults.heroColors(), modifier = modifier.fillMaxWidth()) {
        // progress label, headline, support copy, progress bar
    }
}
```

- [ ] **Step 4: Keep required, recommended, optional, and accessibility groups intact while clarifying badges and supporting copy**

```kotlin
Text(
    text = importance.displayName,
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

```kotlin
StatusBadge(
    text = statusText,
    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor = MaterialTheme.colorScheme.primary
)
```

- [ ] **Step 5: Run the permission overview test class again**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.PermissionOverviewUiModelTest"`

Expected: PASS.

- [ ] **Step 6: Commit the permission-screen redesign**

```bash
git add app/src/main/java/com/java/vmian/presentation/ui/PermissionScreen.kt app/src/main/java/com/java/vmian/presentation/ui/components/PermissionOverviewCard.kt app/src/main/java/com/java/vmian/presentation/ui/components/PermissionItemCard.kt app/src/main/java/com/java/vmian/presentation/ui/components/PermissionSummaryHero.kt
git commit -m "refactor: redesign permission diagnostics"
```

### Task 5: Polish dialogs, validation surfaces, and final verification

**Files:**
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/ManualConfigDialog.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/MainScreen.kt`

- [ ] **Step 1: Add a focused validation test only if a presentation-only helper is extracted; otherwise protect current behavior through build verification**

```kotlin
// Only add this test if a pure helper such as sanitizeHostInput() is extracted.
assertEquals("https://vmq.example", sanitizeHostInput("https://vmq.example"))
```

- [ ] **Step 2: Implement stronger manual-config affordances without changing save behavior**

```kotlin
OutlinedTextField(
    value = host,
    onValueChange = { host = it },
    label = { Text(stringResource(R.string.monitor_host)) },
    supportingText = { Text(stringResource(R.string.manual_config_host_hint)) },
    isError = host.isBlank()
)
```

```kotlin
OutlinedTextField(
    value = monitorKey,
    onValueChange = { monitorKey = it },
    label = { Text(stringResource(R.string.monitor_key)) },
    supportingText = { Text(stringResource(R.string.manual_config_key_hint)) },
    isError = monitorKey.isBlank()
)
```

- [ ] **Step 3: Run the full presentation-layer unit tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.java.vmian.presentation.ui.model.*"`

Expected: PASS.

- [ ] **Step 4: Build the debug app for end-to-end UI verification**

Run: `.\gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL. If blocked, capture the full error text and identify whether it is a local environment lock rather than an implementation regression.

- [ ] **Step 5: Manually verify the preserved user flows in the emulator or device**

Run:

```bash
1. Open main screen and confirm title, hero state card, quick actions, config card, and log workspace render.
2. Tap the stage-based main CTA in setup, permissions-required, and ready states and confirm the existing destination/behavior is unchanged.
3. Open the config method sheet, scan path, and manual config path.
4. Open permission screen and confirm summary hero plus required/recommended/optional/accessibility sections all remain reachable.
5. Trigger test listener, clear logs, clear push logs, and heartbeat test and confirm existing behavior remains intact.
```

Expected: All functional paths remain unchanged; only layout, copy emphasis, and visual hierarchy differ.

- [ ] **Step 6: Commit the final polish and verification notes**

```bash
git add app/src/main/java/com/java/vmian/presentation/ui/components/ManualConfigDialog.kt app/src/main/java/com/java/vmian/presentation/ui/MainScreen.kt app/src/main/res/values/strings.xml
git commit -m "refactor: polish vmian ui surfaces"
```

## Self-Review

### Spec coverage

- Main screen layered structure is covered by Task 3
- Permission hybrid summary-plus-diagnostics structure is covered by Task 4
- Theme, spacing, cards, badges, and motion foundation are covered by Task 2
- Presentation-only copy and summary logic are covered by Task 1
- Dialog polish and final verification are covered by Task 5

### Placeholder scan

- No `TODO`, `TBD`, or deferred implementation markers remain
- Each task lists exact file paths and concrete commands
- Code-changing steps include code snippets

### Type consistency

- `MainScreenUiModel` and `PermissionOverviewUiModel` remain the display-only model anchors throughout the plan
- `MainStatusCard`, `QuickActionsCard`, and `PermissionSummaryHero` are presentation-only components and do not conflict with existing business-layer APIs

