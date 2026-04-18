@echo off
echo ===============================
echo Starte Infrastruktur...
echo PostgreSQL / pgAdmin / Prometheus / Grafana
echo ===============================

docker compose up -d postgres pgadmin prometheus grafana

echo.
echo Fertig.
echo PostgreSQL:  localhost:5432
echo pgAdmin:     http://localhost:5050
echo Prometheus:  http://localhost:9090
echo Grafana:     http://localhost:3000
pause