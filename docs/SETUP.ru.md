# VoltFlow Nav — настройка (DiLink 3.0 / Android 10)

**Язык:** [Беларуская](../SETUP.md) · [English](SETUP.en.md) · **Русский**

На **BYD DiLink 3.0** включить VoltFlow Nav в **Специальных возможностях** системы **не получается** (переключатель заблокирован). Рабочий путь — **Shizuku** на головном устройстве, затем **Grant via Shizuku** в VoltFlow Nav.

Подробная инструкция ниже. Экран настройки в приложении в том же порядке.

---

## Рекомендуется: Shizuku (DiLink 3.0)

### Скачать Shizuku

| Источник | Ссылка |
|----------|--------|
| APK (установка с флешки/файлов) | [GitHub Releases — RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku/releases) |
| Google Play (если доступен) | [Shizuku в Play Store](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) |
| Официальная инструкция | [shizuku.rikka.app/guide/setup](https://shizuku.rikka.app/guide/setup/) |
| Android 10: запуск через USB ADB | [Подключение к компьютеру](https://shizuku.rikka.app/guide/setup/#start-by-connecting-to-a-computer) |

Имя пакета: `moe.shizuku.privileged.api`

### Пошагово (головное устройство Android 10)

1. Установите APK **VoltFlow Nav** ([Releases](https://github.com/scroodge/VoltFlow-Nav/releases)).
2. Установите **Shizuku** с GitHub или Play (см. таблицу).
3. На головном устройстве: **Для разработчиков** и **Отладка по USB**.
4. Подключите машину к ПК, `adb devices`, на экране — **Разрешить отладку** (лучше **Всегда**).
5. При необходимости: `adb install -r Shizuku-v*.apk`
6. **Запустите Shizuku** — команда из приложения Shizuku или на ПК:

```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```

Если путь не сработал на BYD:

```bash
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

7. Откройте **Shizuku** на машине — статус **running**. Не отключайте отладку USB и режим разработчика.
8. **VoltFlow Nav** → **Grant via Shizuku** → разрешите доступ Shizuku.
9. Плитки: **Accessibility: OK** (и при возможности **PROJECT_MEDIA: OK**).
10. **Restart screen capture** (снова после каждой перезагрузки).
11. **Open Disable background Apps** в VoltFlow (или **Настройки → General → Disable background Apps**). Для **VoltFlow Nav** переключатель **OFF**.  
    **OFF = фон разрешён** (чёрный список, как в [BYDMate](https://github.com/AndyShaman/BYDMate)). **ON** = DiLink может убить мост.

### После перезагрузки

- **Shizuku** на Android 10 нужно запускать снова (шаг 6 с ПК).
- Права VoltFlow (**WRITE_SECURE_SETTINGS**, accessibility) **сохраняются** до удаления приложения.
- **Захват экрана** — снова в VoltFlow после каждой перезагрузки головного устройства.

### Навигация

- Не запускайте штатную навигацию **BYD AMap** вместе с Yandex.
- **Яндекс Навигатор** должен быть **на экране** во время маршрута.

---

## Опционально: Accessibility в Настройках

На DiLink 3.0 переключатель **заблокирован** (проверено на Yuan UP). Кнопка **Open accessibility settings** в приложении — для других прошивок.

---

## Альтернатива: ADB с ПК один раз

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.1.0.apk
```

Только grant:

```bash
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

Далее на машине — захват экрана и **Disable background Apps = OFF**.

---

## Приложение: проверка уведомлений Yandex

Для возможного будущего моста через уведомления (модель OpenBYD) проверьте, заполняет ли Yandex поля на вашей прошивке DiLink:

```bash
adb logcat -c
# Запустите маршрут в Yandex, затем:
adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi
```

Непустые `title` / `text` / `subText` в **ongoing**-уведомлении обязательны. Если пусто — оставайтесь на Accessibility + MediaProjection. См. [YANDEX_UI.ru.md](YANDEX_UI.ru.md).
