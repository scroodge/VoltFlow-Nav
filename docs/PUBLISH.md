# Публікацыя на GitHub

**Мова:** Беларуская · [English](PUBLISH.en.md) · [Русский](PUBLISH.ru.md)

## Стварэнне рэпазіторыя

```bash
gh repo create scroodge/VoltFlow-Nav --public --source=. --remote=origin --description "Yandex Navigator on BYD DiLink 3.0 HUD. Part of VoltFlow."
```

Або стварыце **VoltFlow-Nav** на GitHub уручную, потым:

```bash
git remote add origin https://github.com/scroodge/VoltFlow-Nav.git
git push -u origin main
```

## Падпіс рэлізу (лакальна)

1. Стварыце keystore (адзін раз): `voltflow-nav-release.keystore` у карані праекта.
2. Скапіруйце [`local.properties.example`](../local.properties.example) → `local.properties` і ўкажыце ключы (файл у `.gitignore`).
3. Калі пароль змяшчае `#`, экраніруйце як `\#` у `local.properties`.

## Рэліз (changelog → версія → зборка → тэг)

Той жа паток, што ў [BYDMate-own](https://github.com/scroodge/BYDMate-own):

1. Згенеруйце changelog з git-камітаў (рэкамендуем Conventional Commits):

```bash
./gradlew releaseChangelog
```

Прагляд без змены файлаў:

```bash
./gradlew releaseChangelog -PdryRun=true
```

Версія ўручную:

```bash
./gradlew releaseChangelog -PreleaseVersion=1.2.1
```

2. Праверце і пры неабходнасці адрэдагуйце [`CHANGELOG.md`](../CHANGELOG.md) (кананічны файл — англійская, для збіркі).

3. **`versionName`** і **`versionCode`** у [`app/build.gradle`](../app/build.gradle) павінны супадаць з апошнім раздзелам у `CHANGELOG.md` (напр. `## [1.2.1] - 2026-06-05`).

4. Зборка release APK (`verifyReleaseVersion` упадзе, калі версіі не супадаюць):

```bash
./gradlew clean assembleRelease
# Выхад: app/build/outputs/apk/release/VoltFlowNav-v1.2.1.apk
```

5. Commit, тэг, push:

```bash
git add CHANGELOG.md app/build.gradle
git commit -m "chore(release): v1.2.1"
git tag v1.2.1
git push origin main v1.2.1
```

6. GitHub Actions ([release.yml](../.github/workflows/release.yml)) далучае `VoltFlowNav-v1.2.1.apk` да Release.

Убудаваная праверка абнаўленняў: `https://api.github.com/repos/scroodge/VoltFlow-Nav/releases/latest`, усталёўка першага `.apk` asset.

CI-зборкі без падпісу, пакуль не дадасце секреты keystore ў GitHub Actions.

## Першы рэліз

У `CHANGELOG.md` павінен быць датаваны раздзел з `versionName`, потым:

```bash
./gradlew assembleRelease
git tag v1.2.0
git push origin v1.2.0
```

## Перад push

```bash
git ls-files | rg 'arrows/|openbyd-patch|ui\.xml|\.apk'   # павінна быць пуста
./gradlew assembleDebug
```
