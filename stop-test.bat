@echo off
title JMeter Test stoppen

echo ==========================================
echo     Stoppe laufende JMeter Container
echo ==========================================
echo.

for /f "tokens=*" %%i in ('docker ps -a --filter "ancestor=justb4/jmeter:latest" --format "{{.ID}}"') do (
    echo Stoppe Container %%i ...
    docker rm -f %%i >nul 2>&1
)

echo.
echo Alle JMeter Container wurden beendet und entfernt.
echo.

pause