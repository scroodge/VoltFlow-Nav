# Журнал змен

**Мова:** **Беларуская** · [English](../CHANGELOG.md) · [Русский](CHANGELOG.ru.md)

Усе значныя змены VoltFlow Nav. Кананічны файл для збіркі — [CHANGELOG.md](../CHANGELOG.md) (англійская).

Фармат заснаваны на [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
версіі — [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.0] - 2026-06-04

### Дададзена

- **Эксперыментальны вывад на панэль DiLink 5/6** (праз вяшчанне): кіруе панэллю на DiLink 5/6 у фармаце `AUTONAVI_STANDARD_BROADCAST_SEND` як OpenBYD (`TYPE=8`, `IS_BYD_MAP=true`, нумарацыя `NEW_ICON` ад OpenBYD), побач з існуючым шляхам DiLink 3.
- Пераключальнік мэты **Auto / DiLink 3 / DiLink 5** на экране наладкі з аўтавызначэннем па сістэмных уласцівасцях (`ro.build.product` / `ro.vehicle.type`). Паводзіны DiLink 3 не змяніліся.

### Выпраўлена

- Захоп экрана зноў уключаецца пры кожным адкрыцці праграмы, пакуль ён не актыўны: пасля перазагрузкі дастаткова адкрыць праграму. Раней аўтазапыт спрацоўваў адзін раз і «згараў» пры аўтазапуску з boot — заставалася толькі кнопка.

## [1.1.1] - 2026-06-04

### Дададзена

- Мовы інтэрфейсу: **беларуская** (па змаўчанні), англійская, руская; пераключальнік **BY / EN / RU** на экране наладкі.

### Зменена

- Замест непрацуючай оптымізацыі батарэі Android — экран DiLink **Disable background Apps** (як [BYDMate](https://github.com/AndyShaman/BYDMate)): VoltFlow Nav = **OFF**.
- Кнопка **Open Disable background Apps**; выдалена `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

## [1.1.0] - 2026-06-04

### Дададзена

- **Наладка праз Shizuku (рэкамендуецца на DiLink 3.0):** **Grant via Shizuku** — `WRITE_SECURE_SETTINGS`, accessibility, `PROJECT_MEDIA`.
- Кнопка **Open Shizuku app**.
- Дакументацыя: [SETUP.md](../SETUP.md), [SETUP.en.md](SETUP.en.md), [SETUP.ru.md](SETUP.ru.md) — спасылкі на Shizuku (GitHub, Play, інструкцыя) і USB ADB на Android 10.

### Зменена

- Экран наладкі: Shizuku → ADB з ПК → accessibility ўручную.
- README: Shizuku-first; Accessibility у Наладах на DiLink 3.0 заблакіравана (Yuan UP).

## [1.0.0] - 2026-06-04

### Дададзена

- Першы мост VoltFlow Nav: Яндэкс Навігатар → HUD BYD DiLink 3.0 праз AMap broadcast.
- Экран наладкі: accessibility, захоп экрана, аднаразовы ADB grant.
- Аўтаправерка GitHub Releases і спампоўка/усталёўка APK у дадатку.
- Інструмент changelog (`./gradlew releaseChangelog`) і праверка версіі перад release-зборкай.

[Unreleased]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.1.1...v1.2.0
[1.1.1]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/scroodge/VoltFlow-Nav/releases/tag/v1.0.0
