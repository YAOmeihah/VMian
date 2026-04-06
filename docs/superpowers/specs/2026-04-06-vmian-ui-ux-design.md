# VMian UI/UX Redesign Spec

Date: 2026-04-06
Status: Approved for planning
Scope: Presentation-layer redesign only

## Goal

Refactor the VMian Android app UI/UX into a modern control-center experience while preserving all existing feature logic, data flow, and user capabilities.

The redesign must:

- Keep business behavior unchanged
- Keep all current functional entry points available
- Improve visual hierarchy, readability, and task guidance
- Preserve existing configuration, permission, testing, and log workflows

## Product Direction

The approved direction is:

- Visual style: Modern control center
- Information density: Balanced
- Permission experience: Hybrid guidance + full diagnostics
- Structural direction: Layered command center

## Non-Goals

The redesign will not change:

- Configuration parsing or persistence behavior
- QR scan result handling or configuration save logic
- Permission evaluation rules or settings jump logic
- Heartbeat test behavior
- Notification listener test behavior
- Log source, log semantics, or clear-log behavior
- ViewModel business decisions
- UseCase, Repository, Service, or util-layer logic

## Experience Principles

### 1. State First

Users should understand the app's current state immediately after opening the main screen. The first visual layer must answer:

- Is the app ready?
- What is missing?
- What is the next best action?

### 2. Action Clarity

Primary and secondary actions must be grouped by intent. Users should not need to scan the entire screen to find the next operation.

### 3. Controlled Density

The app should feel efficient, not crowded. Important information stays visible, while dense or operational content is visually contained inside structured panels.

### 4. Diagnostics Without Overload

Permission and log-heavy views should still support diagnosis, but use stronger grouping, summaries, and section rhythm so they remain readable.

### 5. Premium Native Feel

The app should feel polished and modern inside Jetpack Compose and Material 3 rather than styled like a web dashboard transplanted into Android.

## Visual System

### Color Direction

The UI will use a refined light-first palette:

- Deep navy as the primary anchor
- Cool blue-gray support tones
- Soft, high-readability light backgrounds
- Clear semantic colors for success, warning, and error states

Design intent:

- More mature and productized than the current general blue-gray treatment
- Higher contrast between state levels and content layers
- Better visual separation between passive information and actionable elements

Dark theme support should remain functional, but the redesign is optimized primarily for strong readability and hierarchy in light mode.

### Surface Hierarchy

Three card classes will define the interface:

- Primary cards: high-priority state and summary panels
- Information cards: structured content like configuration and permission items
- Work cards: dense operational areas such as log panels

Each class should differ in weight through padding, border treatment, background elevation, and text hierarchy rather than through loud decoration.

### Typography and Labeling

Typography should emphasize fast scanning:

- Strong title hierarchy for screen headers and card headlines
- Compact supporting text for contextual explanation
- Stable badge treatment for status labels
- Consistent section labeling across the app

### Motion

Motion should be subtle and purposeful:

- Smooth page and sheet transitions
- Gentle emphasis when state changes
- No decorative or attention-seeking animation

Motion must not degrade usability or performance.

## Main Screen Redesign

## Structure

The main screen will be reorganized into four visual layers.

### 1. Control Header

The top area keeps the title and permission entry point, but becomes a stronger product header with clearer control-center identity.

Responsibilities:

- Establish screen identity
- Expose the permissions/settings entry clearly
- Support a more intentional first impression

### 2. Primary Status Layer

The current status card becomes the hero panel of the screen.

Responsibilities:

- Communicate current lifecycle stage
- Present the single most important recommendation
- Hold the main CTA

The underlying stage logic remains unchanged:

- Setup
- Permissions required
- Ready

Only the visual framing and action emphasis are updated.

### 3. Secondary Operations Layer

Configuration information and quick actions will be grouped into a more intentional operational section.

This layer should separate:

- Current system/configuration snapshot
- Immediate actions the user can take next

Expected actions retained:

