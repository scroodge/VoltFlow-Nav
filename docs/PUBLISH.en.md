# Publishing to GitHub

**Language:** [Belarusian](PUBLISH.md) · **English** · [Russian](PUBLISH.ru.md)

## Create repository

```bash
gh repo create scroodge/VoltFlow-Nav --public --source=. --remote=origin --description "Yandex Navigator on BYD DiLink 3.0 HUD. Part of VoltFlow."
```

Or create **VoltFlow-Nav** on GitHub manually, then:

```bash
git remote add origin https://github.com/scroodge/VoltFlow-Nav.git
git push -u origin main
```

## Release signing (local)

1. Create keystore (once): `voltflow-nav-release.keystore` in project root.
2. Copy [`local.properties.example`](../local.properties.example) → `local.properties` and fill signing keys (file is gitignored).
3. If the password contains `#`, escape it as `\#` in `local.properties`.

## Release workflow (changelog → version → build → tag)

Same flow as [BYDMate-own](https://github.com/scroodge/BYDMate-own):

1. Generate changelog from git commits (Conventional Commits recommended):

```bash
./gradlew releaseChangelog
```

Preview without editing files:

```bash
./gradlew releaseChangelog -PdryRun=true
```

Manual version:

```bash
./gradlew releaseChangelog -PreleaseVersion=1.2.1
```

2. Review and edit [`CHANGELOG.md`](../CHANGELOG.md) if needed (canonical English file for the build).

3. Set **`versionName`** and increment **`versionCode`** in [`app/build.gradle`](../app/build.gradle) to match the latest dated section in `CHANGELOG.md` (e.g. `## [1.2.1] - 2026-06-05`).

4. Build release APK (`verifyReleaseVersion` runs automatically and fails if versions disagree):

```bash
./gradlew clean assembleRelease
# Output: app/build/outputs/apk/release/VoltFlowNav-v1.2.0.apk
```

5. Commit, tag, and push:

```bash
git add CHANGELOG.md app/build.gradle
git commit -m "chore(release): v1.2.1"
git tag v1.2.1
git push origin main v1.2.1
```

6. GitHub Actions ([release.yml](../.github/workflows/release.yml)) attaches `VoltFlowNav-v1.2.1.apk` to the Release.

The in-app updater calls `https://api.github.com/repos/scroodge/VoltFlow-Nav/releases/latest` and installs the first `.apk` asset.

CI builds stay unsigned unless you add keystore secrets to GitHub Actions.

## First release

Ensure `CHANGELOG.md` has a dated section matching `versionName`, then:

```bash
./gradlew assembleRelease
git tag v1.2.0
git push origin v1.2.0
```

## Verify before push

```bash
git ls-files | rg 'arrows/|openbyd-patch|ui\.xml|\.apk'   # should be empty
./gradlew assembleDebug
```
