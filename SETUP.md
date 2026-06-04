# VoltFlow Nav — наладка (DiLink 3.0 / Android 10)

**Мова:** **Беларуская** · [English](docs/SETUP.en.md) · [Русский](docs/SETUP.ru.md)

На **BYD DiLink 3.0** уключыць VoltFlow Nav у сістэмных **Спецыяльных магчымасцях** **не выходзіць** (пераключальнік заблакіраваны). Працуе шлях праз **Shizuku** на галавной прыладзе, затым **Grant via Shizuku** у VoltFlow Nav.

Поўная інструкцыя ніжэй. Экран наладкі ў дадатку — у тым жа парадку.

---

## Рэкамендуецца: Shizuku (DiLink 3.0)

### Спампаваць Shizuku

| Крыніца | Спасылка |
|---------|----------|
| APK (на галавную прыладу) | [GitHub Releases — RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku/releases) |
| Google Play (калі даступны) | [Shizuku ў Play Store](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) |
| Афіцыйная інструкцыя | [shizuku.rikka.app/guide/setup](https://shizuku.rikka.app/guide/setup/) |
| Android 10: запуск праз USB ADB | [Падключэнне да камп'ютара](https://shizuku.rikka.app/guide/setup/#start-by-connecting-to-a-computer) |

Пакет: `moe.shizuku.privileged.api`

### Пакрокава (галавная прылада Android 10)

1. Усталюйце APK **VoltFlow Nav** ([Releases](https://github.com/scroodge/VoltFlow-Nav/releases)).
2. Усталюйце **Shizuku** з GitHub або Play (табліца вышэй).
3. На галавной прыладзе: **Рэжым распрацоўшчыка** і **ADB па USB** (для вашай мадэлі BYD/DiLink — форумы).
4. Падключыце аўтамабіль да ПК, `adb devices`, на экране — **Дазволіць адладку** (лепш **Заўсёды**).
5. Калі ставілі з ПК: `adb install -r Shizuku-v*.apk`
6. **Запусціце Shizuku** — каманда з дадатку Shizuku або на ПК:

```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```

Калі шлях не працуе на BYD:

```bash
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

7. Адкрыйце **Shizuku** на галавной — статус **running**. Не адключайце ADB і рэжым распрацоўшчыка.
8. **VoltFlow Nav** → **Grant via Shizuku** → дазвольце доступ Shizuku.
9. Пліткі статусу: **Accessibility: OK** (і пры магчымасці **PROJECT_MEDIA: OK**).
10. **Restart screen capture** і пацвердзіце сістэмны дыялог (зноў пасля кожнай перазагрузкі).
11. Опцыянальна: **Open battery settings** — без абмежаванняў для VoltFlow Nav.

### Пасля перазагрузкі

- **Shizuku** на Android 10 трэба запускаць зноў (паўтарыце крок 6 з ПК або паводле Shizuku ў дадатку).
- Правы VoltFlow (**WRITE_SECURE_SETTINGS**, accessibility) **застаюцца** да выдалення APK.
- **Захоп экрана** — зноў у VoltFlow пасля кожнай перазагрузкі галавной.

### Навігацыя

- **Не** запускайце штатную навігацыю BYD AMap разам з Yandex.
- **Яндэкс Навігатар** павінен быць **на экране** падчас маршруту.

---

## Опцыянальна: Accessibility у Наладах

На DiLink 3.0 пераключальнік **заблакіраваны** (праверана на Yuan UP). Кнопка **Open accessibility settings** у дадатку — для іншых зборак Android.

Калі на вашай прыладзе працуе: уключыце **VoltFlow Nav**, вярніцеся ў дадатак, дазвольце захоп экрана.

---

## Альтэрнатыва: аднаразовы ADB з ПК

Той жа вынік, што Grant via Shizuku, без Shizuku на аўтамабілі:

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.1.0.apk
```

Толькі grant:

```bash
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

Далей на галавной — захоп экрана і батарэя ў VoltFlow Nav.

---

## Дадатак: праверка апавяшчэнняў Yandex

Для магчымага будучага мосту праз апавяшчэнні (мадэль OpenBYD), праверце, ці запаўняе Yandex поля на вашай зборцы DiLink:

```bash
adb logcat -c
# Запусціце маршрут у Yandex, затым:
adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi
```

Непустыя `title` / `text` / `subText` у **ongoing**-апавяшчэнні патрэбныя. Калі пуста — заставайцеся на Accessibility + MediaProjection. Гл. [YANDEX_UI.md](YANDEX_UI.md).
