# BYD DiLink 3.0 cluster navigation protocol (Yuan UP, China)

**Мова / Language:** [Беларуская](README.md) · [English](docs/README.en.md) · [Русский](docs/README.ru.md)

**Каротка (be):** Пратакол штокавага моста AMap — `AUTONAVI_STANDARD_BROADCAST_SEND` → `com.example.amapservice` → HUD. VoltFlow Nav адпраўляе гэты broadcast без root.

---

Reverse-engineered from `com.example.amapservice` (`/system/priv-app/AmapService/AmapService.apk`)
on `ro.build.product=DiLink3.0`, Android 10, `ro.vehicle.type=Di3.0_3.5UI`.

## How the cluster is driven

```
nav app ──AUTONAVI_STANDARD_BROADCAST_SEND──▶ com.example.amapservice ──CAN bus──▶ instrument cluster
```

`com.example.amapservice` is a **persistent system service** that registers a *runtime* receiver for
`AUTONAVI_STANDARD_BROADCAST_SEND` (implicit broadcast — no package targeting needed) and forwards to
the cluster via `sendNavigateInfoToCAN()`. It is the stock AMap "third-party broadcast" integration.
**No root, no OpenBYD, no privileged proxy required** — any app can send this broadcast.

Priority rule: if BYD's own AMap (`com.byd.automap`) is actively navigating (`IS_BYD_MAP=true`,
naviState 0/1), third-party broadcasts are **ignored** (AmapService.java:253). So our bridge only
works when BYD AMap is **not** navigating. Send `IS_BYD_MAP=false` (or omit it).

## Navigation update broadcast (per maneuver)

Action: `AUTONAVI_STANDARD_BROADCAST_SEND`

| Extra | Type | Required | Meaning |
|-------|------|----------|---------|
| `KEY_TYPE` | int | **yes** | `10001` = guidance info (the only value processed for turns) |
| `TYPE` | int | **yes** | naviState — must be `0` or `1` or the update is dropped (AmapService.java:270) |
| `NEW_ICON` | int | yes | turn icon id (see table) — the maneuver shown on the cluster |
| `SEG_REMAIN_DIS` | int | yes | metres to the next maneuver |
| `NEXT_ROAD_NAME` | string | yes | name of the road after the maneuver |
| `ROUTE_REMAIN_DIS` | int | rec. | metres remaining to destination |
| `ROUTE_REMAIN_TIME` | int | rec. | seconds remaining to destination |
| `NEXT_SEG_REMAIN_DIS` | int | opt | metres for the maneuver after next |
| `NEXT_NEXT_TURN_ICON` | int | opt | icon id for the maneuver after next |
| `NEXT_NEXT_ROAD_NAME` | string | opt | road after the maneuver after next |
| `ROUNG_ABOUT_NUM` / `NEXT_ROUNG_ABOUT_NUM` | int | opt | roundabout exit number |
| `ETA_TEXT` | string | opt | arrival clock time text |
| `EXIT_NAME_INFO` / `EXIT_DIRECTION_INFO` | string | opt | highway exit name / direction |
| `SEG_REMAIN_DIS_AUTO` / `ROUTE_REMAIN_DIS_AUTO` / `ROUTE_REMAIN_TIME_AUTO` | string | opt | pre-formatted display strings |
| `IS_BYD_MAP` | bool | no | leave false/absent for a third-party (us) |

## Stop / end navigation broadcast

Action `AUTONAVI_STANDARD_BROADCAST_SEND` with:
- `KEY_TYPE` = `10019`
- `EXTRA_STATE` = `9` or `12`  → clears the cluster (AmapService.java:388)

## NEW_ICON values (index into AmapService.TURN_STRING; service maps to CAN via TurnIdMapToCAN)

| id | meaning (zh) | use for Yandex |
|----|--------------|----------------|
| 2  | 左转 left turn | turn left |
| 3  | 右转 right turn | turn right |
| 4  | 左前方 left-front | slight/keep left |
| 5  | 右前方 right-front | slight/keep right |
| 6  | 左后方 left-back | sharp left |
| 7  | 右后方 right-back | sharp right |
| 8  | 左转掉头 U-turn (left) | U-turn |
| 9  | 直行 straight | go straight |
| 10 | 到达途经点 waypoint | via point |
| 11 | 进入环岛 (RHT, CCW) | enter roundabout |
| 12 | 驶出环岛 (RHT) | exit roundabout |
| 13 | 服务区 service area | — |
| 14 | 收费站 toll station | toll |
| 15 | 到达目的地 destination | arrived |
| 16 | 隧道 tunnel | — |
| 17 | 进入环岛 (LHT, CW) | (left-hand-traffic roundabout) |
| 18 | 驶出环岛 (LHT) | — |
| 19 | 右转掉头 U-turn (right/LHT) | — |
| 20 | 顺行 continue | continue straight |
| 21–28 | 绕环岛 左/右/直/掉头 variants (RHT 21-24, LHT 25-28) | roundabout-with-direction |

Russia is right-hand traffic, so the relevant turns are 2–9, 11/12 (roundabout), 15, 20.

## Font limitation — transliterate non-Latin road names

The relay encodes `NEXT_ROAD_NAME` as UTF-16LE and sends it over CAN to
`BYDAutoInstrumentDevice` (AmapService.java:487, `getBytes("UnicodeLittleUnmarked")`).
The codepoints arrive intact, but the **cluster firmware font only has Latin + Chinese
glyphs — no Cyrillic**. Confirmed on the car: `routeName='TEST'` displayed, `'Сурганова'`
showed only the arrow (blank name). So Cyrillic must be **transliterated to Latin** before
sending (e.g. "Сурганова" → "Surganova"). The bridge does this via `Translit.kt`.
(OpenBYD had the same need — its `hud_transliterator_enabled` option.)

## Test commands (adb)

Make sure BYD AMap is NOT navigating first. Watch logs: `adb logcat -s AmapService`.

```sh
# single left turn, 350 m, onto "Тверская", 5.2 km / 10 min to go
adb shell "am broadcast -a AUTONAVI_STANDARD_BROADCAST_SEND \
  --ei KEY_TYPE 10001 --ei TYPE 0 --ei NEW_ICON 2 --ei SEG_REMAIN_DIS 350 \
  --es NEXT_ROAD_NAME 'Тверская' --ei ROUTE_REMAIN_DIS 5200 --ei ROUTE_REMAIN_TIME 600"

# built-in self-test: cycles icons 2..28 on the cluster (action DEBUG_CASE)
adb shell "am broadcast -a DEBUG_CASE --es routeName 'TEST' --es routeDist '300'"

# stop / clear the cluster
adb shell "am broadcast -a AUTONAVI_STANDARD_BROADCAST_SEND --ei KEY_TYPE 10019 --ei EXTRA_STATE 9"
```