- Scan configuration
- Manual configuration
- Edit configuration
- Test listener
- Open permission settings
- Trigger heartbeat test when ready

### 4. Log Workspace

Logs remain on the main screen but become a more contained work area.

Goals:

- Better visual separation from setup/status content
- Clearer tab or mode distinction if already present
- Stronger affordance for clear actions
- Better readability for dense entries

This remains a presentation refactor only. Log data sources and behaviors do not change.

## Main Screen UX Rules

- The first viewport should prioritize status comprehension and next action
- The main CTA must always visually outrank secondary actions
- Secondary actions should feel grouped, not scattered
- Config details should read as a snapshot, not a raw form dump
- Logs should feel operational but not overwhelm the screen

## Permission Screen Redesign

## Structure

The permission screen will use a hybrid model: top guidance plus full diagnostic visibility below.

### 1. Permission Summary Hero

A summary block at the top explains:

- Overall readiness
- What remains incomplete
- Which item should be handled next

This creates a clear onboarding flow without removing the power-user diagnostic view.

### 2. Full Permission Diagnostics

All current groups remain visible:

- Required permissions
- Recommended permissions
- Optional permissions
- Accessibility service card

These groups will be visually clearer through stronger section separation, improved badge treatment, and more consistent card rhythm.

### 3. Action Guidance

Each permission item should communicate:

- Current status
- Why it matters
- What action is available

The experience should reduce uncertainty without changing the underlying permission action logic.

## Permission Screen UX Rules

- The top of the screen should answer "what is blocking readiness?"
- Required permissions must feel more urgent than recommended or optional ones
- Section headers must be easier to scan than the current flat list rhythm
- Accessibility service should remain visible as a distinct operational item
- Diagnostic completeness must be preserved

## Component Strategy

The redesign may introduce or refactor presentation-only components to support clearer boundaries.

Allowed changes:

- Split oversized composables into smaller UI-focused components
- Introduce styling primitives or shared visual wrappers
- Add UI models that reshape existing state for display only
- Normalize dialog, sheet, badge, and action-row patterns

Not allowed:

- Moving business rules into UI
- Rewriting domain decisions under the guise of visual cleanup
- Changing side effects or settings behavior

## Files Expected to Change

Primary expected scope:

- `app/src/main/java/com/java/vmian/ui/theme/*`
- `app/src/main/java/com/java/vmian/presentation/ui/*`
- `app/src/main/java/com/java/vmian/presentation/ui/components/*`
- Presentation-only UI model helpers used to reshape existing state for display

Potential small supporting changes:

- `strings.xml` for clarified visual copy introduced by the redesigned presentation

## Accessibility Requirements

The redesign must preserve or improve accessibility:

- Strong contrast in light mode
- Clear semantic use of color for states, never color alone
- Consistent content descriptions for interactive icons
- Stable focusable/clickable targets
- Motion restraint for usability

## Testing and Verification Expectations

Implementation must verify that:

- Existing user flows remain reachable
- Existing tests related to UI models still pass or are updated without behavior change
- The app builds successfully
- No business-layer files are modified unless absolutely required for presentation-only interfaces

## Risks

### Risk 1: Visual Refactor Accidentally Changes Workflow

Mitigation:

- Preserve current action wiring
- Preserve stage derivation and permission decision logic
- Restrict logic edits to presentation-only mapping where possible

### Risk 2: Main Screen Becomes Too Decorative

Mitigation:

- Keep the main CTA and state summary dominant
- Use restrained surfaces and spacing
- Avoid heavy glass effects or noisy gradients

### Risk 3: Permission Screen Loses Diagnostic Value

Mitigation:

- Keep all existing groups and item actions
- Add summary and hierarchy without collapsing critical detail

## Approved Implementation Direction

Proceed with a layered command-center redesign that:

- Refactors the main screen into header, hero state panel, operations layer, and log workspace
- Refactors the permission screen into summary guidance plus full diagnostic sections
- Unifies theme, spacing, cards, badges, and motion across the app
- Preserves all existing feature logic and operational pathways
