# Протокол навигации на приборной панели BYD DiLink 3.0 (Yuan UP, Китай)

**Язык:** [Беларуская](../CLUSTER_PROTOCOL.md) · [English](CLUSTER_PROTOCOL.en.md) · **Русский**

Реверс-инжиниринг из `com.example.amapservice` (`/system/priv-app/AmapService/AmapService.apk`)
на `ro.build.product=DiLink3.0`, Android 10, `ro.vehicle.type=Di3.0_3.5UI`.

## Как управляется приборная панель

```
нав. приложение ──AUTONAVI_STANDARD_BROADCAST_SEND──▶ com.example.amapservice ──CAN bus──▶ приборка
```

`com.example.amapservice` — **постоянная системная служба**, регистрирующая *динамический* приёмник для
`AUTONAVI_STANDARD_BROADCAST_SEND` (неявный broadcast — целевой пакет не нужен) и пересылающая данные на
приборку через `sendNavigateInfoToCAN()`. Штатная интеграция AMap «третья сторона через broadcast».
**Без root, без OpenBYD, без привилегированного прокси** — любое приложение может отправить этот broadcast.

Правило приоритета: если штатная навигация BYD AMap (`com.byd.automap`) активна (`IS_BYD_MAP=true`,
naviState 0/1), broadcast от третьих сторон **игнорируется** (AmapService.java:253). Мост работает, когда BYD AMap
**не** ведёт маршрут. Отправляйте `IS_BYD_MAP=false` (или не передавайте поле).

## Broadcast обновления навигации (на каждый манёвр)

Действие: `AUTONAVI_STANDARD_BROADCAST_SEND`

| Extra | Тип | Обяз. | Значение |
|-------|-----|-------|----------|
| `KEY_TYPE` | int | **да** | `10001` = сведения о маршруте (единственное значение для поворотов) |
| `TYPE` | int | **да** | naviState — должен быть `0` или `1`, иначе обновление отбрасывается (AmapService.java:270) |
| `NEW_ICON` | int | да | id иконки поворота (таблица ниже) — манёвр на приборке |
| `SEG_REMAIN_DIS` | int | да | метры до следующего манёвра |
| `NEXT_ROAD_NAME` | string | да | название дороги после манёвра |
| `ROUTE_REMAIN_DIS` | int | рек. | метры до пункта назначения |
| `ROUTE_REMAIN_TIME` | int | рек. | секунды до пункта назначения |
| `NEXT_SEG_REMAIN_DIS` | int | опц. | метры до манёвра после следующего |
| `NEXT_NEXT_TURN_ICON` | int | опц. | id иконки для манёвра после следующего |
| `NEXT_NEXT_ROAD_NAME` | string | опц. | дорога после манёвра после следующего |
| `ROUNG_ABOUT_NUM` / `NEXT_ROUNG_ABOUT_NUM` | int | опц. | номер съезда с кольца |
| `ETA_TEXT` | string | опц. | время прибытия (текст) |
| `EXIT_NAME_INFO` / `EXIT_DIRECTION_INFO` | string | опц. | название / направление съезда с трассы |
| `SEG_REMAIN_DIS_AUTO` / `ROUTE_REMAIN_DIS_AUTO` / `ROUTE_REMAIN_TIME_AUTO` | string | опц. | уже отформатированные строки для экрана |
| `IS_BYD_MAP` | bool | нет | false или отсутствует для третьей стороны (мы) |

## Broadcast остановки / конца навигации

Действие `AUTONAVI_STANDARD_BROADCAST_SEND` с:
- `KEY_TYPE` = `10019`
- `EXTRA_STATE` = `9` или `12` → очистка приборки (AmapService.java:388)

## Значения NEW_ICON (индекс в AmapService.TURN_STRING; служба мапит на CAN через TurnIdMapToCAN)

| id | значение (zh) | для Yandex |
|----|---------------|------------|
| 2  | 左转 поворот налево | налево |
| 3  | 右转 поворот направо | направо |
| 4  | 左前方 слегка налево | держаться слева |
| 5  | 右前方 слегка направо | держаться справа |
| 6  | 左后方 резко налево | резко налево |
| 7  | 右后方 резко направо | резко направо |
| 8  | 左转掉头 разворот (слева) | разворот |
| 9  | 直行 прямо | прямо |
| 10 | 到达途经点 промежуточная точка | via |
| 11 | 进入环岛 въезд на кольцо (RHT, против час. стрелки) | въезд на кольцо |
| 12 | 驶出环岛 съезд с кольца (RHT) | съезд |
| 13 | 服务区 зона обслуживания | — |
| 14 | 收费站 платёжка | платная |
| 15 | 到达目的地 прибытие | прибытие |
| 16 | 隧道 тоннель | — |
| 17 | 进入环岛 въезд (LHT, по час. стрелки) | (кольцо для левостороннего движения) |
| 18 | 驶出环岛 съезд (LHT) | — |
| 19 | 右转掉头 разворот (справа/LHT) | — |
| 20 | 顺行 продолжать прямо | прямо |
| 21–28 | варианты кольца с направлением (RHT 21–24, LHT 25–28) | кольцо с направлением |

В России правостороннее движение, поэтому актуальны повороты: 2–9, 11/12 (кольцо), 15, 20.

## Ограничение шрифта — транслитерация не-латинских названий

Реле передаёт `NEXT_ROAD_NAME` в UTF-16LE и отправляет по CAN в
`BYDAutoInstrumentDevice` (AmapService.java:487, `getBytes("UnicodeLittleUnmarked")`).
Кодпоинты доходят целыми, но **шрифт прошивки приборки содержит только латиницу и китайские
иероглифы — без кириллицы**. На машине: `routeName='TEST'` отображался, `'Сурганова'`
показала только стрелку (пустое имя). Кириллицу нужно **транслитерировать в латиницу** перед
отправкой (напр. «Сурганова» → «Surganova»). Мост делает это через `Translit.kt`.
(В OpenBYD была та же потребность — опция `hud_transliterator_enabled`.)

## Тестовые команды (adb)

Сначала убедитесь, что BYD AMap **не** ведёт навигацию. Логи: `adb logcat -s AmapService`.

```sh
# один поворот налево, 350 м, на «Тверская», 5,2 км / 10 мин до цели
adb shell "am broadcast -a AUTONAVI_STANDARD_BROADCAST_SEND \
  --ei KEY_TYPE 10001 --ei TYPE 0 --ei NEW_ICON 2 --ei SEG_REMAIN_DIS 350 \
  --es NEXT_ROAD_NAME 'Тверская' --ei ROUTE_REMAIN_DIS 5200 --ei ROUTE_REMAIN_TIME 600"

# встроенный самотест: цикл иконок 2..28 на приборке (action DEBUG_CASE)
adb shell "am broadcast -a DEBUG_CASE --es routeName 'TEST' --es routeDist '300'"

# остановить / очистить приборку
adb shell "am broadcast -a AUTONAVI_STANDARD_BROADCAST_SEND --ei KEY_TYPE 10019 --ei EXTRA_STATE 9"
```
