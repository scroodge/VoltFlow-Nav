# Changelog

**Languages:** English (canonical, for Gradle/releases) · [Беларуская](docs/CHANGELOG.be.md) · [Русский](docs/CHANGELOG.ru.md)

All notable changes to VoltFlow Nav are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.3.0] - 2026-06-17

### Added

- **Yandex Maps support** alongside Yandex Navigator. The bridge now reads the on-screen turn panel of both `ru.yandex.yandexnavi` and `ru.yandex.yandexmaps` (shared MapKit turn-panel resource ids, resolved per firing package).
- **Cluster navigation widget toggle** on the setup screen. Turns the simulated AMap cluster broadcast on/off so users can disable it if it conflicts with the car's own navigation widget while a trip is in progress.
- **In-app reminder** that Yandex's *Settings → Navigation → "Show turn hints in the corner of the screen"* (first switch) must be ON — without it Yandex draws the hint inside the map and the bridge cannot read turns.

### Changed

- **Turn-icon resolution is now text-first with pixel fallback.** The maneuver image's `contentDescription` is read first (deterministic; populated on some Yandex builds — this is how OpenBYD classifies); pixel classification is used only when it's absent. On-car testing confirmed this car's Yandex build leaves the `contentDescription` null, so the pixel classifier remains the working path here.
- **Turn icon is debounced** (2 consistent reads) so a single misclassified frame can no longer flip the cluster arrow — fixes an observed `STRAIGHT⇄LEFT` flicker on a static turn.
- **Guidance-end is detected by a 4-second expiration window** instead of a single missing frame, preventing false STOPs when the turn panel briefly disappears between screens.
- Debug logs are now written to the public **Downloads/VoltFlowNav/** folder so they can be opened from the car's built-in file manager without ADB.
- Shizuku setup polls the binder up to **12 times** before giving up, so a slow daemon after a cold boot still binds (mirrors the OpenBYD local-ADB retry).

## [1.2.0] - 2026-06-04

### Added

- **Experimental DiLink 5/6 cluster output** (broadcast-first): drives the cluster on DiLink 5/6 using the OpenBYD-style `AUTONAVI_STANDARD_BROADCAST_SEND` format (`TYPE=8`, `IS_BYD_MAP=true`, OpenBYD `NEW_ICON` numbering), alongside the existing DiLink 3 path.
- **Auto / DiLink 3 / DiLink 5** target switch on the setup screen, with auto-detection from system properties (`ro.build.product` / `ro.vehicle.type`). DiLink 3 behavior is unchanged.

### Fixed

- Screen capture now re-establishes on every app open while it isn't running, so after a reboot just opening the app restores it (the previous one-shot auto-request was consumed by the boot auto-launch, leaving only the manual button).

## [1.1.1] - 2026-06-04

### Added

- In-app UI languages: **Belarusian** (default), English, and Russian, with **BY / EN / RU** switcher on the setup screen. Choice is saved and applied on next launch.

### Changed

- Replace non-working Android battery optimization with BYD DiLink **Disable background Apps** screen (`com.byd.appstartmanagement`), matching [BYDMate](https://github.com/AndyShaman/BYDMate): VoltFlow Nav must be **OFF** in the blacklist (OFF = background allowed).
- Setup docs and in-app button **Open Disable background Apps**; removed `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

## [1.1.0] - 2026-06-04

### Added

- **Shizuku setup (recommended on DiLink 3.0):** in-app **Grant via Shizuku** runs `WRITE_SECURE_SETTINGS`, enables accessibility, and allows `PROJECT_MEDIA` (same core steps as `setup-car.sh`).
- **Open Shizuku app** button on the setup screen.
- Setup guides: [docs/SETUP.en.md](docs/SETUP.en.md), [docs/SETUP.ru.md](docs/SETUP.ru.md), [SETUP.md](SETUP.md) with Shizuku download links (GitHub, Play Store, official manual) and Android 10 USB ADB start commands.

### Changed

- Setup screen order: Shizuku first, PC ADB alternative, manual accessibility last (optional).
- README quick install: Shizuku-first for BYD DiLink 3.0; documents that system Accessibility toggle is blocked on tested Yuan UP firmware.

## [1.0.0] - 2026-06-04

### Added

- Initial VoltFlow Nav bridge: Yandex Navigator to BYD DiLink 3.0 HUD via AMap broadcast protocol.
- Setup screen with accessibility, screen capture, and one-time ADB grant flow.
- GitHub Releases auto-update check and in-app APK download/install.
- Release changelog tooling (`./gradlew releaseChangelog`) and version guard before release builds.

[Unreleased]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.1.1...v1.2.0
[1.1.1]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/scroodge/VoltFlow-Nav/releases/tag/v1.0.0
