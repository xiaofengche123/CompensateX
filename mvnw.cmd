@ECHO OFF
SETLOCAL

SET BASE_DIR=%~dp0
IF "%BASE_DIR:~-1%"=="\" SET BASE_DIR=%BASE_DIR:~0,-1%
SET WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
SET WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
SET WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

IF EXIST "%WRAPPER_JAR%" GOTO run

ECHO maven-wrapper.jar not found, downloading...
FOR /F "tokens=1,* delims==" %%A IN (%WRAPPER_PROPERTIES%) DO (
  IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
)

IF "%WRAPPER_URL%"=="" (
  ECHO wrapperUrl is missing in %WRAPPER_PROPERTIES%
  EXIT /B 1
)

IF NOT EXIST "%WRAPPER_DIR%" MKDIR "%WRAPPER_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"
IF ERRORLEVEL 1 (
  ECHO Failed to download maven-wrapper.jar
  EXIT /B 1
)

:run
java -Dmaven.multiModuleProjectDirectory=%BASE_DIR% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
IF ERRORLEVEL 1 EXIT /B %ERRORLEVEL%

ENDLOCAL
