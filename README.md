



# VoltFlow Nav

*Яндэкс Навігатар на прыборнай панэлі BYD (HUD)*

**Мова:** Беларуская · English · Русский







**VoltFlow Nav** — свабодны мост з **Яндэкс Навігатара** на прыборную панэль **BYD DiLink 3.0** без root і без OpenBYD. Частка экасістэмы [VoltFlow](https://github.com/scroodge/VoltFlow) (зарядка, Mate-тэлеметрыя).

> **Адмова.** Праект не звязаны з Yandex, BYD ці AMap/Gaode. Усталёўка на свой рызыка. Патрабуюцца Accessibility і захоп экрана для чытання панэлі манёўраў Yandex.

## Што працуе сёння


| Дысплей                                 | Статус        | v1.0                                                    |
| --------------------------------------- | ------------- | ------------------------------------------------------- |
| **Прыборная панэль (HUD)**              | ✓ Працуе      | Стрэлкі манёўраў, вуліца (лаціна), ETA, рэштак маршруту |
| **Панэль кіравання (цэнтр)**            | ~ Эксперымент | Часткова; не абяцаем у v1.0                             |
| **Поўная падтрымка цэнтральнай панэлі** | У планах      | Будучая версія                                          |

<p align="center">
  <img src="docs/assets/readme/hud-demo.jpg" width="600" alt="HUD demo" />
</p>

## Сумяшчальнасць

- **Аўтамабіль:** BYD Yuan UP 2024 (тэст)
- **DiLink:** 3.0 (`ro.build.product=DiLink3.0`, `ro.vehicle.type=Di3.0_3.5UI`)
- **Android:** 10
- **Навігатар:** `ru.yandex.yandexnavi`

## Хуткая ўстаноўка

1. Спампуйце APK з [Releases](https://github.com/scroodge/VoltFlow-Nav/releases) (`VoltFlowNav-*.apk`).
2. Усталюйце на галавную прыладу (файлавы менеджар або `adb install -r VoltFlowNav.apk`).
3. Адкрыйце **VoltFlow Nav** — экран наладкі ([SETUP.md](SETUP.md), [en](docs/SETUP.en.md), [ru](docs/SETUP.ru.md)):
   - **Узровень 1:** **Open accessibility settings** → уключыце службу (без ПК).
   - **Узровень 2:** **Grant via Shizuku**, калі Налады блакіруюць (на Android 10 Shizuku — адзін раз USB ADB).
   - **Узровень 3:** ПК — `./setup-car.sh` або `adb shell pm grant ... WRITE_SECURE_SETTINGS`.
4. На аўтамабілі: **захоп экрана** (пасля кожнай перазагрузкі зноў).
5. **Не** запускайце BYD AMap падчас Yandex.
6. Маршрут у Yandex, Yandex **на экране**.

Наступныя версіі: у дадатку ўключыце **Правяраць абнаўленні пры запуску** — прапануе спампаваць новы APK з [GitHub Releases](https://github.com/scroodge/VoltFlow-Nav/releases), калі ён новей усталяванага.

## Як гэта працуе



```
Yandex Navigator
  ├─ AccessibilityService → адлегласць, вуліца, ETA
  └─ MediaProjection → стрэлка манёўра
        ↓
VoltFlow Nav → AUTONAVI_STANDARD_BROADCAST_SEND → com.example.amapservice → HUD
```

Тэхнічныя дэталі: [CLUSTER_PROTOCOL.md](CLUSTER_PROTOCOL.md), [YANDEX_UI.md](YANDEX_UI.md).

## Дакументацыя


| Тэма         | Беларуская                                                | English                           | Русский                           |
| ------------ | --------------------------------------------------------- | --------------------------------- | --------------------------------- |
| Наладка      | [SETUP.md](SETUP.md)                                      | [en](docs/SETUP.en.md)            | [ru](docs/SETUP.ru.md)            |
| Удзел        | [CONTRIBUTING.md](CONTRIBUTING.md)                        | [en](docs/CONTRIBUTING.en.md)     | [ru](docs/CONTRIBUTING.ru.md)     |
| Пратакол HUD | [CLUSTER_PROTOCOL.md](CLUSTER_PROTOCOL.md)                | [en](docs/CLUSTER_PROTOCOL.en.md) | [ru](docs/CLUSTER_PROTOCOL.ru.md) |
| UI Yandex    | [YANDEX_UI.md](YANDEX_UI.md)                              | [en](docs/YANDEX_UI.en.md)        | [ru](docs/YANDEX_UI.ru.md)        |
| Рэліз        | [PUBLISH.md](docs/PUBLISH.md)                             | [en](docs/PUBLISH.en.md)          | [ru](docs/PUBLISH.ru.md)          |
| Маркетынг    | [MARKETING_LAUNCH.md](docs/MARKETING_LAUNCH.md)           | [en](docs/MARKETING_LAUNCH.en.md) | [ru](docs/MARKETING_LAUNCH.ru.md) |
| Патч OpenBYD | [PATCH_NOTES.md](openbyd-patch/PATCH_NOTES.md)            | [en](docs/PATCH_NOTES.en.md)      | [ru](docs/PATCH_NOTES.ru.md)      |
| Changelog    | [CHANGELOG.md](CHANGELOG.md) · [be](docs/CHANGELOG.be.md) | [en](CHANGELOG.md)                | [ru](docs/CHANGELOG.ru.md)        |


## Абмежаванні

- Yandex павінен быць **бачны** на экране падчас руху.
- Пасля **перазагрузкі** — зноў захоп экрана (кнопка «Restart screen capture» у дадатку).
- Манёўры v1.0: у асноўным **лева / права / прама** (іншыя — у планах).
- Цэнтральная панэль — эксперыментальна.

## План развіцця

- Поўнае адлюстраванне на панэлі кіравання
- Дакладнейшыя манёўры (лёгкі/рэзкі паварот, разварот, кольца)

## Частка VoltFlow


| Прадукт                    | Спасылка                                                                                                                                 |
| -------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| VoltFlow (зарядка, PWA)    | [github.com/scroodge/VoltFlow](https://github.com/scroodge/VoltFlow) · [volt-flow-beige.vercel.app](https://volt-flow-beige.vercel.app/) |
| VoltFlow Mate (тэлеметрыя) | у рэпазіторыі VoltFlow                                                                                                                   |


## Падтрымаць праект

Свабодна і **MIT**. Добраахвотныя ахвяраванні дапамагаюць тэсціраваць на BYD і развіваць экасістэму VoltFlow:

- [Buy Me a Coffee — scroodge](https://buymeacoffee.com/scroodge)

## Зборка

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/VoltFlowNav-1.0.0-debug.apk
```

Логі: `adb logcat -s VoltFlowNav`

## Удзел

[CONTRIBUTING.md](CONTRIBUTING.md)

## Ліцэнзія

[MIT](LICENSE) — Alexey Washjurine (scroodge).