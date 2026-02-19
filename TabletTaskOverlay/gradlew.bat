@ECHO OFF
where gradle >NUL 2>NUL
IF %ERRORLEVEL% NEQ 0 (
  ECHO Gradle is not installed. Open this project in Android Studio to sync and generate wrapper files.
  EXIT /B 1
)
gradle %*
