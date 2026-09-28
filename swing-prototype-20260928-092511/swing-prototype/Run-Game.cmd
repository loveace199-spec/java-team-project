@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo JDK 21 is required. Please check your Java installation.
  pause
  exit /b 1
)
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -sourcepath src src\prototype\App.java
if errorlevel 1 (
  echo Compilation failed. Please share the error above.
  pause
  exit /b 1
)
java -cp "bin;src" prototype.App
if errorlevel 1 pause
endlocal
