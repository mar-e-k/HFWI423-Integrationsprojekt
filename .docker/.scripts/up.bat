@echo off
setlocal enabledelayedexpansion

REM Resolve base directory (.docker)
set SCRIPT_DIR=%~dp0
for %%I in ("%SCRIPT_DIR%..") do set BASE_DIR=%%~fI
set APP_PROFILE_ARGS=
set APP_UP_ARGS=up -d

:parse_args
if "%~1"=="" goto args_done
if "%~1"=="--apps" (
    set APP_PROFILE_ARGS=!APP_PROFILE_ARGS! --profile apps
    set APP_UP_ARGS=up -d --build
    set K6_ORCHESTRATOR_URL=http://orchestrator:8080
    set K6_STORE_URL=http://store:8081
    shift
    goto parse_args
)
if "%~1"=="--auth" (
    set APP_PROFILE_ARGS=!APP_PROFILE_ARGS! --profile auth
    shift
    goto parse_args
)
echo ERROR: Unknown option "%~1". Supported: --apps, --auth
exit /b 1
:args_done

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
  %APP_PROFILE_ARGS% ^
  %APP_UP_ARGS%

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
