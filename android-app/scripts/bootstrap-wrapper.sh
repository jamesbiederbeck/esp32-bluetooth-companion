#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.7"
WRAPPER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../gradle/wrapper" && pwd)"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"

if [[ -f "$WRAPPER_JAR" ]]; then
  echo "Gradle wrapper jar already present at $WRAPPER_JAR"
  exit 0
fi

TMP_ZIP="$(mktemp)"
trap 'rm -f "$TMP_ZIP"' EXIT

curl -L "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$TMP_ZIP"

unzip -p "$TMP_ZIP" "gradle-${GRADLE_VERSION}/lib/plugins/gradle-wrapper-${GRADLE_VERSION}.jar" > "$WRAPPER_JAR"

if [[ ! -s "$WRAPPER_JAR" ]]; then
  echo "Failed to create Gradle wrapper jar." >&2
  exit 1
fi

echo "Wrote Gradle wrapper jar to $WRAPPER_JAR"
