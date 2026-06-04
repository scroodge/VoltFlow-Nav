# Журнал змен

**Мова:** **Беларуская** · [English](../CHANGELOG.md) · [Русский](CHANGELOG.ru.md)

Усе значныя змены VoltFlow Nav. Кананічны файл для збіркі — [CHANGELOG.md](../CHANGELOG.md) (англійская).

Фармат заснаваны на [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
версіі — [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2026-06-04

### Дададзена

- **Наладка праз Shizuku (рэкамендуецца на DiLink 3.0):** **Grant via Shizuku** — `WRITE_SECURE_SETTINGS`, accessibility, `PROJECT_MEDIA`.
- Кнопка **Open Shizuku app**.
- Дакументацыя SETUP са спасылкамі на Shizuku і USB ADB (Android 10).

### Зменена

- Экран наладкі: Shizuku → ADB з ПК → accessibility ўручную.
- README: Shizuku-first; Accessibility у Наладах на DiLink 3.0 заблакіравана (Yuan UP).

## [1.0.0] - 2026-06-04

### Дададзена

- Першы мост VoltFlow Nav: Яндэкс Навігатар → HUD BYD DiLink 3.0 праз AMap broadcast.
- Экран наладкі: accessibility, захоп экрана, аднаразовы ADB grant.
- Аўтаправерка GitHub Releases і спампоўка/усталёўка APK у дадатку.
- Інструмент changelog (`./gradlew releaseChangelog`) і праверка версіі перад release-зборкай.

[Unreleased]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/scroodge/VoltFlow-Nav/releases/tag/v1.0.0
