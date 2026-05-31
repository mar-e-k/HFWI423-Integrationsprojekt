# Vendix - Filial-, Kassen- und Orchestrierungssystem

Vendix ist ein verteiltes Spring-Boot-System für Filialbetrieb und Kassiervorgänge. Das Projekt besteht aus getrennt startbaren Anwendungen für Orchestrierung, Store und POS sowie gemeinsamen Vertrags- und Infrastrukturmodulen.

## Systemueberblick

- `orchestrator`: zentrale Einstiegsanwendung und Gateway. Verwaltet Filialen und Kassen, nutzt Consul/Redis fuer Service- und Locking-Kontext und routet Store-APIs ueber Header wie `X-Store-ID`.
- `store`: fachlicher Store-Service fuer Artikel, Bestand, Bons, Checkout, Voucher und asynchrone Bestandsauffuellung ueber RabbitMQ.
- `pos`: Point-of-Sale-Oberflaeche fuer Kassiervorgaenge. Die Kasse arbeitet gegen die API-Vertraege und soll fachlich nicht direkt auf Store-Datenhaltung zugreifen.
- `commons`: Shared Kernel fuer DTOs, API-Interfaces, Mapper-/CRUD-Basis, Security-, Web- und Infrastrukturbausteine.
- `.docker`: lokale Infrastruktur, Monitoring und k6-Lasttests.

## Voraussetzungen

- Java 25
- Maven 3.9+ oder die Maven-Integration der IDE
- Docker Desktop oder kompatible Docker-Engine mit Docker Compose
- Freie Ports: `3000`, `5432`, `5672`, `6379`, `8080`, `8090`, `8500`, `9090`, `15672`, `5665`

## Schnellstart mit Docker-Infrastruktur und lokalen Apps

1. Infrastruktur starten:

```bash
cd .docker
chmod +x .scripts/*.sh
./.scripts/docker-compose-up.sh
```

2. Orchestrator starten:

```bash
mvn -pl orchestrator/app -am spring-boot:run
```

3. Store starten:

```bash
mvn -pl store/app -am spring-boot:run
```

4. POS starten:

```bash
mvn -pl pos/app -am spring-boot:run
```

Die Anwendungen verwenden standardmaessig das Profil `local`. Dafuer muessen Postgres, Redis, RabbitMQ, Keycloak und Consul laufen. Der Orchestrator ist unter `http://localhost:8080` erreichbar. Store und POS laufen standardmaessig auf zufaelligen Ports (`server.port=0`) und werden ueber Service Discovery beziehungsweise Gateway-Kontext verwendet.

## Vollstaendiger Docker-Start inklusive Apps

Wenn auch Orchestrator, Store und POS als Container gebaut werden sollen:

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

Der Orchestrator wird auf `http://localhost:8080` gemappt. Store und POS laufen im Docker-Netzwerk und registrieren sich dort.

## Wichtige Oberflaechen

- Orchestrator: `http://localhost:8080`
- Keycloak: `http://localhost:8090`
- Consul UI: `http://localhost:8500`
- RabbitMQ Management: `http://localhost:15672` mit `admin/admin`
- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- k6 Web Dashboard: `http://localhost:5665`

## Build und Tests

Gesamtes Projekt bauen und testen:

```bash
mvn clean test
```

Ein einzelnes App-Modul bauen:

```bash
mvn -pl store/app -am clean package
```

## Lasttests mit k6

1. Infrastruktur und mindestens Orchestrator + Store starten.
2. k6-Szenario ausfuehren:

```bash
cd .docker
./.scripts/manage-k6-test.sh run load-test
```

Verfuegbare Szenarien:

- `load-test`
- `stress-test`
- `spike-test`
- `soak-test`
- `capacity-test`
- `messaging-e2e-test`

Die k6-Skripte laufen gegen den Orchestrator (`ORCHESTRATOR_URL`, Standard: `http://host.docker.internal:8080`). Der Orchestrator routet die Store-Requests anhand des Store-Kontexts weiter. Reports werden unter `.docker/testing/k6/reports` abgelegt.

## Stoppen

```bash
cd .docker
./.scripts/manage-k6-test.sh stop
./.scripts/docker-compose-down.sh
```

Falls die Apps lokal per Maven gestartet wurden, muessen die jeweiligen Prozesse im Terminal oder in der IDE beendet werden.
