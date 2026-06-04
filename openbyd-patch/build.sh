#!/bin/bash
# Full pipeline: decode pristine OpenBYD -> apply Yandex patches -> build/align/sign/verify.
# The decoded/ tree is generated (git-ignored); only original/, patches/ and the scripts are tracked.
#
#   ./build.sh            decode fresh + patch + build   (default, reproducible)
#   ./build.sh --keep     skip re-decode if decoded/ exists, just re-patch + build
#
# Requires: apktool, Android SDK build-tools (zipalign, apksigner), a JDK (keytool), python3.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ORIG="$HERE/original/OpenBYD-2.2-orig.apk"
DECODED="$HERE/decoded"
DIST="$HERE/dist"
KS="$HERE/yandexbyd.keystore"
OUT="$DIST/OpenBYD-2.2-yandex.apk"
BT="$(ls -d "$HOME"/Library/Android/sdk/build-tools/* | sort | tail -1)"
KEYTOOL="$(/usr/libexec/java_home 2>/dev/null)/bin/keytool"
[ -x "$KEYTOOL" ] || KEYTOOL=keytool

[ -f "$ORIG" ] || { echo "Missing pristine input: $ORIG"; exit 1; }
mkdir -p "$DIST"

# 1. decode (fresh unless --keep and a tree already exists)
if [ "${1:-}" != "--keep" ] || [ ! -d "$DECODED" ]; then
  echo "==> decoding $ORIG"
  apktool d -f -o "$DECODED" "$ORIG" >/dev/null
fi

# 2. apply patches (idempotent, asserts each edit)
echo "==> applying patches"
python3 "$HERE/apply_patches.py"

# 3. keystore (throwaway, auto-created once)
if [ ! -f "$KS" ]; then
  "$KEYTOOL" -genkeypair -keystore "$KS" -alias yb -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass android -keypass android \
    -dname "CN=YandexBYD, OU=Mod, O=Personal, L=NA, S=NA, C=NA"
fi

# 4. build -> align -> sign -> verify
echo "==> building"
apktool b "$DECODED" -o "$DIST/_unsigned.apk" >/dev/null
"$BT/zipalign" -f -p 4 "$DIST/_unsigned.apk" "$DIST/_aligned.apk"
"$BT/apksigner" sign --ks "$KS" --ks-pass pass:android --key-pass pass:android \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out "$OUT" "$DIST/_aligned.apk"
rm -f "$DIST/_unsigned.apk" "$DIST/_aligned.apk"
"$BT/apksigner" verify "$OUT" >/dev/null && echo "Built + verified: $OUT"
