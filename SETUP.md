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
3. Адкрыйце **Shizuku** на аўтамабілі — статус **running**.
4. **VoltFlow Nav** → **Grant via Shizuku** → дазвольце доступ Shizuku. Акрамя Accessibility і PROJECT_MEDIA гэта дае SYSTEM_ALERT_WINDOW — без яго Android 10 моўчкі блакуе аўтааднаўленне захопу пасля перазагрузкі.
5. Праверце пліткі статусу: **Accessibility: OK** (і, калі магчыма, **PROJECT_MEDIA: OK**).
6. Націсніце **Screen capture**.
7. **Open Disable background Apps** у VoltFlow (ці **Налады → General → Disable background Apps**). Для **VoltFlow Nav** пераключальнік **OFF**.

**OFF = фон дазволены** (чорны спіс, як у [BYDMate](https://github.com/AndyShaman/BYDMate)). **ON** = DiLink можа забіць мост.

### Пасля перазагрузкі

- Правы VoltFlow (**WRITE_SECURE_SETTINGS**, accessibility) **застаюцца** да выдалення праграмы.

### Навігацыя

- ⚠️ **Абавязкова для Yandex (v1.3.0+):** у Яндексе адкрыйце **Налады → Навігацыя** і ўключыце **«Паказваць падказкі паваротаў у куце экрана»** (першы пераключальнік). Без гэтага Яндекс малюе падказку ўнутры карты пры набліжэнні да павароту, і мост **не можа** прачытаць дадзеныя навігацыі.
- Падтрымліваюцца **Яндекс Навігатар** (`ru.yandex.yandexnavi`) і **Яндекс Карты** (`ru.yandex.yandexmaps`).
- **Не** запускайце штатную навігацыю BYD AMap разам з Yandex.
- Яндекс (Навігатар ці Карты) павінен быць **на экране** падчас маршруту.
- На экране наладкі **«Вывад на HUD» → «Віджэт навігацыі ў прыборнай панэлі»** можна выключыць, калі сімуляваныя апавяшчэнні AMap канфліктуюць з уласным навігацыйным віджэтам аўтамабіля.

### Выбар мэты DiLink (v1.2.0+)

На экране наладкі ёсць селектар **Auto / DiLink 3 / DiLink 5**:
- **Auto** — рэкамендавана (аўтавызначэнне па `ro.build.product` / `ro.vehicle.type`).
- **DiLink 3** — стабільны шлях для DiLink 3.0.
- **DiLink 5** — эксперыментальны вывад для DiLink 5/6 (broadcast-first, фармат OpenBYD).

---

## Опцыянальна: Accessibility у Наладах

На DiLink 3.0 пераключальнік **заблакіраваны** (праверана на Yuan UP). Кнопка **Open accessibility settings** у дадатку — для іншых зборак Android.

Калі на вашай прыладзе працуе: уключыце **VoltFlow Nav**, вярніцеся ў дадатак, дазвольце захоп экрана.

---

## Альтэрнатыва: ADB з ПК адзін раз

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.2.0.apk
```

Толькі grant:

```bash
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

Далей на аўтамабілі — захоп экрана і **Disable background Apps = OFF**.

---

## Дадатак: праверка апавяшчэнняў Yandex

Для магчымага будучага мосту праз апавяшчэнні (мадэль OpenBYD), праверце, ці запаўняе Yandex поля на вашай зборцы DiLink:

```bash
adb logcat -c
# Запусціце маршрут у Yandex, затым:
adb shell dumpsys notification --noredact | grep -A30 ru.yandex.yandexnavi
```

Непустыя `title` / `text` / `subText` у **ongoing**-апавяшчэнні патрэбныя. Калі пуста — заставайцеся на Accessibility + MediaProjection. Гл. [YANDEX_UI.md](YANDEX_UI.md).
