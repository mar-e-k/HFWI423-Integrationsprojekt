@echo off
docker compose down
docker compose up -d postgres pgadmin prometheus grafana
pause