# Пратакол навігацыі на прыборнай панэлі BYD DiLink 3.0 (Yuan UP, Кітай)

**Мова:** Беларуская · [English](docs/CLUSTER_PROTOCOL.en.md) · [Русский](docs/CLUSTER_PROTOCOL.ru.md)

Рэверс-інжынірынг з `com.example.amapservice` (`/system/priv-app/AmapService/AmapService.apk`)
на `ro.build.product=DiLink3.0`, Android 10, `ro.vehicle.type=Di3.0_3.5UI`.

## Як кіруецца прыборная панэль

```
нав. дадатак ──AUTONAVI_STANDARD_BROADCAST_SEND──▶ com.example.amapservice ──CAN bus──▶ прыборная панэль
```

`com.example.amapservice` — **пастаянная сістэмная служба**, якая рэгіструе *дынамічны* прымальнік для
`AUTONAVI_STANDARD_BROADCAST_SEND` (неявны broadcast — мэтавы пакет не патрэбны) і перасылае даныя на
прыборку праз `sendNavigateInfoToCAN()`. Гэта штокавая інтэграцыя AMap «трэці бок праз broadcast».
**Без root, без OpenBYD, без прывілейаванага проксі** — любая дадатка можа адправіць гэты broadcast.

Правіла прыярытэту: калі штатная навігацыя BYD AMap (`com.byd.automap`) актыўна (`IS_BYD_MAP=true`,
naviState 0/1), broadcast ад трэціх бакоў **ігнаруецца** (AmapService.java:253). Мост працуе, калі BYD AMap
**не** вядзе маршрут. Адпраўляйце `IS_BYD_MAP=false` (ці не перадавайце поле).

## Broadcast абнаўлення навігацыі (на кожны манёўр)

Дзеянне: `AUTONAVI_STANDARD_BROADCAST_SEND`

| Extra | Тып | Абавяз. | Значэнне |
|-------|-----|---------|----------|
| `KEY_TYPE` | int | **так** | `10001` = звесткі пра маршрут (адзінае значэнне для паваротаў) |
| `TYPE` | int | **так** | naviState — павінен быць `0` або `1`, інакш абнаўленне адкідваецца (AmapService.java:270) |
| `NEW_ICON` | int | так | id іконкі павароту (табліца ніжэй) — манёўр на прыборцы |
| `SEG_REMAIN_DIS` | int | так | метры да наступнага манёўру |
| `NEXT_ROAD_NAME` | string | так | назва дарогі пасля манёўру |
| `ROUTE_REMAIN_DIS` | int | рэк. | метры да пункту прызначэння |
| `ROUTE_REMAIN_TIME` | int | рэк. | секунды да пункту прызначэння |
| `NEXT_SEG_REMAIN_DIS` | int | опц. | метры да манёўру пасля наступнага |
| `NEXT_NEXT_TURN_ICON` | int | опц. | id іконкі для манёўру пасля наступнага |
| `NEXT_NEXT_ROAD_NAME` | string | опц. | дарога пасля манёўру пасля наступнага |
| `ROUNG_ABOUT_NUM` / `NEXT_ROUNG_ABOUT_NUM` | int | опц. | нумар з'езду з кольца |
| `ETA_TEXT` | string | опц. | час прыбыцця (тэкст) |
| `EXIT_NAME_INFO` / `EXIT_DIRECTION_INFO` | string | опц. | назва / напрамак з'езду з трасы |
| `SEG_REMAIN_DIS_AUTO` / `ROUTE_REMAIN_DIS_AUTO` / `ROUTE_REMAIN_TIME_AUTO` | string | опц. | ужо адфарматаваныя радкі для экрана |
| `IS_BYD_MAP` | bool | не | false або адсутнічае для трэцяга боку (мы) |

## Broadcast спынення / канца навігацыі

