**Язык:** [Беларуская](../README.md) · [English](README.en.md) · **Русский**

<div align="center">

<img src="assets/brand/voltflow-icon.png" width="96" alt="VoltFlow Nav" />

# VoltFlow Nav

*Яндекс Навигатор на приборной панели BYD (HUD)*

<p>
  <a href="https://github.com/scroodge/VoltFlow-Nav/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-00E676?style=flat-square" alt="MIT" /></a>
  <a href="https://github.com/scroodge/VoltFlow"><img src="https://img.shields.io/badge/VoltFlow-ecosystem-2962FF?style=flat-square" alt="VoltFlow" /></a>
</p>

<img src="assets/readme/hero-banner.png" width="800" alt="VoltFlow Nav" />

</div>

**VoltFlow Nav** — бесплатный мост из **Яндекс Навигатора** на **приборку BYD DiLink 3.0** без root и без OpenBYD. Часть экосистемы [VoltFlow](https://github.com/scroodge/VoltFlow).

> **Отказ от ответственности.** Не связан с Yandex, BYD или AMap/Gaode. Установка на свой риск. Нужны Accessibility и захват экрана для чтения панели манёвров Yandex.

## Что работает сейчас

<table>
<tr><th>Экран</th><th>Статус</th><th>v1.0</th></tr>
<tr><td><strong>Приборная панель (HUD)</strong></td><td>✓ Работает</td><td>Стрелки, улица (латиница), ETA, остаток маршрута</td></tr>
<tr><td><strong>Центральный экран</strong></td><td>~ Эксперимент</td><td>Частично; в v1.0 не обещаем</td></tr>
<tr><td><strong>Полная поддержка центра</strong></td><td>В планах</td><td>Будущая версия</td></tr>
</table>

<img src="assets/readme/hud-demo.jpg" width="600" alt="HUD demo" />

## Совместимость

- **Авто:** BYD Yuan UP (тест)
- **DiLink:** 3.0 (`ro.build.product=DiLink3.0`, `ro.vehicle.type=Di3.0_3.5UI`)
- **Android:** 10
- **Навигатор:** `ru.yandex.yandexnavi`

## Быстрая установка

1. Скачайте APK из [Releases](https://github.com/scroodge/VoltFlow-Nav/releases) (`VoltFlowNav-*.apk`).
2. Установите на головное устройство (файловый менеджер или `adb install -r VoltFlowNav.apk`).
3. Откройте **VoltFlow Nav** — следуйте экрану настройки в приложении.
4. Один раз с ПК по ADB:

<details>
<summary>ADB: WRITE_SECURE_SETTINGS</summary>

```bash
adb connect <car-ip>:5555
adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS
```

</details>

5. На машине: разрешите **захват экрана** (кнопка в приложении; после каждой перезагрузки).
6. **Не** запускайте штатную навигацию BYD AMap.
7. Маршрут в Yandex — Yandex **на экране**.

Следующие версии: включите **Проверять обновления при запуске** — приложение предложит скачать новый APK с [GitHub Releases](https://github.com/scroodge/VoltFlow-Nav/releases), если он новее установленного.

## Как это работает

<img src="assets/readme/flow-diagram.png" width="800" alt="Data flow" />

```
Яндекс Навигатор
  ├─ AccessibilityService → расстояние, улица, ETA
  └─ MediaProjection → стрелка манёвра
        ↓
VoltFlow Nav → AUTONAVI_STANDARD_BROADCAST_SEND → com.example.amapservice → HUD
```

См. [CLUSTER_PROTOCOL.ru.md](CLUSTER_PROTOCOL.ru.md), [YANDEX_UI.ru.md](YANDEX_UI.ru.md).

## Документация

| Тема | Беларуская | English | Русский |
|------|------------|---------|---------|
| Участие | [be](../CONTRIBUTING.md) | [en](CONTRIBUTING.en.md) | [ru](CONTRIBUTING.ru.md) |
| Протокол HUD | [be](../CLUSTER_PROTOCOL.md) | [en](CLUSTER_PROTOCOL.en.md) | [ru](CLUSTER_PROTOCOL.ru.md) |
| UI Yandex | [be](../YANDEX_UI.md) | [en](YANDEX_UI.en.md) | [ru](YANDEX_UI.ru.md) |
| Релиз | [be](PUBLISH.md) | [en](PUBLISH.en.md) | [ru](PUBLISH.ru.md) |
| Маркетинг | [be](MARKETING_LAUNCH.md) | [en](MARKETING_LAUNCH.en.md) | [ru](MARKETING_LAUNCH.ru.md) |
| Патч OpenBYD | [be](../openbyd-patch/PATCH_NOTES.md) | [en](PATCH_NOTES.en.md) | [ru](PATCH_NOTES.ru.md) |
| Changelog | [be](CHANGELOG.be.md) | [en](../CHANGELOG.md) | [ru](CHANGELOG.ru.md) |

## Ограничения

- Yandex должен быть **виден** на экране.
- После **перезагрузки** — снова захват экрана (кнопка «Restart screen capture» в приложении).
- v1.0: в основном **лево / право / прямо**.
- Центральный экран — эксперимент.

## Дорожная карта

- Полное зеркалирование на центральный экран
- Точные манёвры (лёгкий/крутой, разворот, кольца)

## Часть VoltFlow

| Продукт | Ссылка |
|---------|--------|
| VoltFlow (зарядка) | [GitHub](https://github.com/scroodge/VoltFlow) · [PWA](https://volt-flow-beige.vercel.app/) |
| VoltFlow Mate | в репозитории VoltFlow |

## Поддержать проект

Бесплатно, **MIT**. Донаты помогают тестам на BYD и экосистеме VoltFlow:

- [Buy Me a Coffee — scroodge](https://buymeacoffee.com/scroodge)

## Сборка

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/VoltFlowNav-1.0.0-debug.apk
adb logcat -s VoltFlowNav
```

## Участие

[CONTRIBUTING.md](../CONTRIBUTING.md) · [CONTRIBUTING.ru.md](CONTRIBUTING.ru.md)

## Лицензия

[MIT](../LICENSE)
