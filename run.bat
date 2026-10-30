@echo off
echo ============================================================
echo   Starting FindIt: Smart Lost and Found Matcher
echo ============================================================

set JAVA_HOME=C:\Users\DELL\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.11.v20260515-1531\jre
set PATH=%JAVA_HOME%\bin;C:\Users\DELL\apache-maven-3.9.6\bin;%PATH%

echo [1/2] Verifying MySQL 8.0 Daemon on port 3307...
start /B "" "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --datadir="C:\Users\DELL\findit_mysqldata" --port=3307 --bind-address=127.0.0.1 >nul 2>&1
timeout /t 2 /nobreak >nul

echo [2/2] Launching Spring Boot Full-Stack Application...
echo.
echo Application will be accessible at: http://localhost:8080/
echo Press Ctrl+C in this window to stop the application.
echo.
"%JAVA_HOME%\bin\java.exe" -jar "backend\target\findit-backend-1.0.0.jar"
pause

