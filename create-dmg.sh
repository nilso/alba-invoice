#!/bin/sh

# Define variables
APP_NAME="AlbaInvoice"
MAIN_JAR="alba-invoice-1.0-SNAPSHOT.jar"
MAIN_CLASS="Main"
VERSION="1.0"

# Create the DMG package
jpackage \
  --name "$APP_NAME" \
  --input "target" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --type dmg \
  --java-options '--enable-preview' \
  --app-version "$VERSION" \
  --mac-package-name "$APP_NAME" \
  --mac-package-identifier "alba.app" \
  --mac-package-signing-prefix "alba.app" \
  --verbose