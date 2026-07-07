# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a native Android app (Kotlin + Jetpack Compose), currently at the freshly-generated
"Empty Activity" template stage — `MainActivity.kt` only contains the default Compose
`Greeting` boilerplate. There is no application architecture, navigation, data layer, or
feature code yet. When implementing features, you are establishing the architecture, not
following an existing one — check with the user on structural decisions (navigation library,
DI, persistence, package layout) rather than assuming a convention.

- Package: `com.example.co_parenting_calendar`
- Application ID / namespace: `com.example.co_parenting_calendar`
- Module layout: single `:app` module (`settings.gradle.kts`)

## Commands

Build and test from the project root using the Gradle wrapper.

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (JVM, app/src/test)
./gradlew testDebugUnitTest

# Run a single unit test class
./gradlew testDebugUnitTest --tests "com.example.co_parenting_calendar.ExampleUnitTest"

# Run instrumented tests (app/src/androidTest) — requires a connected device/emulator
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lint

# Full clean build
./gradlew clean build
```

There is no Espresso/Compose UI test harness wired up beyond the default template test yet.

## Toolchain / config

- Kotlin `2.2.10`, AGP `9.2.1`, compileSdk `36`, minSdk `26`, targetSdk `36`.
- Java source/target compatibility: `11`.
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) — add
  new dependencies there rather than hardcoding versions in `app/build.gradle.kts`.
- UI is built with Jetpack Compose (Material 3) via the Compose BOM
  (`androidx.compose:compose-bom:2026.02.01`); `buildFeatures.compose = true` is set in
  `app/build.gradle.kts`.
- Compose theme scaffolding lives in `app/src/main/java/com/example/co_parenting_calendar/ui/theme/`
  (`Color.kt`, `Theme.kt`, `Type.kt`) — extend these rather than hardcoding colors/typography in
  composables.
