# VoltFlow Nav — setup (DiLink 3.0 / Android 10)

**Language:** English · [Russian](SETUP.ru.md) · [Belarusian](../SETUP.md)

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
3. On the head unit: enable **Developer options** and **USB debugging** (see BYD/DiLink forums for your model).
4. Connect the car to a PC with ADB; run `adb devices` and accept **Allow USB debugging** on the screen (check **Always allow** if offered).
5. Install Shizuku if you used the PC: `adb install -r Shizuku-v*.apk`
6. **Start Shizuku** — copy the command from the Shizuku app, or run on the PC:

```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```

If that path fails on your BYD build, try:

```bash
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

7. Open the **Shizuku** app on the head unit — status should show the service **running**. Keep **USB debugging** and **Developer options** enabled (Shizuku requirement).
8. Open **VoltFlow Nav** → tap **Grant via Shizuku** → allow Shizuku access when prompted.
9. Check status tiles: **Accessibility: OK** (and optionally **PROJECT_MEDIA: OK**).
10. Tap **Restart screen capture** and confirm the system dialog (needed again after each reboot).
11. Optional: **Open battery settings** and disable restrictions for VoltFlow Nav.

### After reboot

- **Shizuku** must be started again on Android 10 (repeat step 6 from a PC, or use Shizuku’s in-app instructions).
- VoltFlow’s **WRITE_SECURE_SETTINGS** grant and accessibility enablement **stay** until you uninstall VoltFlow Nav.
- **Screen capture** must be allowed again in VoltFlow after each head-unit reboot.

### Navigation

- Do **not** run BYD AMap navigation while using Yandex (it blocks third-party HUD updates).
- Keep **Yandex Navigator visible** on screen during the route.

---

## Optional: system Accessibility settings

On DiLink 3.0 the per-app accessibility toggle is **blocked** in Settings (verified on Yuan UP). The in-app **Open accessibility settings** button remains for other Android builds only.

If it works on your device: enable **VoltFlow Nav**, return to the app, then allow screen capture.

---

## Alternative: one-time PC ADB

Same result as Shizuku grant, without installing Shizuku on the car:

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.0.0.apk
```

Grant only:

```bash
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

Then open VoltFlow Nav on the head unit for screen capture and battery.

---

## Appendix: Yandex notification spike

For a possible future notification-based bridge (OpenBYD model), check whether Yandex fills notification fields on your DiLink build:

```bash
adb logcat -c
# Start a Yandex route, then:
adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi
```

Non-empty `title` / `text` / `subText` on an **ongoing** notification would be required. If empty, keep Accessibility + MediaProjection. See [YANDEX_UI.en.md](YANDEX_UI.en.md).
