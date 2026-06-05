# Публикация на GitHub

**Язык:** [Беларуская](PUBLISH.md) · [English](PUBLISH.en.md) · **Русский**

## Создание репозитория

```bash
gh repo create scroodge/VoltFlow-Nav --public --source=. --remote=origin --description "Yandex Navigator on BYD DiLink 3.0 HUD. Part of VoltFlow."
```

Или создайте **VoltFlow-Nav** на GitHub вручную, затем:

```bash
git remote add origin https://github.com/scroodge/VoltFlow-Nav.git
git push -u origin main
```

## Подпись релиза (локально)

1. Создайте keystore (один раз): `voltflow-nav-release.keystore` в корне проекта.
2. Скопируйте [`local.properties.example`](../local.properties.example) → `local.properties` и укажите ключи (файл в `.gitignore`).
3. Если пароль содержит `#`, экранируйте как `\#` в `local.properties`.

## Релиз (changelog → версия → сборка → тег)

Тот же поток, что в [BYDMate-own](https://github.com/scroodge/BYDMate-own):

1. Сгенерируйте changelog из git-коммитов (рекомендуем Conventional Commits):

```bash
./gradlew releaseChangelog
```

Просмотр без изменения файлов:

```bash
./gradlew releaseChangelog -PdryRun=true
```

Версия вручную:

```bash
./gradlew releaseChangelog -PreleaseVersion=1.2.1
```

2. Проверьте и при необходимости отредактируйте [`CHANGELOG.md`](../CHANGELOG.md) (канонический файл на английском для сборки).

3. **`versionName`** и **`versionCode`** в [`app/build.gradle`](../app/build.gradle) должны совпадать с последним разделом в `CHANGELOG.md` (напр. `## [1.2.1] - 2026-06-05`).

4. Сборка release APK (`verifyReleaseVersion` упадёт, если версии не совпадают):

```bash
./gradlew clean assembleRelease
# Выход: app/build/outputs/apk/release/VoltFlowNav-v1.2.0.apk
```

5. Commit, тег, push:

```bash
git add CHANGELOG.md app/build.gradle
git commit -m "chore(release): v1.2.1"
git tag v1.2.1
git push origin main v1.2.1
```

6. GitHub Actions ([release.yml](../.github/workflows/release.yml)) прикрепляет `VoltFlowNav-v1.2.1.apk` к Release.

Встроенная проверка обновлений: `https://api.github.com/repos/scroodge/VoltFlow-Nav/releases/latest`, установка первого `.apk` asset.

CI-сборки без подписи, пока не добавите секреты keystore в GitHub Actions.

## Первый релиз

В `CHANGELOG.md` должен быть датированный раздел с `versionName`, затем:

```bash
./gradlew assembleRelease
git tag v1.2.0
git push origin v1.2.0
```

## Перед push

```bash
git ls-files | rg 'arrows/|openbyd-patch|ui\.xml|\.apk'   # должно быть пусто
./gradlew assembleDebug
```
