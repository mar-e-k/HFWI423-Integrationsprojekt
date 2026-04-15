@echo off
setlocal enabledelayedexpansion

REM Resolve base directory (.docker)
set SCRIPT_DIR=%~dp0
for %%I in ("%SCRIPT_DIR%..") do set BASE_DIR=%%~fI

echo Starting Vendix platform...

REM -------------------------
REM 1. Validate .env
REM -------------------------
if not exist "%BASE_DIR%\.env" (
    echo ERROR: .env file missing at %BASE_DIR%\.env
    exit /b 1
)

REM -------------------------
REM 2. Ensure networks exist
REM -------------------------
echo Ensuring Docker networks exist...

docker network create app 2>nul || echo app network already exists
docker network create monitor-net 2>nul || echo monitor-net network already exists

REM -------------------------
REM 3. Start app stack
REM -------------------------
echo Starting app stack...
docker compose ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\app\docker-compose.yaml" ^
  up -d

REM -------------------------
REM 4. Start monitoring stack
REM -------------------------
echo Starting monitoring stack...
docker compose ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\monitor\docker-compose.yaml" ^
  up -d

REM -------------------------
REM 5. Start testing stack
REM -------------------------
echo Starting testing stack...
docker compose ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\testing\docker-compose.yaml" ^
  up -d

echo All stacks started.

endlocal