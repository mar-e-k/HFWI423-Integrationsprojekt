# Vendix Docker-Umgebung

Dieser Ordner enthaelt die lokale Docker-Umgebung fuer Infrastruktur, Monitoring, optionale App-Container und k6-Lasttests.

## Bestandteile

- `app/docker-compose.yaml`: Postgres, Redis, RabbitMQ, Keycloak und Consul.
- `monitor/docker-compose.yaml`: Grafana, Prometheus und Loki.
- `testing/docker-compose.yaml`: k6-Container mit Skripten und Report-Volume.
- `local/docker-compose.yaml`: optionale Container fuer Orchestrator, Store und POS.
- `.scripts`: Hilfsskripte zum Starten, Stoppen, Keycloak-Sync und k6-Testlauf.
- `.env`: lokale Ports, Benutzer und Test-Konfiguration.

## Infrastruktur starten

Aus dem Ordner `.docker`:

```bash
chmod +x .scripts/*.sh
./.scripts/docker-compose-up.sh
```

Das startet Infrastruktur, Monitoring und den k6-Container. Orchestrator, Store und POS werden damit noch nicht als App-Container gestartet. Diese koennen lokal per Maven/IDE oder mit `local/docker-compose.yaml` gestartet werden.

## Infrastruktur plus App-Container starten

Aus dem Projekt-Root:

```bash
docker network create vendix-network 2>/dev/null || true
docker compose \
  --project-name vendix \
  --project-directory .docker \
  --env-file .docker/.env \
  -f .docker/app/docker-compose.yaml \
  -f .docker/monitor/docker-compose.yaml \
  -f .docker/testing/docker-compose.yaml \
  -f .docker/local/docker-compose.yaml \
  up -d --build
```

Der Orchestrator ist danach unter `http://localhost:8080` erreichbar. Store und POS laufen im Docker-Netzwerk und werden ueber Consul/Gateway-Kontext angebunden.

## Dienste und Ports

- Postgres: `localhost:5432`
- Redis: `localhost:6379`
- RabbitMQ AMQP: `localhost:5672`
- RabbitMQ UI: `http://localhost:15672`
- Keycloak: `http://localhost:8090`
- Consul: `http://localhost:8500`
- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- k6 Dashboard: `http://localhost:5665`

## k6-Lasttests starten

Voraussetzung: Orchestrator und Store muessen erreichbar sein. Der k6-Container ruft standardmaessig `http://host.docker.internal:8080` auf.

```bash
./.scripts/manage-k6-test.sh run load-test
```

Weitere Szenarien:

```bash
./.scripts/manage-k6-test.sh run stress-test
./.scripts/manage-k6-test.sh run spike-test
./.scripts/manage-k6-test.sh run soak-test
./.scripts/manage-k6-test.sh run capacity-test
./.scripts/manage-k6-test.sh run messaging-e2e-test
```

Status und Stop:

```bash
./.scripts/manage-k6-test.sh status
./.scripts/manage-k6-test.sh stop
```

Reports liegen unter `testing/k6/reports`.

## Stoppen

```bash
./.scripts/docker-compose-down.sh
```

Wenn App-Container mit `local/docker-compose.yaml` gestartet wurden, koennen sie mit demselben Compose-Befehl und `down` beendet werden oder ueber Docker Desktop gestoppt werden.
