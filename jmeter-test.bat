@echo off
title JMeter Test Runner

echo =====================================
echo      JMeter Test wird gestartet
echo =====================================
echo.

REM Alten Ergebnisordner komplett loeschen
if exist jmeter-results (
    echo Loesche alten Ordner jmeter-results ...
    rmdir /s /q jmeter-results
)

echo.
echo Starte JMeter Test...
echo.

docker compose --profile test run --rm jmeter

echo.
echo =====================================
echo Test beendet
echo =====================================
echo Report:
echo jmeter-results\report\index.html
echo.

pause