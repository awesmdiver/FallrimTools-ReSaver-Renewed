@echo off
REM Dev launcher — runs directly from target\ without packaging
"C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\java" ^
  --module-path target\lib ^
  --add-modules javafx.controls,javafx.swing,javafx.graphics,javafx.base ^
  --add-reads javafx.graphics=ALL-UNNAMED ^
  --add-reads javafx.swing=ALL-UNNAMED ^
  --enable-native-access=javafx.graphics,javafx.base ^
  -Xms512m -Xmx4g ^
  -jar target\ReSaver.jar %*
