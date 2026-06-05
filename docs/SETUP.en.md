# VoltFlow Nav — setup (DiLink 3.0 / Android 10)

**Language:** [Belarusian](../SETUP.md) · **English** · [Russian](SETUP.ru.md)

On **BYD DiLink 3.0**, enabling VoltFlow Nav in system **Accessibility settings does not work** (toggle blocked). The path that works is **Shizuku** on the head unit, then **Grant via Shizuku** inside VoltFlow Nav.

Full guide below. The in-app setup screen follows the same order.

---

## Recommended: Shizuku (DiLink 3.0)

### Download Shizuku

| Source | Link |
|--------|------|
| APK (sideload on head unit) | [GitHub Releases — RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku/releases) |
| Google Play (if available on your device) | [Shizuku on Play Store](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) |
| Official user manual | [shizuku.rikka.app/guide/setup](https://shizuku.rikka.app/guide/setup/) |
| Android 10: start via USB ADB | [Start by connecting to a computer](https://shizuku.rikka.app/guide/setup/#start-by-connecting-to-a-computer) |

Package name: `moe.shizuku.privileged.api`

### Step-by-step (Android 10 head unit)

1. Install **VoltFlow Nav** APK on the head unit ([Releases](https://github.com/scroodge/VoltFlow-Nav/releases)).
2. Install **Shizuku** APK from GitHub or Play (see table above).
3. Open **Shizuku** on the car — status should be **running**.
4. Open **VoltFlow Nav** → tap **Grant via Shizuku** → allow Shizuku access when prompted.
5. Check that status tiles are checked: **Accessibility: OK** (and, if available, **PROJECT_MEDIA: OK**).
6. Tap **Screen capture**.
7. Tap **Open Disable background Apps** in VoltFlow (or **Settings → General → Disable background Apps**). Set **VoltFlow Nav** to **OFF**.

**OFF = background allowed** (blacklist behavior, same as [BYDMate](https://github.com/AndyShaman/BYDMate)). **ON** = DiLink may kill the bridge.

### After reboot

- VoltFlow permissions (**WRITE_SECURE_SETTINGS**, accessibility) **stay** until you uninstall the app.

### Navigation

- Do **not** run BYD AMap navigation while using Yandex (it blocks third-party HUD updates).
- Keep **Yandex Navigator visible** on screen during the route.

### DiLink target selector (v1.2.0+)

Setup screen includes **Auto / DiLink 3 / DiLink 5**:
- **Auto** — recommended (auto-detection via `ro.build.product` / `ro.vehicle.type`).
- **DiLink 3** — stable path for DiLink 3.0.
- **DiLink 5** — experimental output path for DiLink 5/6 (broadcast-first, OpenBYD format).

---

## Optional: system Accessibility settings

On DiLink 3.0 the per-app accessibility toggle is **blocked** in Settings (verified on Yuan UP). The in-app **Open accessibility settings** button remains for other Android builds only.

If it works on your device: enable **VoltFlow Nav**, return to the app, then allow screen capture.

---

## Alternative: one-time PC ADB

Same result as Shizuku grant, without installing Shizuku on the car:

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.2.0.apk
```

Grant only:

```bash
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

Then open VoltFlow Nav on the head unit for screen capture and **Disable background Apps = OFF**.

---

## Appendix: Yandex notification spike

For a possible future notification-based bridge (OpenBYD model), check whether Yandex fills notification fields on your DiLink build:

```bash
adb logcat -c
# Start a Yandex route, then:
adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi
```

Non-empty `title` / `text` / `subText` on an **ongoing** notification would be required. If empty, keep Accessibility + MediaProjection. See [YANDEX_UI.en.md](YANDEX_UI.en.md).
