#!/bin/sh

set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
KEYSTORE_PATH="$PROJECT_DIR/namaz-rings-upload.jks"
KEYCHAIN_SERVICE="one.umar.namazrings.upload"
KEYCHAIN_ACCOUNT=$(id -un)

if [ -e "$KEYSTORE_PATH" ]; then
    echo "Upload keystore already exists: $KEYSTORE_PATH"
    exit 1
fi

UPLOAD_PASSWORD=$(openssl rand -hex 32)

security add-generic-password \
    -U \
    -a "$KEYCHAIN_ACCOUNT" \
    -s "$KEYCHAIN_SERVICE" \
    -w "$UPLOAD_PASSWORD" >/dev/null

keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_PATH" \
    -storepass "$UPLOAD_PASSWORD" \
    -keypass "$UPLOAD_PASSWORD" \
    -alias upload \
    -keyalg RSA \
    -keysize 4096 \
    -validity 10000 \
    -dname "CN=Namaz Rings Upload, OU=Android, O=Umar, L=Bengaluru, ST=Karnataka, C=IN"

unset UPLOAD_PASSWORD

echo "Created $KEYSTORE_PATH"
echo "Stored its random password in macOS Keychain service: $KEYCHAIN_SERVICE"
