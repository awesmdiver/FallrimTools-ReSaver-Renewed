@echo off
setlocal
set MVN=C:\Tools\maven\apache-maven-3.9.16\bin\mvn.cmd
set JPACKAGE=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\jpackage.exe

echo === Building ReSaver 6.0 (Java 21 + JavaFX 21) ===
call "%MVN%" clean package -DskipTests
if %errorlevel% neq 0 ( echo BUILD FAILED & exit /b 1 )

if exist dist cmd /c "rmdir /s /q dist"

echo === Packaging with jpackage ===
"%JPACKAGE%" ^
  --input target ^
  --main-jar ReSaver.jar ^
  --main-class resaver.ReSaver ^
  --runtime-image "C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot" ^
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
  --java-options "--sun-misc-unsafe-memory-access=allow" ^
  --name ReSaver ^
  --app-version 6.0 ^
  --description "FallrimTools ReSaver - Skyrim Save Editor" ^
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
