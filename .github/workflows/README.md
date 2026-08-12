# Halfwake — Pocket (Android)

The phone version of Halfwake. A home-screen widget that keeps a diary of
your phone's own real activity — uptime since last boot, screen time,
app-switching rate, time since it was last touched. No keystrokes, no
accessibility service, no network permission at all.

## Important: this has not been compiled

I built this in a sandbox with no Android SDK and no network access, so I
could only syntax-review it by hand — I could not run `gradle build`,
open it in an emulator, or install it on a device. Treat this as a strong
first draft, not verified-working code. The most likely failure points
if something doesn't compile cleanly:

- Gradle/AGP/Kotlin version mismatches (I used AGP 8.5.0 / Kotlin 1.9.24 —
  adjust to whatever your installed Android Studio expects)
- `R` class references in `HalfwakeWidgetProvider.kt` and `MainActivity.kt`
  resolving correctly once Gradle actually generates them
- Minor API-level nuances in `UsageRepository.kt` around `UsageEvents`

If any of these come up when you open it, bring me the exact error and
I'll fix it — that's normal for a first build, not a sign the design is wrong.

## How to actually run it

1. Open the `halfwake-pocket/` folder in Android Studio (it'll prompt to
   sync Gradle — let it).
2. Run on a real device or emulator running Android 9 (API 28) or later.
3. On first launch, tap **Grant usage access** — this opens Android's own
   Settings screen (`Settings → Apps → Special access → Usage access`).
   This is a special permission Android requires you to grant manually;
   no app can request it via a normal permission popup.
4. Once granted, reopen the app. It'll run its first tick immediately.
5. Long-press your home screen → Widgets → Halfwake, and drop the widget
   wherever you want it.

## What it actually reads, and what it deliberately does not

**Reads:**
- Device uptime since last boot (`SystemClock.elapsedRealtime()`)
- Total time any app spent in the foreground today, and per-hour app-switch count (via `UsageStatsManager` — Android's own official aggregate usage API, the same data source Digital Wellbeing is built on)
- Timestamp of the last screen-on / interactive event
- Battery percentage and charging state (`BatteryManager`)
- Thermal throttling status (`PowerManager.currentThermalStatus`)
- Which category (game / social / video / other) the foreground app belongs to, using Android's own declared app category metadata (`ApplicationInfo.category`) — real system data, not a maintained guess-list, with a tiny fallback override for a few high-traffic apps that under-report their category

**Does not read, under any circumstance:**
- Keystrokes or any raw input
- Anything requiring an AccessibilityService
- Anything requiring network access — the app has no `INTERNET` permission
  in its manifest at all, so there is no way for this data to leave the phone

## Files
