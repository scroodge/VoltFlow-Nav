# Журнал изменений

**Язык:** [Беларуская](CHANGELOG.be.md) · [English](../CHANGELOG.md) · **Русский**

Все значимые изменения VoltFlow Nav. Канонический файл для сборки — [CHANGELOG.md](../CHANGELOG.md) (английский).

Формат основан на [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
версии — [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.0] - 2026-06-04

### Добавлено

- **Экспериментальный вывод на панель DiLink 5/6** (через широковещание): управляет панелью на DiLink 5/6 в формате `AUTONAVI_STANDARD_BROADCAST_SEND` как OpenBYD (`TYPE=8`, `IS_BYD_MAP=true`, нумерация `NEW_ICON` от OpenBYD), наряду с существующим путём DiLink 3.
- Переключатель цели **Auto / DiLink 3 / DiLink 5** на экране настройки с автоопределением по системным свойствам (`ro.build.product` / `ro.vehicle.type`). Поведение DiLink 3 не изменилось.

### Исправлено

- Захват экрана снова включается при каждом открытии приложения, пока он не активен: после перезагрузки достаточно открыть приложение. Раньше автозапрос срабатывал один раз и «сгорал» при автозапуске с boot — оставалась только кнопка.

## [1.1.1] - 2026-06-04

### Добавлено

- Языки интерфейса: **беларуская** (по умолчанию), английский, русский; переключатель **BY / EN / RU** на экране настройки.

### Изменено

- Вместо неработающей оптимизации батареи Android — экран DiLink **Disable background Apps** (как [BYDMate](https://github.com/AndyShaman/BYDMate)): для VoltFlow Nav переключатель **OFF** (OFF = фон разрешён).
- Кнопка **Open Disable background Apps** в приложении; убрано `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

## [1.1.0] - 2026-06-04

### Добавлено

- **Настройка через Shizuku (рекомендуется на DiLink 3.0):** кнопка **Grant via Shizuku** выдаёт `WRITE_SECURE_SETTINGS`, включает accessibility и `PROJECT_MEDIA`.
- Кнопка **Open Shizuku app** на экране настройки.
- Руководства: [SETUP.md](../SETUP.md), [SETUP.en.md](SETUP.en.md), [SETUP.ru.md](SETUP.ru.md) — ссылки на Shizuku и команды запуска через USB ADB на Android 10.

### Изменено

- Порядок на экране настройки: Shizuku → ADB с ПК → accessibility вручную (опционально).
- README: Shizuku-first; на DiLink 3.0 переключатель accessibility в Настройках заблокирован (Yuan UP).

## [1.0.0] - 2026-06-04

### Добавлено

- Первый мост VoltFlow Nav: Яндекс Навигатор → HUD BYD DiLink 3.0 через AMap broadcast.
- Экран настройки: accessibility, захват экрана, одноразовый ADB grant.
- Автопроверка GitHub Releases и загрузка/установка APK в приложении.
- Инструмент changelog (`./gradlew releaseChangelog`) и проверка версии перед release-сборкой.

[Unreleased]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.1.1...v1.2.0
[1.1.1]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/scroodge/VoltFlow-Nav/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/scroodge/VoltFlow-Nav/releases/tag/v1.0.0
