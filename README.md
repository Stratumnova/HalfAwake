# Halfwake — Pocket (Android)

The phone version of Halfwake. A home-screen widget that keeps a diary of your phone's own real activity — uptime since last boot, screen time, app-switching rate, time since it was last touched. No keystrokes, no accessibility service, no network permission at all.

## Important: this has not been compiled

Built without an Android SDK or network access, so it was only syntax-reviewed by hand — never run through `gradle build`, an emulator, or a device. Treat it as a strong first draft. Most likely first-build issues:

- Gradle/AGP/Kotlin version mismatches (AGP 8.5.0 / Kotlin 1.9.24)
- R class references in HalfwakeWidgetProvider.kt and MainActivity.kt resolving once Gradle generates them
- Minor API-level nuances in UsageRepository.kt around UsageEvents

## How to run it

1. Open the project in Android Studio, let Gradle sync.
2. Run on a device or emulator, Android 10 (API 29) or later.
3. On first launch, tap Grant usage access — opens Settings → Apps → Special access → Usage access. Must be granted manually.
4. Reopen the app. First tick runs immediately.
5. Long-press home screen → Widgets → Halfwake.

## What it reads, and what it never reads

Reads: device uptime since boot, total foreground time and app-switch count today (via UsageStatsManager), time since last screen interaction, battery percentage and charging state, thermal throttling status, and which category (game / social / video / other) the foreground app belongs to, via Android's own declared app category metadata.

Never reads: keystrokes or raw input, anything requiring an AccessibilityService, or anything over the network — there is no INTERNET permission in the manifest at all.

## Files

- MoodEngine.kt — the 11-rule priority table
- UsageRepository.kt — all real-metric reads
- AppCategoryRepository.kt — classifies the foreground app
- NotificationHelper.kt — fires the critical-battery notice
- BatteryLowReceiver.kt — catches the instant system battery-low broadcast
- DiaryLines.kt — hand-written line pools, one per mood
- DiaryStore.kt — flat JSON storage, capped at 500 entries
- TickWorker.kt — WorkManager job, ticks every 3h
- MainActivity.kt — permission flow and full diary log
- widget/HalfwakeWidgetProvider.kt — the home-screen widget

## Mood rules (v0.2 placeholder thresholds)

Checked top to bottom, first match wins: Critical (battery under 10%, not charging), Burning (thermal severe+), Groggy (uptime under 1hr), Fed (charging), Happy (15+ min gaming this hour), Sad (30+ min social/video this hour), Tired (6+ hrs screen time today), Busy (15+ app switches this hour), Quiet (late night, low usage), Restless (6+ hrs untouched, daytime), Content (default).

## Open items

- Real thresholds need tuning against a week or two of actual usage
- No custom launcher icon artwork yet
