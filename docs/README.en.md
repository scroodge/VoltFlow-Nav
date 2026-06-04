**Language:** [Belarusian](../README.md) · **English** · [Russian](README.ru.md)

<div align="center">

<img src="assets/brand/voltflow-icon.png" width="96" alt="VoltFlow Nav" />

# VoltFlow Nav

*Yandex Navigator turn-by-turn on your BYD instrument cluster (HUD)*

<p>
  <a href="https://github.com/scroodge/VoltFlow-Nav/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-00E676?style=flat-square" alt="MIT" /></a>
  <a href="https://github.com/scroodge/VoltFlow"><img src="https://img.shields.io/badge/VoltFlow-ecosystem-2962FF?style=flat-square" alt="VoltFlow" /></a>
</p>

<img src="assets/readme/hero-banner.png" width="800" alt="VoltFlow Nav" />

</div>

**VoltFlow Nav** is a free, open-source bridge from **Yandex Navigator** to the **BYD DiLink 3.0** cluster — no root, no OpenBYD. Part of the [VoltFlow](https://github.com/scroodge/VoltFlow) ecosystem.

> **Disclaimer.** Not affiliated with Yandex, BYD, or AMap/Gaode. Install at your own risk. Requires Accessibility and screen capture to read Yandex’s on-screen maneuver panel.

## What works today

<table>
<tr><th>Display</th><th>Status</th><th>v1.0</th></tr>
<tr><td><strong>Instrument cluster (HUD)</strong></td><td>✓ Working</td><td>Maneuver arrows, street (Latin), ETA, route remaining</td></tr>
<tr><td><strong>Drive panel (center)</strong></td><td>~ Experimental</td><td>Partial; not promised in v1.0</td></tr>
<tr><td><strong>Full drive panel</strong></td><td>Roadmap</td><td>Future release</td></tr>
</table>

<p align="center">
  <img src="assets/readme/hud-demo.jpg" width="600" alt="HUD demo" />
</p>

## Compatibility

- **Vehicle:** BYD Yuan UP (tested)
- **DiLink:** 3.0 (`ro.build.product=DiLink3.0`, `ro.vehicle.type=Di3.0_3.5UI`)
- **Android:** 10
- **Navigator:** `ru.yandex.yandexnavi`

## Quick install

1. Download APK from [Releases](https://github.com/scroodge/VoltFlow-Nav/releases) (`VoltFlowNav-*.apk`).
2. Sideload on the head unit (file manager or `adb install -r VoltFlowNav.apk`).
3. **Shizuku setup** on the head unit (recommended for DiLink 3.0) — full guide: [SETUP.en.md](SETUP.en.md):
   - Install [Shizuku](https://github.com/RikkaApps/Shizuku/releases) ([Play](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) · [manual](https://shizuku.rikka.app/guide/setup/)).
   - Start Shizuku once via USB ADB (Android 10), then in VoltFlow Nav tap **Grant via Shizuku**.
   - Manual accessibility in Settings **does not work** on DiLink 3.0 (tested on Yuan UP).
4. On the car: allow **screen capture** (in-app button; required after each reboot).
5. Do **not** run BYD AMap navigation (it blocks third-party broadcasts).
6. Start a Yandex route and keep Yandex **visible** on screen.

Alternative without Shizuku: one-time PC [`setup-car.sh`](../setup-car.sh) or `adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS`.

For later versions: enable **Check for updates on launch** in the app — it offers to download a newer APK from [GitHub Releases](https://github.com/scroodge/VoltFlow-Nav/releases) when available.

## How it works

<img src="assets/readme/flow-diagram.png" width="800" alt="Data flow" />

```
Yandex Navigator
  ├─ AccessibilityService → distance, street, ETA
  └─ MediaProjection → maneuver arrow
        ↓
VoltFlow Nav → AUTONAVI_STANDARD_BROADCAST_SEND → com.example.amapservice → HUD
```

See [CLUSTER_PROTOCOL.en.md](CLUSTER_PROTOCOL.en.md) and [YANDEX_UI.en.md](YANDEX_UI.en.md).

## Documentation

| Topic | Belarusian | English | Russian |
|-------|------------|---------|---------|
| Setup (3 tiers) | [be](../SETUP.md) | [en](SETUP.en.md) | [ru](SETUP.ru.md) |
| Contributing | [be](../CONTRIBUTING.md) | [en](CONTRIBUTING.en.md) | [ru](CONTRIBUTING.ru.md) |
| HUD protocol | [be](../CLUSTER_PROTOCOL.md) | [en](CLUSTER_PROTOCOL.en.md) | [ru](CLUSTER_PROTOCOL.ru.md) |
| Yandex UI | [be](../YANDEX_UI.md) | [en](YANDEX_UI.en.md) | [ru](YANDEX_UI.ru.md) |
| Release | [be](PUBLISH.md) | [en](PUBLISH.en.md) | [ru](PUBLISH.ru.md) |
| Marketing | [be](MARKETING_LAUNCH.md) | [en](MARKETING_LAUNCH.en.md) | [ru](MARKETING_LAUNCH.ru.md) |
| OpenBYD patch | [be](../openbyd-patch/PATCH_NOTES.md) | [en](PATCH_NOTES.en.md) | [ru](PATCH_NOTES.ru.md) |
| Changelog | [be](CHANGELOG.be.md) | [en](../CHANGELOG.md) | [ru](CHANGELOG.ru.md) |

## Limitations

- Yandex must stay **on screen** while navigating.
- Re-tap screen capture after **reboot** (in-app button: «Restart screen capture»).
- v1.0 maneuvers: mostly **left / right / straight**.
- Drive panel is experimental.

## Roadmap

- Full drive-panel mirroring
- Finer maneuvers (slight/sharp, U-turn, roundabouts)

## Part of VoltFlow

| Product | Link |
|---------|------|
| VoltFlow (charging PWA) | [GitHub](https://github.com/scroodge/VoltFlow) · [App](https://volt-flow-beige.vercel.app/) |
| VoltFlow Mate | documented in VoltFlow repo |

## Support the project

Free and **MIT**. Optional donations help BYD hardware testing and the VoltFlow ecosystem:

- [Buy Me a Coffee — scroodge](https://buymeacoffee.com/scroodge)

## Build

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/VoltFlowNav-1.0.0-debug.apk
adb logcat -s VoltFlowNav
```

## Contributing

[CONTRIBUTING.md](../CONTRIBUTING.md) · [CONTRIBUTING.en.md](CONTRIBUTING.en.md)

## License

[MIT](../LICENSE)
