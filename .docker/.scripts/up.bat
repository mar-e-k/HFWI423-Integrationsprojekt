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
REM 2. Ensure network exists
REM -------------------------
echo Ensuring Docker network exists...

docker network create vendix-network 2>nul || echo vendix-network network already exists

REM -------------------------
REM 3. Start all stacks
REM -------------------------
echo Starting core stacks...
docker compose ^
  --project-name vendix ^
  --env-file "%BASE_DIR%\.env" ^
  -f "%BASE_DIR%\app\docker-compose.yaml" ^
  -f "%BASE_DIR%\monitor\docker-compose.yaml" ^
  -f "%BASE_DIR%\testing\docker-compose.yaml" ^
  up -d

echo Core stacks started.
echo To start the optional testing stack, run: docker compose --project-name vendix --profile testing up -d

endlocal