# Publishing to GitHub

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

```bash
./gradlew assembleRelease
# Signed APK: app/build/outputs/apk/release/VoltFlowNav-1.0.0-release.apk
```

CI builds stay unsigned unless you add keystore secrets to GitHub Actions.

## First release

```bash
./gradlew assembleRelease
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions ([release.yml](../.github/workflows/release.yml)) attaches `VoltFlowNav-v1.0.0.apk` to the Release.

## Verify before push

```bash
git ls-files | rg 'arrows/|openbyd-patch|ui\.xml|\.apk'   # should be empty
./gradlew assembleDebug
```
