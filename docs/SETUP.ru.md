# VoltFlow Nav — уровни настройки (DiLink 3.0 / Android 10)

**Язык:** [Беларуская](../SETUP.md) · [English](SETUP.en.md) · **Русский**

Выберите первый способ, который работает на вашей головной. Экран настройки в приложении следует тому же порядку.

## Уровень 1 — Специальные возможности в Настройках (без ПК)

1. Установите и откройте **VoltFlow Nav**.
2. Нажмите **Open accessibility settings** и включите **VoltFlow Nav**.
3. Вернитесь в приложение; разрешите **захват экрана** (после каждой перезагрузки снова).
4. При необходимости откройте **настройки батареи** из приложения.

**Проверка на машине:**

- Открывается экран службы: `adb shell am start -a android.settings.ACCESSIBILITY_DETAILS_SETTINGS -e android.intent.extra.COMPONENT_NAME com.bridge.yandexbyd/com.bridge.yandexbyd.YandexA11yService`
- После включения плитка **Accessibility: OK**, в `enabled_accessibility_services` есть `com.bridge.yandexbyd`.

Если DiLink блокирует переключатель — уровень 2 или 3.

## Уровень 2 — Shizuku на головном устройстве

1. Установите [Shizuku](https://shizuku.rikka.app/).
2. Запустите Shizuku (на **Android 10** первый запуск — **USB ADB** один раз).
3. В VoltFlow Nav: **Grant via Shizuku**, разрешите доступ Shizuku.

Выдаёт `WRITE_SECURE_SETTINGS`, включает accessibility и `PROJECT_MEDIA` (как [`setup-car.sh`](../setup-car.sh)).

## Уровень 3 — ADB с ПК один раз

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.0.0.apk
```

Или только grant:

```bash
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

Далее на машине — захват экрана и батарея в приложении.

---

## Spike: уведомления Yandex

Проверка, есть ли данные маршрута в notification (как у OpenBYD):

```bash
adb logcat -c
# Запустите маршрут в Yandex, затем:
adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi
```

Нужны непустые `title` / `text` / `subText` у ongoing-уведомления. Если пусто — оставайтесь на Accessibility (текущий VoltFlow). См. [YANDEX_UI.ru.md](YANDEX_UI.ru.md).