Дзеянне `AUTONAVI_STANDARD_BROADCAST_SEND` з:
- `KEY_TYPE` = `10019`
- `EXTRA_STATE` = `9` або `12` → ачыстка прыборкі (AmapService.java:388)

## Значэнні NEW_ICON (індэкс у AmapService.TURN_STRING; служба мапіць на CAN праз TurnIdMapToCAN)

| id | значэнне (zh) | для Yandex |
|----|---------------|------------|
| 2  | 左转 паварот налева | налева |
| 3  | 右转 паварот направа | направа |
| 4  | 左前方 лёгка налева | трымацца налева |
| 5  | 右前方 лёгка направа | трымацца направа |
| 6  | 左后方 рэзка налева | рэзка налева |
| 7  | 右后方 рэзка направа | рэзка направа |
| 8  | 左转掉头 разварот (злева) | разварот |
| 9  | 直行 прама | прама |
| 10 | 到达途经点 промежкавая кропка | via |
| 11 | 进入环岛 уезд на кольца (RHT, супраць гадз. стрэлкі) | уезд на кольца |
| 12 | 驶出环岛 з'езд з кольца (RHT) | з'езд |
| 13 | 服务区 зона абслугоўвання | — |
| 14 | 收费站 плацоўка | плата |
| 15 | 到达目的地 прыбыцце | прыбыцце |
| 16 | 隧道 тунэль | — |
| 17 | 进入环岛 уезд (LHT, па гадз. стрэлцы) | (кольца для леваручнага руху) |
| 18 | 驶出环岛 з'езд (LHT) | — |
| 19 | 右转掉头 разварот (справа/LHT) | — |
| 20 | 顺行 працягваць прама | прама |
| 21–28 | варыянты кольца з напрамкам (RHT 21–24, LHT 25–28) | кольца з напрамкам |

У Расіі праваручны рух, таму актуальныя павароты: 2–9, 11/12 (кольца), 15, 20.

## Абмежаванне шрыфта — транслітарацыя не-лацінскіх назваў

Рэле перадае `NEXT_ROAD_NAME` у UTF-16LE і адпраўляе праз CAN у
`BYDAutoInstrumentDevice` (AmapService.java:487, `getBytes("UnicodeLittleUnmarked")`).
Кодпойнты даходзяць цэлымі, але **шрыфт прашыўкі прыборкі мае толькі лацініцу і кітайскія
іерогліфы — без кірыліцы**. На аўтамабілі: `routeName='TEST'` адлюстроўваўся, `'Сурганова'`
паказала толькі стрэлку (пустая назва). Кірыліцу трэба **транслітэраваць у лацініцу** перад
адпраўкай (напр. «Сурганова» → «Surganova»). Мост робіць гэта праз `Translit.kt`.
(У OpenBYD была такая ж патрэба — опцыя `hud_transliterator_enabled`.)

## Тэставыя каманды (adb)

Спачатку пераканайцеся, што BYD AMap **не** вядзе навігацыю. Логі: `adb logcat -s AmapService`.

```sh
# адзін паварот налева, 350 м, на «Тверская», 5,2 км / 10 хв да мэты
adb shell "am broadcast -a AUTONAVI_STANDARD_BROADCAST_SEND \
  --ei KEY_TYPE 10001 --ei TYPE 0 --ei NEW_ICON 2 --ei SEG_REMAIN_DIS 350 \
  --es NEXT_ROAD_NAME 'Тверская' --ei ROUTE_REMAIN_DIS 5200 --ei ROUTE_REMAIN_TIME 600"

# убудаваны саматэст: цыкл іконак 2..28 на прыборцы (action DEBUG_CASE)
adb shell "am broadcast -a DEBUG_CASE --es routeName 'TEST' --es routeDist '300'"

# спыніць / ачысціць прыборку
adb shell "am broadcast -a AUTONAVI_STANDARD_BROADCAST_SEND --ei KEY_TYPE 10019 --ei EXTRA_STATE 9"
```
