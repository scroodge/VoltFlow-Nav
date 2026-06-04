# Yandex Navigator turn-panel UI (for AccessibilityService reading)

**Мова / Language:** [Беларуская](README.md) · [English](docs/README.en.md) · [Русский](docs/README.ru.md)

**Каротка (be):** Yandex не аддае манёўры праз апавяшчэнні — чытаем resource-id панэлі павароту праз AccessibilityService; стрэлку класіфікуем з захопу экрана.

---

Yandex Navigator (`ru.yandex.yandexnavi`) does **not** expose turn-by-turn via its
notification (`title="Навигатор запущен"`, text/contentView null) or MediaSession.
The data is only on-screen, so we read it with an AccessibilityService.

Captured via `uiautomator dump` during active navigation. All `content-desc` are empty.

| Field | resource-id | example | notes |
|-------|-------------|---------|-------|
| maneuver distance (number) | `ru.yandex.yandexnavi:id/text_maneuverballoon_distance` | `100` | → SEG_REMAIN_DIS |
| maneuver distance (unit)   | `ru.yandex.yandexnavi:id/text_maneuverballoon_metrics`  | ` м` / ` км` | multiply by 1000 for км |
| next street                | `ru.yandex.yandexnavi:id/text_nextstreet`               | `ul. Pryklad` | → NEXT_ROAD_NAME (transliterate) |
| route remaining distance   | `ru.yandex.yandexnavi:id/textview_eta_distance`         | `2,7 км` | → ROUTE_REMAIN_DIS |
| route remaining time       | `ru.yandex.yandexnavi:id/textview_eta_time`             | `5 мин`  | → ROUTE_REMAIN_TIME |
| ETA clock                  | `ru.yandex.yandexnavi:id/textview_eta_arrival`          | `11:05`  | → ETA_TEXT |
| current road (status)      | `ru.yandex.yandexnavi:id/statusPanel` / `text_statuspanel` | `pr. Pryklad` | informational |
| current speed              | `ru.yandex.yandexnavi:id/text_speed_value`             | `0` | not used |
| speed limit                | `ru.yandex.yandexnavi:id/text_speedlimit`              | `60` | not used |
| **maneuver arrow**         | `ru.yandex.yandexnavi:id/image_maneuverballoon_maneuver` | image, bounds ~84×84 | **NO text/desc — direction needs image classification (Phase 2)** |

## Direction (NEW_ICON) — the hard part

The maneuver direction is an ImageView with no text or content-desc, so it can't be read
from the node tree. Plan: capture the arrow's pixel region (bounds from the node) via
MediaProjection and classify left/right/straight/etc geometrically (as OpenBYD did).
MediaProjection can be granted over ADB without the consent dialog:
`adb shell appops set com.bridge.yandexbyd PROJECT_MEDIA allow`.

Phase 1 (text only) sends NEW_ICON=STRAIGHT as a placeholder until the classifier lands.
