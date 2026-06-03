# Yandex BYD Bridge

A small Android app that bridges **Yandex Navigator** turn-by-turn directions to a
**BYD instrument cluster** (HUD / dashboard) running BYD's DiLink system.

It works by listening to the navigation notification Yandex posts while guiding you,
extracting the next manoeuvre and distance, and re-broadcasting them in the
`AUTONAVI_STANDARD_BROADCAST_SEND` format that [OpenBYD](https://github.com/) (`com.sr.openbyd`)
already understands and forwards to the cluster.

```
Yandex Navigator ──notification──▶ YandexNaviListenerService
                                          │  parse turn + distance
                                          ▼
                                   AmapBroadcastSender
                                          │  AUTONAVI_STANDARD_BROADCAST_SEND
                                          ▼
                                   OpenBYD ──▶ BYD cluster
```

- Package: `com.bridge.yandexbyd`
- Language: Kotlin
- Min SDK: API 26 (Android 8.0) · Target/Compile SDK: 34

## Modules

| File | Role |
|------|------|
| `MainActivity.kt` | UI to check / grant Notification Listener access. |
| `YandexNaviListenerService.kt` | Captures Yandex notifications, parses title/text, resolves the turn kind. |
| `TurnKindMapper.kt` | Maps instructions to `TURN_KIND_*` via keyword matching (RU/EN/TR) with an icon-bitmap fallback. |
| `AmapBroadcastSender.kt` | Emits the AutoNavi broadcast intents OpenBYD intercepts. |
| `BootReceiver.kt` | Hook for boot-completed (service itself is OS-managed). |

## Build

The Android SDK path is set in `local.properties` (not committed). Then:

```sh
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

Or open the folder in Android Studio and **Build → Build APK(s)**.

## Install on the car

1. Enable ADB / developer mode on the BYD DiLink head unit
   (see [github.com/ahmada3mar/BYD](https://github.com/ahmada3mar/BYD)).
2. Join the car's Wi-Fi hotspot, then:
   ```sh
   adb connect <car-ip>:5555
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Open **Yandex BYD Bridge** on the car screen → **Grant Access** → enable it
   in the Notification Listener list.

## Test & tune

Start Yandex Navigator, set a destination, and watch the cluster. Stream logs with:

```sh
adb logcat -s YandexBYDBridge
```

> **Tuning note:** `TurnKindMapper.RULES` may need adjustment after the first test —
> logcat shows the exact title/text Yandex sends in your language; add or tweak the
> keywords accordingly. The bitmap fallback only distinguishes left/right/straight.

## Notes on the implementation vs. the original spec

- The `AndroidManifest.xml` omits the legacy `package="…"` attribute; with Android
  Gradle Plugin 8.x the package is taken from `namespace` in `app/build.gradle`
  (keeping the attribute is now a build error).
- A vector adaptive launcher icon is included (`@mipmap/ic_launcher`) since the
  manifest references it; minSdk 26 means no legacy PNG densities are needed.
