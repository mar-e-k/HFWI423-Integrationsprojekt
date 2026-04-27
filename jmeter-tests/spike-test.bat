@echo off
title JMeter Test Runner

echo =====================================
echo      Spike-Test wird gestartet
echo =====================================
echo.

REM Alten Ergebnisordner komplett loeschen
cd /d "%~dp0.."
if exist jmeter-results (
    echo Loesche echten jmeter-results Ordner...
    rmdir /s /q jmeter-results
)

echo.
echo Starte Spike-Test (Baseline -^> Spike -^> Recovery)...
echo.

REM === Spike-Test Profil (Defaults im JMX) ===
REM Stage 1: Baseline   ->  10 User,  10s ramp-up,  30s Dauer
REM Stage 2: Spike      -> 300 User,   5s ramp-up,  60s Dauer
REM Stage 3: Recovery   ->  10 User,   5s ramp-up,  60s Dauer
REM Gesamtlaufzeit: ca. 2.5 Minuten
REM
REM User Journey (jeder User, jede Stage, im Loop, 200ms Think Time):
REM   1) POST /api/v1/articles            (Create Article -> articleId)
REM   2) POST /api/v1/cart/add?articleId=...
REM   3) GET  /api/v1/cart
REM   4) POST /api/v1/orders/submit
set TESTPLAN=spike-test.jmx

docker compose --profile test run --rm jmeter

echo.
echo =====================================
echo Test beendet
echo =====================================
echo Report:
echo jmeter-results\report\index.html
echo.

pause