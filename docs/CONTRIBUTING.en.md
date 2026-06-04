**Language:** [Belarusian](../CONTRIBUTING.md) · **English** · [Russian](CONTRIBUTING.ru.md)

# Contributing to VoltFlow Nav

Thank you! MIT licensed — [github.com/scroodge/VoltFlow-Nav](https://github.com/scroodge/VoltFlow-Nav).

## Workflow

1. Fork → branch `feature/…` or `fix/…`
2. Changes + `./gradlew assembleDebug`
3. Pull request with description and `adb logcat -s VoltFlowNav` if relevant

## Car reports

In issues/PRs include:

- BYD model, DiLink version (`adb shell getprop ro.build.product`)
- Yandex Navigator app version
- What works: HUD / drive panel

## Local-only paths (gitignored)

Do not commit `arrows/`, `openbyd-patch/`, `ui.xml`, or vendor APKs.

## Maintainer install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/VoltFlowNav-*-debug.apk
```

Car setup: [SETUP.en.md](SETUP.en.md) (Shizuku on DiLink 3.0, or one-time [`setup-car.sh`](../setup-car.sh) from a PC).
