# VoltFlow Nav — узроўні наладкі (DiLink 3.0 / Android 10)

**Мова:** **Беларуская** · [English](docs/SETUP.en.md) · [Русский](docs/SETUP.ru.md)

У дадатку той жа парадак: спачатку Налады, потым Shizuku, потым ADB з ПК.

## Узровень 1 — Спецыяльныя магчымасці (без ПК)

1. Усталюйце **VoltFlow Nav**, націсніце **Open accessibility settings**, уключыце службу.
2. Вярніцеся ў дадатак, дазвольце **захоп экрана** (пасля кожнай перазагрузкі зноў).

**Праверка на аўтамабілі:** `adb shell settings get secure enabled_accessibility_services` змяшчае `com.bridge.yandexbyd`. Каманда адкрыцця экрана — у [SETUP.en.md](docs/SETUP.en.md).

## Узровень 2 — Shizuku

Усталяваць Shizuku, запусціць (на Android 10 першы раз — USB ADB), у VoltFlow — **Grant via Shizuku**.

## Узровень 3 — ПК

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.0.0.apk
```

## Spike: апавяшчэнні Yandex

`adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi` падчас маршруту — калі title/text пустыя, заставайцеся на Accessibility.
