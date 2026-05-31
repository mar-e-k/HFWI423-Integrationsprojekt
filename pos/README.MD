# Vendix POS

POS ist die Kassenanwendung fuer den Point of Sale. Sie stellt die Kassenoberflaeche bereit und fuehrt Kassiervorgaenge gegen die API-Vertraege des Systems aus.

## Aufgabe im System

- Auswahl und Nutzung eines Register-/Store-Kontexts.
- Warenkorb, Artikelscan und Mengenverwaltung.
- Checkout ueber den Store-Kontext.
- Bon-Druck beziehungsweise Folgeaktionen ueber API-Aufrufe.
- Keine eigene fachliche Store-Datenhaltung.

## Lokal starten

Vorher Docker-Infrastruktur, Orchestrator und Store starten:

```bash
cd ../.docker
./.scripts/docker-compose-up.sh
```

Aus dem Projekt-Root in getrennten Terminals:

```bash
mvn -pl orchestrator/app -am spring-boot:run
mvn -pl store/app -am spring-boot:run
mvn -pl pos/app -am spring-boot:run
```

Alternativ in der IDE starten:

```text
pos/app/src/main/java/de/fhdw/vendix/pos/PosApplication.java
```

POS verwendet standardmaessig `server.port=0`, bekommt also einen freien Port. Den konkreten Port sieht man im Spring-Boot-Log beim Start.

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
  up -d --build orchestrator store pos
```

## Wichtige Abhaengigkeiten

- Orchestrator: Kontextauswahl, Gateway und Routing.
- Store: fachliche Checkout-/Artikel-/Bon-Funktionen.
- Keycloak: Login und Rollen.
- Consul: Service-Kontext.

## Hinweis zu Lasttests

Die k6-Lasttests testen nicht die Vaadin-POS-Oberflaeche direkt, sondern die fachlichen API-Flows, die ein POS typischerweise ausloest. Dafuer muessen Orchestrator und Store laufen; POS selbst ist fuer die k6-API-Lasttests nicht zwingend erforderlich.
