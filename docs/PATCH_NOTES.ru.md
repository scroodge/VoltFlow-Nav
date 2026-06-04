# Патч OpenBYD 2.2 → Yandex

**Язык:** [Беларуская](../openbyd-patch/PATCH_NOTES.md) · [English](PATCH_NOTES.en.md) · **Русский**

## Что делает OpenBYD 2.2 (из декомпиляции)

- Это **не** приёмник `AUTONAVI_STANDARD_BROADCAST_SEND` — он *отправляет* этот broadcast
  (см. `HudController.sendStandardAmapBroadcast`). Исходный план «моста» (Yandex → моё приложение →
  AUTONAVI broadcast → OpenBYD) невозможен: OpenBYD никогда не слушает этот broadcast.
- **Единственный** ввод навигации — `MapNotificationListenerService`, NotificationListenerService
  с whitelist для `com.google.android.apps.maps` и `app.revanced.android.apps.maps`.
- Приборку ведёт привилегированный путь: `ProxyManager` → `ICarControl.turnOnNavi()` /
  `updateNavigation()` + `ClusterProjectionService`. Поэтому патчим OpenBYD, а не переписываем с нуля.

### Парсер (MapNotificationListenerService.onNotificationPosted)
- Читает `android.title`, `android.text`, `android.subText`; требует `flags & FLAG_ONGOING_EVENT (2)`.
- Regex расстояния до манёвра в **title**: `(\d+[\.,]?\d*)\s*(m|м|km|км)` — уже с кириллицей.
- Regex ETA/остатка маршрута в **subText** уже с `ч|мин|км|м` — кириллица поддерживается.
- **Направление манёвра** только из bitmap крупной иконки уведомления, сверка с perceptual-hash
  реестром стрелок Google Maps (`w40.c`), с геометрическим fallback влево/вправо/прямо. Иконки Yandex
  **не** совпадут с реестром → только грубый геометрический fallback.
- Строит `g60(iconId:int, distance:Integer, road:String, routeDist:Integer, routeTime:Integer)`
  → `HudController.updateNavigation(context, g60, transliterate:boolean)`.

## Фаза 1 (применена) — whitelist Yandex

Файл: `smali/com/sr/openbyd/services/MapNotificationListenerService.smali`, `<clinit>`:
- `.locals 2` → `.locals 3`
- добавлено `const-string v2, "ru.yandex.yandexnavi"`
- `filled-new-array {v0, v1}` → `filled-new-array {v0, v1, v2}`

OpenBYD принимает уведомления Yandex и запускает существующий (кириллический) парсер расстояния/ETA
+ геометрический fallback иконки. Ожидание: расстояние/дорога/ETA вероятно ОК; повороты грубые (Л/П/прямо).

## Фаза 1b (применена) — файловый логер (без adb)

Добавлен `smali/com/sr/openbyd/YBLog.smali` (источник: `YBLog.smali`), статический логер — одна строка на уведомление в:

    /sdcard/Android/data/com.sr.openbyd/files/yandex_nav_log.txt

(через `Context.getExternalFilesDir(null)` — без runtime-разрешения). Читается на головном устройстве любым файловым менеджером.

Вызов в `MapNotificationListenerService.onNotificationPosted`, метка `:goto_0` после title/text/subText:

    invoke-static {v1, v6, v9, v8}, Lcom/sr/openbyd/YBLog;->w(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

Каждая строка: `<Date> | T=<title> | X=<text> | S=<subText>`. Логирует все whitelist-навигации
(до дедупа), дубликаты нормальны для сбора формата.

## Фаза 2 (ожидает) — точный маппинг манёвров

Нужен реальный layout уведомления Yandex (см. CAPTURE ниже). Если геометрического fallback недостаточно —
инжект keyword→iconId для пакета Yandex (RU/EN/TR), переопределяя путь иконки. iconId — `TURN_ICON_*` в
`HudController` (1=LEFT, 2=RIGHT, 3/4=slight L, 5/6=slight R, 7=sharp L, 8=sharp R, 9/10=U-turn,
11/12=straight, 15-20=roundabout, 48=destination).

## Структура каталога (lean — decoded/ генерируется, не в git)

    openbyd-patch/
      original/OpenBYD-2.2-orig.apk   входной APK (git-ignored; своя копия)
      patches/YBLog.smali             источник инжектированного логера  [tracked]
      apply_patches.py                правки в decoded/  [tracked]
      build.sh                        decode -> patch -> build -> sign  [tracked]
      PATCH_NOTES.md                                                             [tracked]
      decoded/                        apktool — GENERATED, git-ignored
      dist/OpenBYD-2.2-yandex.apk     выход — git-ignored
      yandexbyd.keystore              ключ подписи — git-ignored

В git только `original/`, `patches/`, скрипты и этот файл. Остальное регенерируется `build.sh`.

## Сборка

    ./build.sh            # decode + patch + sign
    ./build.sh --keep     # без re-decode, быстрая итерация

Инструменты: apktool, Android build-tools (zipalign, apksigner), JDK keytool, python3. Keystore
создаётся при первом запуске (alias `yb`, storepass/keypass `android`).

`apply_patches.py` идемпотентен, каждая правка — ровно один раз; новая версия apktool/OpenBYD
упадёт с anchor mismatch вместо битой APK. Четыре правки: clinit .locals, whitelist string,
массив из 3 элементов, вызов логера + копирование `patches/YBLog.smali`.

Ручная правка: `./build.sh` (заполнить `decoded/`), править smali, затем `./build.sh --keep`.

Переподписание меняет подпись — старый OpenBYD нужно удалить перед установкой этой сборки
(update-in-place между разными ключами невозможен).
