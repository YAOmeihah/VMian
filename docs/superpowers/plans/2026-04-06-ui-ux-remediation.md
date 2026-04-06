# UI/UX Remediation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve the VMian main flow so users see a clear next step, understand permission progress, and get calmer feedback while configuring and testing the app.

**Architecture:** Introduce lightweight UI model mappers for the main screen and permission overview so state-to-copy decisions are testable. Keep the existing screens and dialogs, but reorganize content hierarchy and refine feedback surfaces inside the current Compose structure.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, existing ViewModel/state flow, JUnit unit tests

---

### Task 1: Add testable UI model mappers

**Files:**
- Create: `app/src/main/java/com/java/vmian/presentation/ui/model/MainScreenUiModel.kt`
- Create: `app/src/main/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModel.kt`
- Test: `app/src/test/java/com/java/vmian/presentation/ui/model/MainScreenUiModelTest.kt`
- Test: `app/src/test/java/com/java/vmian/presentation/ui/model/PermissionOverviewUiModelTest.kt`

- [ ] Write failing tests for setup, permission-missing, and ready states.
- [ ] Run targeted unit tests and confirm they fail for missing symbols or mismatched expectations.
- [ ] Implement minimal mappers to satisfy the test expectations.

### Task 2: Rework main screen hierarchy

**Files:**
- Create: `app/src/main/java/com/java/vmian/presentation/ui/components/MainStatusCard.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/MainScreen.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/ConfigInfoCard.kt`

- [ ] Add a status card above configuration details.
- [ ] Replace parallel primary buttons with one stage-based primary CTA plus secondary actions.
- [ ] Move configuration choice into a bottom sheet so setup starts from one clear entry point.
- [ ] Replace transient toast feedback with snackbar feedback on the main screen.

### Task 3: Improve permission and config affordances

**Files:**
- Create: `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionOverviewCard.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/PermissionScreen.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/PermissionItemCard.kt`
- Modify: `app/src/main/java/com/java/vmian/presentation/ui/components/ManualConfigDialog.kt`
- Modify: `app/src/main/java/com/java/vmian/ui/theme/Theme.kt`

- [ ] Add permission progress summary above grouped permission items.
- [ ] Restore visible status labels on permission cards and reduce repeated refresh noise.
- [ ] Start accessibility detection immediately instead of delaying it.
- [ ] Add stronger manual-config input hints and validation.
- [ ] Disable dynamic color by default to keep the app’s intended visual identity.

### Task 4: Verify and note environment blockers

**Files:**
- Modify: none

- [ ] Run targeted unit tests for the new mapper classes.
- [ ] If Gradle is blocked by the locked `app/build/.../R.jar`, document the exact command and failure so the next pass can resume cleanly.
