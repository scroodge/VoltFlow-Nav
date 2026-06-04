**Мова:** Беларуская · [English](docs/CONTRIBUTING.en.md) · [Русский](docs/CONTRIBUTING.ru.md)

# Удзел у VoltFlow Nav

Дзякуй за цікавасць! Праект MIT, рэпазіторый: [github.com/scroodge/VoltFlow-Nav](https://github.com/scroodge/VoltFlow-Nav).

## Як дапамагчы

1. Fork → галіна `feature/…` або `fix/…`
2. Змены + `./gradlew assembleDebug`
3. Pull request з апісаннем і, калі магчыма, `adb logcat -s VoltFlowNav`

## Звесткі пра аўтамабіль

У issue/PR укажыце:

- Мадэль BYD, версія DiLink (`adb shell getprop ro.build.product`)
- Версія Yandex Navigator
- Што працуе: HUD / цэнтральная панэль

## Лакальныя файлы (не ў git)

`arrows/`, `openbyd-patch/`, `ui.xml`, vendor APK — у `.gitignore`, не дадавайце ў PR.

## Зборка для распрацоўшчыкаў

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/VoltFlowNav-*-debug.apk
```

Наладка на аўтамабілі — [SETUP.md](SETUP.md) (Shizuku на DiLink 3.0 або `./setup-car.sh` з ПК).
