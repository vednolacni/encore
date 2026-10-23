#!/usr/bin/env bash
# Signs an APK with the release key: APK Signature Scheme v3 only, which every Android from 9 on
# reads (minSdk is 28). Then it checks the signature.
#
#   RELEASE_KEYSTORE_PASSWORD=... bash ci/sign-release.sh app.apk release.p12
#
# The keystore holds its key under the alias "encore". The APK is signed in place.
set -euo pipefail
APK="$1"
KEYSTORE="$2"
: "${RELEASE_KEYSTORE_PASSWORD:?RELEASE_KEYSTORE_PASSWORD must hold the password of the keystore}"
HERE="$(cd "$(dirname "$0")" && pwd)"
APKSIGNER="$("$HERE/apksigner-path.sh")"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
SIGNED="$WORK/signed.apk"

"$APKSIGNER" sign \
  --ks "$KEYSTORE" --ks-key-alias encore --ks-pass env:RELEASE_KEYSTORE_PASSWORD \
  --v1-signing-enabled false --v2-signing-enabled false --v3-signing-enabled true --v4-signing-enabled false \
  --out "$SIGNED" "$APK"
"$APKSIGNER" verify --verbose --print-certs "$SIGNED"
mv "$SIGNED" "$APK"
