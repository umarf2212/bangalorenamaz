#!/bin/sh

set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
KEYSTORE_PATH="$PROJECT_DIR/namaz-rings-upload.jks"
KEYCHAIN_SERVICE="one.umar.namazrings.upload"
KEYCHAIN_ACCOUNT=$(id -un)

if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "Missing upload keystore. Run scripts/create-upload-key.sh first."
    exit 1
fi

UPLOAD_PASSWORD=$(security find-generic-password \
    -a "$KEYCHAIN_ACCOUNT" \
    -s "$KEYCHAIN_SERVICE" \
    -w)

ANDROID_HOME=${ANDROID_HOME:-/Users/umarfarooque/Library/Android/sdk} \
NAMAZ_UPLOAD_KEYSTORE="$KEYSTORE_PATH" \
NAMAZ_UPLOAD_PASSWORD="$UPLOAD_PASSWORD" \
NAMAZ_UPLOAD_ALIAS="upload" \
    "$PROJECT_DIR/gradlew" -p "$PROJECT_DIR" testDebugUnitTest bundleRelease

unset UPLOAD_PASSWORD

jarsigner -verify -verbose -certs \
    "$PROJECT_DIR/app/build/outputs/bundle/release/app-release.aab" >/dev/null

echo "Signed bundle ready: $PROJECT_DIR/app/build/outputs/bundle/release/app-release.aab"
