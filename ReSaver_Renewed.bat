@echo off
REM Dev launcher — runs directly from target\ without packaging.
REM Run "mvn package -DskipTests" first to populate target\.

if exist "%~dp0build-config.bat" call "%~dp0build-config.bat"

if not defined JPACKAGE_JDK (
    if defined JAVA_HOME (
        set "JPACKAGE_JDK=%JAVA_HOME%"
    ) else (
        echo ERROR: JAVA_HOME is not set.
        echo        Set JAVA_HOME or create build-config.bat from build-config.bat.template.
        exit /b 1
    )
)

"%JPACKAGE_JDK%\bin\java" ^
  --module-path target\lib ^
  --add-modules javafx.controls,javafx.swing,javafx.graphics,javafx.base ^
  --add-reads javafx.graphics=ALL-UNNAMED ^
  --add-reads javafx.swing=ALL-UNNAMED ^
  --enable-native-access=javafx.graphics,javafx.base,ALL-UNNAMED ^
  -Xms512m -Xmx4g ^
  -jar target\ReSaver_Renewed.jar %*
