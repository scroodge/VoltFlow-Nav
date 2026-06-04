#!/bin/bash
# One-shot ADB setup for VoltFlow Nav on a DiLink 3.0 head unit.
# Run after `adb connect <car-ip>:5555`. Re-run screen capture on the car after reboot.
# On DiLink 3.0, Shizuku + Grant via Shizuku is usually easier — see SETUP.md.
set -euo pipefail

PKG=com.bridge.yandexbyd
A11Y="$PKG/$PKG.YandexA11yService"
APK="${1:-$HOME/Downloads/VoltFlowNav-v1.1.0.apk}"

echo "==> installing $APK"
adb install -r "$APK"

echo "==> letting the app self-enable accessibility after future reinstalls"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS || true

echo "==> granting accessibility (the app also self-heals this on launch now)"
adb shell settings put secure enabled_accessibility_services "$A11Y"
adb shell settings put secure accessibility_enabled 1

echo "==> allowing screen capture without the consent dialog"
adb shell appops set "$PKG" PROJECT_MEDIA allow

echo "==> exempting from background-kill (BYD app manager / doze)"
adb shell dumpsys deviceidle whitelist +"$PKG" >/dev/null
adb shell cmd appops set "$PKG" RUN_ANY_IN_BACKGROUND allow
adb shell cmd appops set "$PKG" RUN_IN_BACKGROUND allow

echo
echo "Done. Now ON THE CAR:"
echo "  1. Open VoltFlow Nav and tap Restart screen capture (needed once per boot)."
echo "  2. Make sure BYD AMap is NOT navigating (it suppresses third parties)."
echo "  3. Start a Yandex route and keep Yandex visible on screen."
echo
echo "Watch it run:  adb logcat -s VoltFlowNav"
