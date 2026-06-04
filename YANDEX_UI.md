# UI панэлі манёўраў Яндэкс Навігатара (для AccessibilityService)

**Мова:** Беларуская · [English](docs/YANDEX_UI.en.md) · [Русский](docs/YANDEX_UI.ru.md)

Яндэкс Навігатар (`ru.yandex.yandexnavi`) **не** перадае пакрокавую навігацыю праз
апавяшчэнне (`title="Навигатор запущен"`, text/contentView null) ці MediaSession.
Даныя толькі на экране, таму чытаем іх праз AccessibilityService.

Захоплена праз `uiautomator dump` падчас актыўнай навігацыі. Усе `content-desc` пустыя.

| Поле | resource-id | прыклад | нататкі |
|------|-------------|---------|---------|
| адлегласць да манёўру (лічба) | `ru.yandex.yandexnavi:id/text_maneuverballoon_distance` | `100` | → SEG_REMAIN_DIS |
| адлегласць (адзінка) | `ru.yandex.yandexnavi:id/text_maneuverballoon_metrics` | ` м` / ` км` | для км × 1000 |
| наступная вуліца | `ru.yandex.yandexnavi:id/text_nextstreet` | `ul. Pryklad` | → NEXT_ROAD_NAME (трансліт) |
| рэштак маршруту | `ru.yandex.yandexnavi:id/textview_eta_distance` | `2,7 км` | → ROUTE_REMAIN_DIS |
| час да мэты | `ru.yandex.yandexnavi:id/textview_eta_time` | `5 мин` | → ROUTE_REMAIN_TIME |
| час прыбыцця | `ru.yandex.yandexnavi:id/textview_eta_arrival` | `11:05` | → ETA_TEXT |
| бягучая дарога | `ru.yandex.yandexnavi:id/statusPanel` / `text_statuspanel` | `pr. Pryklad` | інфармацыйна |
| хуткасць | `ru.yandex.yandexnavi:id/text_speed_value` | `0` | не выкарыстоўваецца |
| абмежаванне | `ru.yandex.yandexnavi:id/text_speedlimit` | `60` | не выкарыстоўваецца |
| **стрэлка манёўру** | `ru.yandex.yandexnavi:id/image_maneuverballoon_maneuver` | image, bounds ~84×84 | **без тэксту/desc — напрамак праз класіфікацыю захопу экрана** |

## Напрамак (NEW_ICON) — складаная частка

Напрамак манёўру — ImageView без тэксту і content-desc, з дрэва вузлоў не зчытваецца.
План: захапіць вобласць стрэлкі (bounds з вузла) праз MediaProjection і класіфікаваць
налева/направа/прама геаметрычна (як у OpenBYD). MediaProjection можна дазволіць праз ADB без дыялога:
`adb shell appops set com.bridge.yandexbyd PROJECT_MEDIA allow`.

Фаза 1 (толькі тэкст) адпраўляе NEW_ICON=STRAIGHT як заглушку, пакуль не з'явіцца класіфікатар.
