# Yandex BYD Bridge

Shows **Yandex Navigator** turn-by-turn guidance on the **instrument cluster** of a
**BYD with DiLink 3.0** (tested on a Yuan UP, China, Android 10, `ro.vehicle.type=Di3.0_3.5UI`).

No OpenBYD, no root. A normal app reads Yandex's guidance and feeds the cluster through
BYD's stock AMap broadcast bridge (`com.example.amapservice`).

```
Yandex Navigator
   │  accessibility  → distance, next street, ETA, route remaining
   │  screen capture → maneuver arrow  → left / right / straight
   ▼
YandexBYDBridge ──AUTONAVI_STANDARD_BROADCAST_SEND──▶ com.example.amapservice ──CAN──▶ cluster
```

Why this design (see the decompile docs for the full story):
- Yandex exposes **nothing** via its notification or MediaSession, so guidance is read from
  the on-screen turn panel with an **AccessibilityService** ([YANDEX_UI.md](YANDEX_UI.md)).
- The turn **direction** is an unlabeled image, so it's captured via **MediaProjection** and
  classified geometrically ([ManeuverClassifier.kt](app/src/main/java/com/bridge/yandexbyd/ManeuverClassifier.kt)).
- The cluster is driven by the stock AMap broadcast contract ([CLUSTER_PROTOCOL.md](CLUSTER_PROTOCOL.md)).
- The cluster font has no Cyrillic, so road names are transliterated to Latin
  ([Translit.kt](app/src/main/java/com/bridge/yandexbyd/Translit.kt)).

## Components

| File | Role |
|------|------|
| `YandexA11yService.kt` | Reads Yandex's turn panel by resource-id; orchestrates each update. |
| `CaptureService.kt` | MediaProjection foreground service; crops the arrow region on demand. |
| `ManeuverClassifier.kt` | Arrow bitmap → NEW_ICON (left/right/straight; calibrated on real crops). |
| `AmapBroadcastSender.kt` | Emits the exact `AUTONAVI_STANDARD_BROADCAST_SEND` the cluster consumes. |
| `Translit.kt` | Cyrillic → Latin so names render on the cluster. |
| `MainActivity.kt` | Status + "Start Screen Capture" button (projection grant). |

## Build

```sh
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk  (copy to ~/Downloads/YandexBYDBridge.apk)
```

## Install & set up on the car

```sh
adb connect <car-ip>:5555
./setup-car.sh                 # installs + grants accessibility, capture, background exemption
```
Then on the car: open the app, tap **Start Screen Capture** once, make sure BYD AMap isn't
navigating, start a Yandex route with Yandex visible on screen.

### Grants explained (all via ADB — the car blocks the Settings screens)
| What | Command | When |
|------|---------|------|
| Accessibility | `settings put secure enabled_accessibility_services …` | **every reinstall** (Android disables it on update) |
| Screen capture | `appops set … PROJECT_MEDIA allow` + tap "Start Screen Capture" | appop once; tap **every reboot** (projection token doesn't persist) |
| Stay alive | `dumpsys deviceidle whitelist +…` + `RUN_ANY_IN_BACKGROUND` | once (survives reboot) |

## Watch / debug

```sh
adb logcat -s YandexBYDBridge
```
- `PANEL …` — what was read from Yandex
- `arrow: n=… topL=… topR=… -> icon=…` — arrow classification (2=left, 3=right, 9=straight)
- `→ cluster: …` — what was sent

## Status & known limitations

Working: distance (counts down), next street (transliterated), ETA, route remaining, and
left / right / straight arrows.

To refine as samples are collected (currently fall through to nearest of left/right/straight):
**slight/sharp turns, U-turn, roundabout exits**. The bridge saves each analyzed arrow to
`/sdcard/Android/data/com.bridge.yandexbyd/files/arrows/` for offline calibration of
`ManeuverClassifier` against the AMap NEW_ICON table in [CLUSTER_PROTOCOL.md](CLUSTER_PROTOCOL.md).

Operational notes:
- Yandex must be **visible** on screen while navigating (accessibility + capture read the UI).
- BYD's own AMap must **not** be navigating (it has priority and suppresses third parties).
- Re-tap **Start Screen Capture** after each head-unit reboot.
