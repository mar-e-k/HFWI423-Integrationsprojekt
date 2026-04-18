@echo off
title JMeter Test Runner

echo =====================================
echo      Stress-Test wird gestartet
echo =====================================
echo.

REM Alten Ergebnisordner komplett loeschen
cd /d "%~dp0.."
if exist jmeter-results (
    echo Loesche echten jmeter-results Ordner...
    rmdir /s /q jmeter-results
)

echo.
echo Starte Stress-Test...
echo.

set TESTPLAN=stress-test.jmx

docker compose --profile test run --rm jmeter

echo.
echo =====================================
echo Test beendet
echo =====================================
echo Report:
echo jmeter-results\report\index.html
echo.

pause