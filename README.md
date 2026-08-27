# Namaz Rings

A tiny, offline-first prayer timetable for Bengaluru. The Android home-screen widget is the main feature: five rings show Fajr, Dhuhr, Asr, Maghrib, and Isha, with the next prayer's ring emptying as its time approaches.

The existing static website remains available at [blrnamaz.umar.one](https://blrnamaz.umar.one). The web page and Android app both read the same twelve JSON files in [`months/`](months/), so timetable corrections only need to be made once.

## Ring behaviour

- A completed prayer has an empty ring.
- A later prayer has a full ring.
- The next prayer drains from 100% after the previous prayer to 0% at its own time.
- After Isha, the widget rolls over to tomorrow and starts the overnight Fajr countdown.
- Only the five daily prayers are included for now. Sahri, sunrise, and Zawal remain in the source data for a later version.

The source timetable's `zawal` value is converted to Dhuhr by adding 10 minutes, matching the original website logic. Asr uses `asr_hanafi`.

## Android app

The app is deliberately small:

- Native Kotlin and Android framework APIs only
- No Compose, AndroidX, ads, analytics, accounts, network calls, or runtime permissions
- One `Activity`, one `AppWidgetProvider`, and Canvas-rendered UI
- Android 8.0+ (`minSdk 26`)
- Package name: `one.umar.namazrings`

The widget refreshes every 30 minutes, which is the minimum cadence Android accepts through `updatePeriodMillis`. The full app refreshes its countdown every 30 seconds while visible. Tapping the widget opens the app; the app's button can request that supported launchers pin the widget directly.

## Build and test

Install Android SDK Platform 36 and Build Tools 34.0.0 or newer, then set `ANDROID_HOME` or create `local.properties` with the SDK path.

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

For a Play-ready signed bundle, follow [`docs/PLAY_INTERNAL_TESTING.md`](docs/PLAY_INTERNAL_TESTING.md).

## Adding another city later

Keep one folder per city with the same twelve month files and schema, then add a small city selector in the app. The countdown engine is independent of Bengaluru; only `ScheduleRepository` currently fixes the asset path, city name, time zone, 10-minute Dhuhr offset, and Hanafi Asr choice.
