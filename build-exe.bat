@echo off
setlocal

echo === FallrimTools ReSaver (Renewed) ===
echo.

REM ─── Load local build configuration ─────────────────────────────────────────
REM  Copy build-config.bat.template to build-config.bat to set custom paths.
if exist "%~dp0build-config.bat" call "%~dp0build-config.bat"

REM ─── Resolve Maven ───────────────────────────────────────────────────────────
REM  Uses MVN from build-config.bat, then MAVEN_HOME, then "mvn" on PATH.
if not defined MVN (
    if defined MAVEN_HOME (
        set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
    ) else (
        set "MVN=mvn"
    )
)

REM ─── Resolve JDK ─────────────────────────────────────────────────────────────
REM  Uses JPACKAGE_JDK from build-config.bat, then JAVA_HOME.
if not defined JPACKAGE_JDK (
    if defined JAVA_HOME (
        set "JPACKAGE_JDK=%JAVA_HOME%"
    ) else (
        echo ERROR: No JDK found. Set JAVA_HOME, or copy build-config.bat.template
        echo        to build-config.bat and set JPACKAGE_JDK to your JDK 21+ path.
        exit /b 1
    )
)

set "JPACKAGE=%JPACKAGE_JDK%\bin\jpackage.exe"
if not exist "%JPACKAGE%" (
    echo ERROR: jpackage not found at: %JPACKAGE%
    echo        Make sure JPACKAGE_JDK points to a full JDK 21+, not a JRE.
    exit /b 1
)

echo JDK  : %JPACKAGE_JDK%
echo Maven: %MVN%
echo.

echo === Compiling with Maven ===
call "%MVN%" clean package -DskipTests
if %errorlevel% neq 0 ( echo BUILD FAILED & exit /b 1 )

if exist dist rmdir /s /q dist

echo.
echo === Packaging with jpackage ===
"%JPACKAGE%" ^
  --input target ^
  --main-jar ReSaver.jar ^
  --main-class resaver.ReSaver ^
  --runtime-image "%JPACKAGE_JDK%" ^
  --java-options "--module-path" ^
  --java-options "$APPDIR/lib" ^
  --java-options "--add-modules=javafx.controls,javafx.swing,javafx.graphics,javafx.base" ^
  --java-options "-Xms512m" ^
  --java-options "-Xmx4g" ^
  --java-options "--add-reads" ^
  --java-options "javafx.graphics=ALL-UNNAMED" ^
  --java-options "--add-reads" ^
  --java-options "javafx.swing=ALL-UNNAMED" ^
  --java-options "--enable-native-access=javafx.graphics,javafx.base,ALL-UNNAMED" ^
  --name ReSaver ^
  --app-version 1.0.0 ^
  --description "FallrimTools ReSaver (Renewed)" ^
  --icon src\main\resources\Disk.ico ^
  --win-console ^
  --type app-image ^
  --dest dist

if %errorlevel% equ 0 (
    echo.
    echo === Build complete ===
    echo Executable: dist\ReSaver\ReSaver.exe
) else (
    echo jpackage FAILED
    exit /b 1
)
endlocal
