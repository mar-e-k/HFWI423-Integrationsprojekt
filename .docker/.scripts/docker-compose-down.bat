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
REM 2. Stop all stacks
REM -------------------------
echo Stopping all stacks...
docker compose ^
  --project-name vendix ^
  --project-directory "%BASE_DIR%" ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\app\docker-compose.yaml" ^
  -f "%BASE_DIR%\monitor\docker-compose.yaml" ^
  -f "%BASE_DIR%\testing\docker-compose.yaml" ^
  down -v

echo All stacks stopped.

endlocal