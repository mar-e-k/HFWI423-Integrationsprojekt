# Vendix Store

Der Store-Service bildet den fachlichen Filialbetrieb ab. Er ist fuer Artikel, Bestand, Bons, Checkout, Voucher und Bestandsauffuellung verantwortlich.

## Aufgabe im System

- Artikel lesen und ueber GTIN oder ID bereitstellen.
- Checkout inklusive Bon-Positionen, Drucken und Stornieren.
- Voucher pruefen und einloesen.
- Store-Bestand lesen und aktualisieren.
- Bestandsauffuellungen asynchron ueber RabbitMQ anstossen und Status bereitstellen.

## Lokal starten

Vorher muessen Docker-Infrastruktur und Orchestrator laufen:

```bash
cd ../.docker
./.scripts/docker-compose-up.sh
```

Aus dem Projekt-Root:

```bash
mvn -pl orchestrator/app -am spring-boot:run
mvn -pl store/app -am spring-boot:run
```

Die Befehle sollten in getrennten Terminals laufen. Alternativ in der IDE starten:

```text
store/app/src/main/java/de/fhdw/vendix/store/StoreApplication.java
```

Der Store verwendet standardmaessig `server.port=0`, bekommt also einen freien Port. Fuer Clients ist normalerweise nicht der direkte Port relevant, sondern der Zugriff ueber den Orchestrator/Gateway-Kontext.

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
  up -d --build orchestrator store
```

## Wichtige Abhaengigkeiten

- Postgres: Store-Daten wie Artikel, Bons, Voucher und Bestand.
- RabbitMQ: asynchrone Kommunikation fuer Bestell-/Auffuellprozesse.
- Consul: Registrierung und Auffindbarkeit.
- Keycloak: API-Security.
- Orchestrator: Gateway fuer POS und Lasttests.

## Lasttest-Relevanz

Die k6-Tests erzeugen vor allem Last auf Store-Funktionen wie Artikelscan, Checkout, Bon-Druck, Storno, Voucher und Messaging-E2E. Fuer valide Ergebnisse muss der Store gestartet und mit Seed-Daten versorgt sein.
