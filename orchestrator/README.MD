# Vendix Orchestrator

Der Orchestrator ist die zentrale Einstiegsanwendung des Systems. Er verwaltet Stores und Register, nutzt Redis/Redisson fuer Locking, Consul fuer Service-Kontext und dient als Gateway fuer Store-bezogene APIs.

## Aufgabe im System

- Verwaltung von Filialen und Kassen.
- Gateway-Routing anhand von Store-/Register-Kontext.
- Zentrale UI fuer Administration und Kontextauswahl.
- Integration mit Keycloak, Consul, Redis und Postgres.

## Lokal starten

Vorher die Docker-Infrastruktur starten:

```bash
cd ../.docker
./.scripts/docker-compose-up.sh
```

Dann aus dem Projekt-Root:

```bash
mvn -pl orchestrator/app -am spring-boot:run
```

Alternativ in der IDE die Klasse starten:

```text
orchestrator/app/src/main/java/de/fhdw/vendix/orchestrator/OrchestratorApplication.java
```

Der Orchestrator laeuft auf `http://localhost:8080`.

## Docker-Start

Aus dem Projekt-Root:

```bash
docker network create vendix-network 2>/dev/null || true
docker compose \
  --project-name vendix \
  --project-directory .docker \
  --env-file .docker/.env \
  -f .docker/app/docker-compose.yaml \
  -f .docker/local/docker-compose.yaml \
  up -d --build orchestrator
```

## Wichtige Abhaengigkeiten

- Postgres: Datenhaltung fuer Orchestrator-Daten.
- Redis: Locking fuer Store-/Register-Kontext.
- Consul: Discovery-/Routing-Kontext.
- Keycloak: Login und Rollen.

## Hinweise

Store und POS sollten erst gestartet werden, wenn der Orchestrator und die Infrastruktur laufen. Fuer Lasttests muss der Orchestrator erreichbar sein, da k6 gegen das Gateway testet.
