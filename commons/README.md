# Vendix Commons

`commons` ist kein eigener fachlicher Service, sondern der gemeinsame Shared Kernel des Projekts. Die Module enthalten gemeinsame DTOs, API-Vertraege, Mapper-Konfiguration, CRUD-Basis, Security-, Web- und Infrastrukturbausteine.

## Aufgabe im System

- `commons/api`: DTOs, Enums und fachliche API-Modelle.
- `commons/spring/web/api`: REST-API-Interfaces als gemeinsame Vertraege.
- `commons/spring/web/core`: Gateway-/Web-Hilfen, Header und Filter.
- `commons/spring/security`: Keycloak-/Security-Hilfen.
- `commons/spring/data`: gemeinsame Persistenz-, Redis- und CRUD-Bausteine.
- `commons/spring/starter`: gemeinsame Default-Konfigurationen fuer Apps.

## Build

Nur Commons inklusive benoetigter Abhaengigkeiten bauen:

```bash
mvn -pl commons -am test
```

Gesamtes Projekt testen:

```bash
mvn clean test
```

## Hinweise zur Kopplung

`commons` sollte schlank bleiben. Fachlogik gehoert in `store`, `orchestrator` oder `pos`; in `commons` sollten nur stabile Vertraege und technische Querschnittsfunktionen liegen. Dadurch bleiben die Services deploybar und fachlich besser getrennt.
