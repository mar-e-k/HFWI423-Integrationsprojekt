@echo off
setlocal enabledelayedexpansion

REM Resolve base directory (.docker)
set SCRIPT_DIR=%~dp0
for %%I in ("%SCRIPT_DIR%..") do set BASE_DIR=%%~fI

echo Stopping Vendix platform...

REM -------------------------
REM 1. Validate .env (non-fatal)
REM -------------------------
if not exist "%BASE_DIR%\.env" (
    echo Warning: .env file missing at %BASE_DIR%\.env
)

REM -------------------------
REM 2. Stop testing stack first
REM -------------------------
echo Stopping testing stack...
docker compose ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\testing\docker-compose.yaml" ^
  down

REM -------------------------
REM 3. Stop monitoring stack
REM -------------------------
echo Stopping monitoring stack...
docker compose ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\monitor\docker-compose.yaml" ^
  down

REM -------------------------
REM 4. Stop app stack
REM -------------------------
echo Stopping app stack...
docker compose ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\app\docker-compose.yaml" ^
  down

echo All stacks stopped.

endlocal