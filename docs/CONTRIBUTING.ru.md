**Язык:** [Беларуская](../CONTRIBUTING.md) · [English](CONTRIBUTING.en.md) · **Русский**

# Участие в VoltFlow Nav

Спасибо! MIT — [github.com/scroodge/VoltFlow-Nav](https://github.com/scroodge/VoltFlow-Nav).

## Процесс

1. Fork → ветка `feature/…` или `fix/…`
2. Изменения + `./gradlew assembleDebug`
3. Pull request с описанием и `adb logcat -s VoltFlowNav` при необходимости

## Отчёты по авто

В issue/PR укажите:

- Модель BYD, версия DiLink (`adb shell getprop ro.build.product`)
- Версия Яндекс Навигатора
- Что работает: HUD / центральный экран

## Не коммитить

`arrows/`, `openbyd-patch/`, `ui.xml`, vendor APK (см. `.gitignore`).

## Установка

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/VoltFlowNav-*-debug.apk
```

Настройка на машине — [SETUP.ru.md](SETUP.ru.md) (Shizuku на DiLink 3.0 или `./setup-car.sh` с ПК).
