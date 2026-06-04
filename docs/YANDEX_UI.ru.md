# UI панели манёвров Яндекс Навигатора (для AccessibilityService)

**Язык:** [Беларуская](../YANDEX_UI.md) · [English](YANDEX_UI.en.md) · **Русский**

Яндекс Навигатор (`ru.yandex.yandexnavi`) **не** отдаёт пошаговую навигацию через
уведомление (`title="Навигатор запущен"`, text/contentView null) или MediaSession.
Данные только на экране, поэтому читаем их через AccessibilityService.

Снято через `uiautomator dump` во время активной навигации. Все `content-desc` пустые.

| Поле | resource-id | пример | примечания |
|------|-------------|--------|------------|
| расстояние до манёвра (число) | `ru.yandex.yandexnavi:id/text_maneuverballoon_distance` | `100` | → SEG_REMAIN_DIS |
| расстояние (единица) | `ru.yandex.yandexnavi:id/text_maneuverballoon_metrics` | ` м` / ` км` | для км × 1000 |
| следующая улица | `ru.yandex.yandexnavi:id/text_nextstreet` | `ul. Pryklad` | → NEXT_ROAD_NAME (транслит) |
| остаток маршрута | `ru.yandex.yandexnavi:id/textview_eta_distance` | `2,7 км` | → ROUTE_REMAIN_DIS |
| время до цели | `ru.yandex.yandexnavi:id/textview_eta_time` | `5 мин` | → ROUTE_REMAIN_TIME |
| время прибытия | `ru.yandex.yandexnavi:id/textview_eta_arrival` | `11:05` | → ETA_TEXT |
| текущая дорога | `ru.yandex.yandexnavi:id/statusPanel` / `text_statuspanel` | `pr. Pryklad` | информационно |
| скорость | `ru.yandex.yandexnavi:id/text_speed_value` | `0` | не используется |
| ограничение | `ru.yandex.yandexnavi:id/text_speedlimit` | `60` | не используется |
| **стрелка манёвра** | `ru.yandex.yandexnavi:id/image_maneuverballoon_maneuver` | image, bounds ~84×84 | **без текста/desc — направление через классификацию захвата экрана** |

## Направление (NEW_ICON) — сложная часть

Направление манёвра — ImageView без текста и content-desc, из дерева узлов не читается.
План: захватить область стрелки (bounds из узла) через MediaProjection и классифицировать
влево/вправо/прямо геометрически (как в OpenBYD). MediaProjection можно разрешить через ADB без диалога:
`adb shell appops set com.bridge.yandexbyd PROJECT_MEDIA allow`.

Фаза 1 (только текст) отправляет NEW_ICON=STRAIGHT как заглушку, пока не появится классификатор.
