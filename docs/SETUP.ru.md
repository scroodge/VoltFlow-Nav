# VoltFlow Nav — настройка (DiLink 3.0 / Android 10)

**Язык:** [Беларуская](../SETUP.md) · [English](SETUP.en.md) · **Русский**

На **BYD DiLink 3.0** включить VoltFlow Nav в **Специальных возможностях** системы **не получается** (переключатель заблокирован). Рабочий путь — **Shizuku** на головном устройстве, затем **Grant via Shizuku** в VoltFlow Nav.

Подробная инструкция ниже. Экран настройки в приложении в том же порядке.

---

## Рекомендуется: Shizuku (DiLink 3.0)

### Скачать Shizuku


| Источник                         | Ссылка                                                                                               |
| -------------------------------- | ---------------------------------------------------------------------------------------------------- |
| APK (установка с флешки/файлов)  | [GitHub Releases — RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku/releases)                 |
| Google Play (если доступен)      | [Shizuku в Play Store](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api)     |
| Официальная инструкция           | [shizuku.rikka.app/guide/setup](https://shizuku.rikka.app/guide/setup/)                              |
| Android 10: запуск через USB ADB | [Подключение к компьютеру](https://shizuku.rikka.app/guide/setup/#start-by-connecting-to-a-computer) |


Имя пакета: `moe.shizuku.privileged.api`

### Пошагово (головное устройство Android 10)

1. Установите APK **VoltFlow Nav** ([Releases](https://github.com/scroodge/VoltFlow-Nav/releases)).
2. Установите **Shizuku** с GitHub или Play (см. таблицу).
3. Откройте **Shizuku** на машине — статус **running**. Не отключайте отладку USB и режим разработчика.
4. **VoltFlow Nav** → **Grant via Shizuku** → разрешите доступ Shizuku. Помимо Accessibility и PROJECT_MEDIA это выдаёт SYSTEM_ALERT_WINDOW — без него Android 10 молча блокирует автовосстановление захвата после перезагрузки.
5. Посмотрите что стоят галочки напротив: **Accessibility: OK** (и при возможности **PROJECT_MEDIA: OK**).
6. Нажмите  **Screen capture**.
7. **Open Disable background Apps** в VoltFlow (или **Настройки → General → Disable background Apps**). Для **VoltFlow Nav** переключатель **OFF**.

**OFF = фон разрешён** (чёрный список, как в [BYDMate](https://github.com/AndyShaman/BYDMate)). **ON** = DiLink может убить мост.

### После перезагрузки

- Права VoltFlow (**WRITE_SECURE_SETTINGS**, accessibility) **сохраняются** до удаления приложения.

### Навигация

- ⚠️ **Обязательно для Yandex (v1.3.0+):** в Яндексе откройте **Настройки → Навигация** и включите **«Показывать подсказки поворотов в углу экрана»** (первый переключатель). Без этого Яндекс рисует подсказку внутри карты при приближении к повороту, и мост **не может** прочитать данные навигации.
- Поддерживаются **Яндекс Навигатор** (`ru.yandex.yandexnavi`) и **Яндекс Карты** (`ru.yandex.yandexmaps`).
- Не запускайте штатную навигацию **BYD AMap** вместе с Yandex.
- Яндекс (Навигатор или Карты) должен быть **на экране** во время маршрута.
- На экране настройки **«Вывод на HUD» → «Виджет навигации в приборной панели»** можно выключить, если симулированные уведомления AMap конфликтуют с собственным навигационным виджетом автомобиля.

### Выбор цели DiLink (v1.2.0+)

На экране настройки есть селектор **Auto / DiLink 3 / DiLink 5**:

- **Auto** — рекомендуется (автоопределение по `ro.build.product` / `ro.vehicle.type`).
- **DiLink 3** — стабильный путь для DiLink 3.0.
- **DiLink 5** — экспериментальный вывод для DiLink 5/6 (broadcast-first, формат OpenBYD).

---

## Опционально: Accessibility в Настройках

На DiLink 3.0 переключатель **заблокирован** (проверено на Yuan UP). Кнопка **Open accessibility settings** в приложении — для других прошивок.

---

## Альтернатива: ADB с ПК один раз

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.2.0.apk
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