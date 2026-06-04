# VoltFlow Nav — setup tiers (DiLink 3.0 / Android 10)

**Language:** English · [Russian](SETUP.ru.md) · [Belarusian](../SETUP.md)

Choose the first method that works on your head unit. The in-app setup screen lists the same order.

## Tier 1 — Accessibility in Settings (no PC)

1. Install and open **VoltFlow Nav**.
2. Tap **Open accessibility settings** and enable **VoltFlow Nav**.
3. Return to the app; allow **screen capture** when prompted (again after each reboot).
4. Open **battery** settings from the app if background kill is an issue.

**Verify on car (accessibility spike):**

- Details screen opens: `adb shell am start -a android.settings.ACCESSIBILITY_DETAILS_SETTINGS -e android.intent.extra.COMPONENT_NAME com.bridge.yandexbyd/com.bridge.yandexbyd.YandexA11yService`
- After enabling, status tile shows **Accessibility: OK** and `adb shell settings get secure enabled_accessibility_services` contains `com.bridge.yandexbyd`.

If DiLink blocks the toggle, use Tier 2 or 3.

## Tier 2 — Shizuku on the head unit

1. Install [Shizuku](https://shizuku.rikka.app/) on the head unit.
2. Start Shizuku (on **Android 10**, first start needs **USB ADB** once; see Shizuku’s guide).
3. In VoltFlow Nav, tap **Grant via Shizuku** and allow Shizuku access for VoltFlow Nav.

This grants `WRITE_SECURE_SETTINGS`, enables accessibility, and allows `PROJECT_MEDIA` (same as [`setup-car.sh`](../setup-car.sh) core steps).

## Tier 3 — One-time PC ADB

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.0.0.apk
```

Or only the grant:

```bash
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

Then open VoltFlow Nav on the car for screen capture and battery.

---

## Spike: Yandex notifications (Tier 3 / OpenBYD path)

Before building a notification-based bridge, check whether Yandex fills notification fields on your DiLink build during active navigation:

```bash
adb logcat -c
# Start a Yandex route, then:
adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi
```

Look for non-empty `android.title`, `android.text`, and `android.subText` on an **ongoing** notification. If they stay empty, stay on Accessibility + MediaProjection (current VoltFlow design). See [YANDEX_UI.en.md](YANDEX_UI.en.md) and [PATCH_NOTES.en.md](PATCH_NOTES.en.md) (OpenBYD whitelist).
