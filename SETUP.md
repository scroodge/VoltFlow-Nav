# VoltFlow Nav — наладка (DiLink 3.0 / Android 10)

**Мова:** **Беларуская** · [English](docs/SETUP.en.md) · [Русский](docs/SETUP.ru.md)

На **BYD DiLink 3.0** уключыць VoltFlow Nav у сістэмных **Спецыяльных магчымасцях** **не выходзіць** (пераключальнік заблакіраваны). Працуе шлях праз **Shizuku** на галавной прыладзе, затым **Grant via Shizuku** у VoltFlow Nav.

---

## Рэкамендуецца: Shizuku

| Крыніца | Спасылка |
|---------|----------|
| APK | [GitHub Releases — Shizuku](https://github.com/RikkaApps/Shizuku/releases) |
| Play Store | [Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) |
| Інструкцыя | [shizuku.rikka.app/guide/setup](https://shizuku.rikka.app/guide/setup/) |
| Android 10, USB ADB | [Падключэнне да камп'ютара](https://shizuku.rikka.app/guide/setup/#start-by-connecting-to-a-computer) |

Пакет: `moe.shizuku.privileged.api`

### Каротка

1. Усталюйце **VoltFlow Nav** і **Shizuku**.
2. Уключыце **Рэжым распрацоўшчыка** і **ADB па USB**.
3. На ПК: `adb devices`, затым запуск Shizuku:

```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```

(альбо `/sdcard/Android/data/.../start.sh`)

4. У Shizuku — статус **running**.
5. **VoltFlow Nav** → **Grant via Shizuku** → дазвольце доступ.
6. **Restart screen capture** (пасля кожнай перазагрузкі зноў).

Пасля перазагрузкі головнай — зноў запусціце Shizuku (крок 3). Правы VoltFlow захоўваюцца да выдалення APK.

Поўная інструкцыя: [SETUP.en.md](docs/SETUP.en.md).

---

## Опцыянальна: Accessibility у Наладах

На DiLink 3.0 звычайна заблакіравана (Yuan UP). Кнопка ў дадатку — для іншых прылад.

---

## Альтернатыва: ADB з ПК

```bash
adb connect <car-ip>:5555
./setup-car.sh /path/to/VoltFlowNav-v1.0.0.apk
```

---

## Навігацыя

Не запускайце BYD AMap разам з Yandex. Yandex павінен быць **на экране**.
