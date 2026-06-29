#!/usr/bin/env bash
# Dev launcher for Linux/macOS — runs directly from target/ without packaging.
# Run "mvn package -DskipTests" first to populate target/.
#
# Usage: ./ReSaver_Renewed.sh [options] [save-file]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -f "$SCRIPT_DIR/build-config.sh" ]]; then
    # shellcheck source=/dev/null
    source "$SCRIPT_DIR/build-config.sh"
fi

if [[ -z "${JPACKAGE_JDK:-}" ]]; then
    if [[ -n "${JAVA_HOME:-}" ]]; then
        JPACKAGE_JDK="$JAVA_HOME"
    else
        echo "ERROR: JAVA_HOME is not set."
        echo "       Set JAVA_HOME or create build-config.sh from build-config.sh.template."
        exit 1
    fi
fi

exec "$JPACKAGE_JDK/bin/java" \
  --module-path "$SCRIPT_DIR/target/lib" \
  --add-modules javafx.controls,javafx.swing,javafx.graphics,javafx.base \
  --add-reads javafx.graphics=ALL-UNNAMED \
  --add-reads javafx.swing=ALL-UNNAMED \
  --enable-native-access=javafx.graphics,javafx.base,ALL-UNNAMED \
  -Xms512m -Xmx4g \
  -jar "$SCRIPT_DIR/target/ReSaver_Renewed.jar" "$@"
