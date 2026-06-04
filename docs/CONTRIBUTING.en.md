**Language:** [Belarusian](../CONTRIBUTING.md) · **English** · [Russian](CONTRIBUTING.ru.md)

# Contributing to VoltFlow Nav

Thank you! MIT licensed — [github.com/scroodge/VoltFlow-Nav](https://github.com/scroodge/VoltFlow-Nav).

## Workflow

1. Fork → branch `feature/…` or `fix/…`
2. Changes + `./gradlew assembleDebug`
3. Pull request with description and `adb logcat -s VoltFlowNav` if relevant

## Car reports

Include model, DiLink version (`adb shell getprop ro.build.product`), Yandex app version, and whether HUD / drive panel work.

## Local-only paths (gitignored)

Do not commit `arrows/`, `openbyd-patch/`, `ui.xml`, or vendor APKs.

## Maintainer install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/VoltFlowNav-*-debug.apk
```

Car setup is in-app only (see README for one-time ADB grant).
