#!/usr/bin/env bash
# Build script for Linux (and macOS).
# Produces a self-contained app-image under dist/ReSaver/.
#
# Usage: ./build.sh
#
# Requirements:
#   - JDK 21+  (any distribution — set JAVA_HOME or configure build-config.sh)
#   - Maven 3.9+ (mvn must be on PATH, or configure build-config.sh)
#   - See BUILD.md for full instructions.

set -euo pipefail

echo "=== FallrimTools ReSaver (Renewed) ==="
echo

# ── Load local build configuration ───────────────────────────────────────────
# Copy build-config.sh.template to build-config.sh to override defaults.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -f "$SCRIPT_DIR/build-config.sh" ]]; then
    # shellcheck source=/dev/null
    source "$SCRIPT_DIR/build-config.sh"
fi

# ── Resolve Maven ─────────────────────────────────────────────────────────────
if [[ -z "${MVN:-}" ]]; then
    if [[ -n "${MAVEN_HOME:-}" ]]; then
        MVN="$MAVEN_HOME/bin/mvn"
    else
        MVN="mvn"
    fi
fi

# ── Resolve JDK ──────────────────────────────────────────────────────────────
if [[ -z "${JPACKAGE_JDK:-}" ]]; then
    if [[ -n "${JAVA_HOME:-}" ]]; then
        JPACKAGE_JDK="$JAVA_HOME"
    else
        echo "ERROR: No JDK found. Either:"
        echo "  1. Set the JAVA_HOME environment variable to your JDK 21+ directory, or"
        echo "  2. Copy build-config.sh.template to build-config.sh and set JPACKAGE_JDK."
        exit 1
    fi
fi

JPACKAGE="$JPACKAGE_JDK/bin/jpackage"
if [[ ! -f "$JPACKAGE" ]]; then
    echo "ERROR: jpackage not found at: $JPACKAGE"
    echo "       Make sure JPACKAGE_JDK points to a full JDK 21+, not a JRE."
    exit 1
fi

echo "JDK  : $JPACKAGE_JDK"
echo "Maven: $MVN"
echo

# ── Compile ───────────────────────────────────────────────────────────────────
echo "=== Compiling with Maven ==="
"$MVN" clean package -DskipTests

rm -rf dist

# ── Package ───────────────────────────────────────────────────────────────────
echo
echo "=== Packaging with jpackage ==="
"$JPACKAGE" \
  --input target \
  --main-jar ReSaver.jar \
  --main-class resaver.ReSaver \
  --runtime-image "$JPACKAGE_JDK" \
  --java-options "--module-path" \
  --java-options '$APPDIR/lib' \
  --java-options "--add-modules=javafx.controls,javafx.swing,javafx.graphics,javafx.base" \
  --java-options "-Xms512m" \
  --java-options "-Xmx4g" \
  --java-options "--add-reads" \
  --java-options "javafx.graphics=ALL-UNNAMED" \
  --java-options "--add-reads" \
  --java-options "javafx.swing=ALL-UNNAMED" \
  --java-options "--enable-native-access=javafx.graphics,javafx.base,ALL-UNNAMED" \
  --name ReSaver \
  --app-version 6.0 \
  --description "FallrimTools ReSaver (Renewed)" \
  --icon src/main/resources/Disk.png \
  --type app-image \
  --dest dist

echo
echo "=== Build complete ==="
echo "Launcher: dist/ReSaver/bin/ReSaver"
