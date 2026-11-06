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
javac -encoding UTF-8 -d bin -sourcepath src src\game\App.java
if errorlevel 1 (
  echo Compilation failed. Please share the error above.
  pause
  exit /b 1
)
rem 추가: lib 폴더의 JDBC 드라이버를 실행 시 함께 읽습니다.
java -cp "bin;src;lib/*" game.App
if errorlevel 1 pause
endlocal
