# Changelog

**Languages:** English (canonical, for Gradle/releases) · [Беларуская](docs/CHANGELOG.be.md) · [Русский](docs/CHANGELOG.ru.md)

All notable changes to VoltFlow Nav are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- GitHub Releases auto-update check and in-app APK download/install.
- Release changelog tooling (`./gradlew releaseChangelog`) and version guard before release builds.

## [1.0.0] - 2026-06-04

### Added

- Initial VoltFlow Nav bridge: Yandex Navigator to BYD DiLink 3.0 HUD via AMap broadcast protocol.
- Setup screen with accessibility, screen capture, and one-time ADB grant flow.

[Unreleased]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/scroodge/VoltFlow-Nav/releases/tag/v1.0.0
