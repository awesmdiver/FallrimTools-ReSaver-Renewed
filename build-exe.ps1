#Requires -Version 5.1
<#
.SYNOPSIS
    Builds FallrimTools ReSaver (Renewed) for Windows.

.DESCRIPTION
    Compiles the project with Maven and packages it into a self-contained
    app-image (dist\ReSaver_Renewed\ReSaver_Renewed.exe) using jpackage.

    Paths are resolved in this order:
      1. Variables set in build-config.ps1 (if the file exists)
      2. Standard environment variables: JAVA_HOME, MAVEN_HOME
      3. "mvn" on your system PATH

    Copy build-config.ps1.template to build-config.ps1 to set custom paths.
    build-config.ps1 is git-ignored so your local settings are never committed.

.EXAMPLE
    .\build-exe.ps1
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Write-Host "=== FallrimTools ReSaver (Renewed) ===" -ForegroundColor Cyan
Write-Host ""

# --- Load local build configuration ------------------------------------------
$configFile = Join-Path $PSScriptRoot "build-config.ps1"
if (Test-Path $configFile) {
    . $configFile
}

# --- Resolve Maven ------------------------------------------------------------
if (-not (Get-Variable -Name MVN -Scope Local -ErrorAction SilentlyContinue)) {
    if ($env:MAVEN_HOME) {
        $MVN = Join-Path $env:MAVEN_HOME "bin\mvn.cmd"
    } else {
        $MVN = "mvn"
    }
}

# --- Resolve JDK --------------------------------------------------------------
if (-not (Get-Variable -Name JPACKAGE_JDK -Scope Local -ErrorAction SilentlyContinue)) {
    if ($env:JAVA_HOME) {
        $JPACKAGE_JDK = $env:JAVA_HOME
    } else {
        Write-Error @"
No JDK found. Either:
  1. Set the JAVA_HOME environment variable to your JDK 21+ directory, or
  2. Copy build-config.ps1.template to build-config.ps1 and set `$JPACKAGE_JDK.
"@
        exit 1
    }
}

$jpackage = Join-Path $JPACKAGE_JDK "bin\jpackage.exe"
if (-not (Test-Path $jpackage)) {
    Write-Error "jpackage not found at: $jpackage`nMake sure JPACKAGE_JDK points to a full JDK 21+, not a JRE."
    exit 1
}

Write-Host "JDK  : $JPACKAGE_JDK"
Write-Host "Maven: $MVN"
Write-Host ""

# --- Compile ------------------------------------------------------------------
Write-Host "=== Compiling with Maven ===" -ForegroundColor Cyan
& $MVN clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Error "Maven build failed"; exit 1 }

# --- Clean old dist -----------------------------------------------------------
if (Test-Path "dist") { Remove-Item "dist" -Recurse -Force }

# --- Package ------------------------------------------------------------------
Write-Host ""
Write-Host "=== Packaging with jpackage ===" -ForegroundColor Cyan

# Note: '$APPDIR/lib' uses single quotes so PowerShell does not expand $APPDIR.
# jpackage writes it verbatim into the app launcher config as a runtime token.
& $jpackage `
    --input target `
    --main-jar ReSaver_Renewed.jar `
    --main-class resaver.ReSaver_Renewed `
    --runtime-image $JPACKAGE_JDK `
    --java-options "--module-path" `
    --java-options '$APPDIR/lib' `
    --java-options "--add-modules=javafx.controls,javafx.swing,javafx.graphics,javafx.base" `
    --java-options "-Xms512m" `
    --java-options "-Xmx4g" `
    --java-options "--add-reads" `
    --java-options "javafx.graphics=ALL-UNNAMED" `
    --java-options "--add-reads" `
    --java-options "javafx.swing=ALL-UNNAMED" `
    --java-options "--enable-native-access=javafx.graphics,javafx.base,ALL-UNNAMED" `
    --name ReSaver_Renewed `
    --app-version 1.0.0 `
    --description "FallrimTools ReSaver (Renewed)" `
    --icon src\main\resources\Disk.ico `
    --win-console `
    --type app-image `
    --dest dist

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=== Build complete ===" -ForegroundColor Green
    Write-Host "Executable: dist\ReSaver_Renewed\ReSaver_Renewed.exe"
} else {
    Write-Error "jpackage failed"
    exit 1
}
