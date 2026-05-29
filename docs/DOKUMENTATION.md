# Logistik-System — Technische Dokumentation

**Version:** 2.0
**Stand:** Mai 2026
**Zielgruppe:** Entwickler & Stakeholder

---

## Inhaltsverzeichnis

1. [Projektübersicht](#1-projektübersicht)
2. [Architektur](#2-architektur)
3. [Tech-Stack](#3-tech-stack)
4. [Module & Funktionen](#4-module--funktionen)
   - 4.1 [Artikelverwaltung](#41-artikelverwaltung)
   - 4.2 [Wareneingang](#42-wareneingang)
   - 4.3 [Kommissionierung](#43-kommissionierung)
   - 4.4 [Nachbestellung (Restock)](#44-nachbestellung-restock)
   - 4.5 [Lagerplatzverwaltung](#45-lagerplatzverwaltung)
   - 4.6 [Messaging & Events](#46-messaging--events)
   - 4.7 [Monitoring](#47-monitoring)
5. [Datenmodell](#5-datenmodell)
6. [REST-API](#6-rest-api)
7. [Konfiguration & Umgebungen](#7-konfiguration--umgebungen)
8. [Setup & Inbetriebnahme](#8-setup--inbetriebnahme)
9. [Geplante Aufgaben (Scheduler)](#9-geplante-aufgaben-scheduler)
10. [Performance & Qualität](#10-performance--qualität)
11. [Reflexion: Domain Driven Design](#11-reflexion-domain-driven-design)
12. [Lose Kopplung: Ist & Soll](#12-lose-kopplung-ist--soll)
13. [Mögliche Tests](#13-mögliche-tests)
14. [Quellenverzeichnis](#14-quellenverzeichnis)

---

## 1. Projektübersicht

Das **Logistik-System** ist eine webbasierte Anwendung zur Verwaltung von Lagerprozessen. Es unterstützt Lagermitarbeiter und Disponenten bei der täglichen Arbeit: vom Wareneingang über die Lagerplatzverwaltung bis hin zur Kommissionierung von Filialbestellungen.

### Kernfunktionen auf einen Blick

| Funktion | Beschreibung |
|---|---|
| Artikelverwaltung | Stammdaten, Lagerbestand, Mindestbestand, Palettenlogik |
| Wareneingang | Erfassung, Prüfung und Freigabe eingehender Lieferungen |
| Kommissionierung | Automatische und manuelle Zusammenstellung von Filialbestellungen |
| Nachbestellung | Erkennung von Unterbestand und Auslösung von Bestellungen |
| Lagerplatzverwaltung | Zonierung, Regal- und Fachstruktur im Lager |
| Messaging | Ereignisbasierte Kommunikation mit Einkauf und Filialen |
| Monitoring | Echtzeit-Metriken via Prometheus und Grafana |

### Systemgrenzen

Das System ist **eine von mehreren Anwendungen** in einem verteilten Verbund. Die Kommunikation mit dem Einkauf-System und den Filialsystemen erfolgt asynchron über einen zentralen Message-Broker (RabbitMQ). Das Logistik-System empfängt Bestellungen und Kontingente und sendet Lieferbestätigungen zurück.

```
[ Einkauf-System ]  <--AMQP-->  [ Logistik-System ]  <--AMQP-->  [ Filial-Systeme ]
                                        |
                                  [ PostgreSQL ]
```

---

## 2. Architektur

### Überblick (inkl. API Gateway)

```
[JMeter / Browser]
        |
        v
[API Gateway :8080]           <- Spring Cloud Gateway (gateway/, Spring Boot 3.4.5)
   LoggingFilter               Logging, Routing, Rate-Limiting (10 req/s)
   RateLimitingFilter
        |
        v
[Logistik-Service :8081]      <- Spring Boot 4.0.0, Vaadin 25
   Domain-Controller-Schicht   /api/artikels, /api/kommissionen, ...
   Service-Schicht             GoodsReceiptService, KommissionService, ...
   Repository-Schicht          Spring Data JPA
        |          |
        v          v
  [PostgreSQL]  [RabbitMQ]
```

Der Gateway ist der **einzige externe Einstiegspunkt**. Er zentralisiert Querschnittsbelange (Logging, Rate-Limiting) vollständig getrennt vom Business-Code.

### Schichtenmodell (Logistik-Service)

```
┌────────────────────────────────────────────┐
│           Präsentationsschicht             │
│    Vaadin Views (Server-Side Rendering)    │
│    + React-Komponenten (Vaadin Pro)        │
├────────────────────────────────────────────┤
│         REST-API / Controller-Schicht      │
│  Domain-Controller (api/<domain>/*)        │
│  OpenAPI-Dokumentation (Swagger UI)        │
├────────────────────────────────────────────┤
│             Geschäftslogik                 │
│     Services (Spring @Service Beans)       │
│     Scheduler (@Scheduled)                 │
│     Spring ApplicationEvents (Observer)    │
├────────────────────────────────────────────┤
│              Datenschicht                  │
│  Spring Data JPA Repositories              │
│  PostgreSQL 16 (via HikariCP-Pool)         │
└────────────────────────────────────────────┘
             ↕  AMQP (RabbitMQ)
┌────────────────────────────────────────────┐
│           Messaging-Schicht                │
│  EinkaufEventListener / Publisher          │
│  LogisticOrderListener / Publisher         │
└────────────────────────────────────────────┘
```

### Wichtige Architekturentscheidungen

**API Gateway als einziger Einstiegspunkt**
Der Spring Cloud Gateway (Port 8080) leitet alle Anfragen an den Logistik-Service (Port 8081) weiter. Querschnittsbelange wie Request-Logging und Rate-Limiting (10 req/s pro IP) sind ausschließlich im Gateway implementiert — vollständig getrennt vom Business-Code.

**Domain-sliced REST-Controller**
Die REST-API ist in fachliche Domänen unterteilt. Jede Domäne hat ihren eigenen Controller und sein eigenes Package (`api/<domain>/`). Änderungen an einem Endpoint betreffen keine anderen Domänen.

**Event-Driven statt direkter API-Aufrufe**
Die Kommunikation zwischen den Systemen (Einkauf, Logistik, Filialen) erfolgt über RabbitMQ. Intern werden Spring ApplicationEvents für den Observer-Pattern genutzt (z.B. `GoodsReceiptApprovedEvent`). Damit sind Komponenten entkoppelt und können unabhängig voneinander skaliert oder deployed werden.

**Transaktionsatomarität bei der Kommissionierung**
Jede Filialkommission wird in einer eigenen Datenbanktransaktion (`REQUIRES_NEW`) verarbeitet. Schlägt eine Kommission fehl, wird nur diese zurückgerollt — alle anderen laufen weiter.

**Optimistic-Locking bei Wareneingang**
Race Conditions beim gleichzeitigen Zugriff auf Restock-Orders werden durch ein atomares `UPDATE ... WHERE delivered = false` verhindert, ohne explizite Locks zu benötigen.

**Pessimistic-Locking bei Kommissionsabschluss**
Beim Abschließen einer Kommission werden die zugehörigen `MessageLogistic`-Zeilen per `SELECT FOR UPDATE` gesperrt, um parallele AMQP-Listener-Zugriffe zu blockieren, bis der Commit abgeschlossen ist.

**Palettenlogik für Lagerbestand**
Der effektive Gesamtbestand eines Artikels setzt sich zusammen aus:
- `stockLevel` (offene Einheiten im Fach)
- `reservePallets × piecesPerPallet` (geschlossene Paletten im Reservefach)

Beim Picken werden zuerst die offenen Einheiten verbraucht. Fällt der Bestand unter null, wird automatisch eine Reservepalette geöffnet.

---

## 3. Tech-Stack

### Backend (Logistik-Service)

| Technologie | Version | Zweck |
|---|---|---|
| Java | 21 (LTS) | Programmiersprache |
| Spring Boot | 4.0.0 | Anwendungsframework |
| Vaadin | 25.0.4 | Server-Side-UI-Framework |
| Spring Data JPA / Hibernate | (via Boot) | ORM / Datenzugriff |
| PostgreSQL | 16 | Relationale Datenbank |
| HikariCP | (via Boot) | Connection Pool |
| RabbitMQ (CloudAMQP) | — | Message Broker |
| Spring AMQP | (via Boot) | AMQP-Client |
| plaguv-amqp | 1.3.0 | Eigene AMQP-Event-Bibliothek |
| springdoc-openapi | 3.0.3 | OpenAPI / Swagger UI (Spring Boot 4.x kompatibel) |
| Spring Actuator + Micrometer | (via Boot) | Metriken & Health-Checks |
| Maven | 3.x | Build-Tool |
| Spotless (Eclipse-Formatter) | 2.43.0 | Code-Formatierung |

> **Hinweis springdoc:** Version 3.0.3 ist zwingend für Spring Boot 4.0 erforderlich. springdoc 2.x ist mit Spring Framework 7 (Boot 4.x) inkompatibel.

### API Gateway (gateway/)

| Technologie | Version | Zweck |
|---|---|---|
| Spring Boot | 3.4.5 | Gateway-Framework |
| Spring Cloud Gateway | 2024.0.1 | Routing, Filter |
| Spring Boot Actuator | (via Boot) | Health-Endpoint |

> **Warum Boot 3.4.5?** Spring Cloud unterstützt Spring Boot 4.0 noch nicht (Stand Mai 2026). Das Gateway läuft daher als separates Projekt auf Boot 3.4.5 — vollständig isoliert vom Logistik-Service.

### Frontend

| Technologie | Version | Zweck |
|---|---|---|
| React | 19.2.3 | UI-Komponenten-Framework |
| React Router | 7.12.0 | Client-seitiges Routing |
| Vite | 7.3.1 | Build-Tool / Dev-Server |
| TypeScript | 5.9.3 | Typsicherheit |
| Vaadin React Components | 25.0.3 | UI-Komponentenbibliothek |
| Vaadin Aura / Lumo Theme | — | Design-System |
| Line Awesome | 2.1.0 | Icon-Set |
| date-fns | — | Datums-Utilities |
| OpenLayers (ol) | — | Kartendarstellung |

### Monitoring & Infrastruktur

| Technologie | Version | Zweck |
|---|---|---|
| Prometheus | 2.53.0 | Metriken-Sammlung |
| Grafana | 11.1.0 | Metriken-Visualisierung |
| InfluxDB | 1.8 | Zeitreihendaten (JMeter) |
| JMeter | 5.6.3 | Last- und Performanztests |
| Docker / Docker Compose | — | Lokale Infrastruktur |

---

## 4. Module & Funktionen

### 4.1 Artikelverwaltung

**Service:** `ArticleInfoService`
`src/main/java/com/example/application/services/ArticleInfoService.java`
**Entität:** `ArticleInfo`
`src/main/java/com/example/application/data/articleInfo/ArticleInfo.java`
**REST-Controller:** `ArtikelController` → `/api/artikels`
`src/main/java/com/example/application/api/artikel/ArtikelController.java`
**View:** Artikelstammdaten sind in mehrere Views eingebettet (Restock, Wareneingang, Kommission)

Die Artikelverwaltung ist das zentrale Stammdaten-Modul. Jeder Artikel besitzt:

- **Artikelnummer** (max. 18 Zeichen, eindeutig)
- **Artikelname**
- **Lagerort** (Hauptfach) und **Reservelagerort**
- **Lagerbestand** (`stockLevel`) — offene Einheiten im Hauptfach
- **Mindestbestand** (`minStock`) — Schwellenwert für automatische Nachbestellung
- **Stück pro Palette** (`piecesPerPallet`) — für die Palettenlogik
- **Reservepaletten** (`reservePallets`) — Anzahl geschlossener Paletten im Reservefach

**Bestandsberechnung:**
```
Gesamtbestand = stockLevel + (piecesPerPallet × reservePallets)
```

Der Gesamtbestand (`getTotalStock()`) ist ein berechnetes Feld (`@Transient`) und wird nicht in der Datenbank gespeichert.
> Quelle: `ArticleInfo.java:132` — `getTotalStock()`

**Artikelauflösung (in Services):**
Intern wird ein Artikel bevorzugt über die `articleId` (systeminterne ID aus dem Einkauf-System) aufgelöst. Falls keine `articleId` vorliegt, erfolgt der Fallback über die `articleNumber`. Dieses Muster ist in `KommissionService.resolveArticle()` implementiert und wird systemweit konsistent angewendet.
> Quelle: `KommissionService.java:39` — `resolveArticle()`

**Neue Artikel:**
Neue, noch nicht im System vorhandene Artikel werden als `NewArticleCandidate` erfasst und im `NewArticlesView` zur Prüfung vorgelegt. Der `NewArticleCountService` liefert einen Zähler für das Badge im Navigationsmenü.
> Quellen: `services/NewArticleCandidate.java`, `services/NewArticleCountService.java`, `views/newArticleView/`

---

### 4.2 Wareneingang

**Service:** `GoodsReceiptService`
`src/main/java/com/example/application/services/GoodsReceiptService.java`
**Entitäten:** `GoodsReceipt`, `GoodsReceiptItem`
`src/main/java/com/example/application/data/goodsreceipts/GoodsReceipt.java`
`src/main/java/com/example/application/data/goodsreceipts/GoodsReceiptItem.java`
**Enums:** `GoodsReceiptStatus.java`, `GoodsReceiptItemStatus.java` (selbes Package)
**REST-Controller:** `WareneingangController` → `/api/wareneingaenge`
`src/main/java/com/example/application/api/wareneingang/WareneingangController.java`
**View:** `GoodsReceiptView`
`src/main/java/com/example/application/views/goodsreceipt/`

Der Wareneingang bildet den gesamten Prozess vom Eingang einer Lieferung bis zur Einbuchung in den Reservebestand ab.

#### Observer Pattern bei Wareneingang-Abschluss

Beim Abschluss der Prüfung (`completeInspection()`) wird ein `GoodsReceiptApprovedEvent` über den Spring `ApplicationEventPublisher` ausgelöst. Der `RestockCheckListener` reagiert darauf und kann eine Nachbestellprüfung anstoßen — ohne dass `GoodsReceiptService` weiß, wer zuhört.

```
GoodsReceiptService.completeInspection()
    └── eventPublisher.publishEvent(new GoodsReceiptApprovedEvent(this, id))
            └── RestockCheckListener.onGoodsReceiptApproved()   [@EventListener]
```

> Quellen: `events/GoodsReceiptApprovedEvent.java`, `events/RestockCheckListener.java`

#### Statusmodell

```
IN_PRUEFUNG  →  GEPRUEFT  →  FREIGEGEBEN
```

| Status | Bedeutung |
|---|---|
| `IN_PRUEFUNG` | Wareneingang angelegt, Positionen werden geprüft |
| `GEPRUEFT` | Alle Positionen entschieden (mind. eine abgelehnt) |
| `FREIGEGEBEN` | Alle Positionen freigegeben, Bestand eingebucht |

Der Gesamtstatus des Wareneingangs wird **automatisch** aus den Einzelpositionen (`GoodsReceiptItem`) abgeleitet (`recomputeReceiptStatus()`). `FREIGEGEBEN` wird ausschließlich durch den expliziten `completeInspection()`-Aufruf gesetzt — da dabei zusätzlich die Mengen in den Reservebestand übertragen werden.
> Quelle: `GoodsReceiptService.java:415` — `recomputeReceiptStatus()`

#### Positionsstatus

| Status | Bedeutung |
|---|---|
| `IN_PRUEFUNG` | Position noch nicht geprüft |
| `FREIGEGEBEN` | Menge akzeptiert |
| `ABGELEHNT` | Menge beanstandet (Defekt, Fehlmenge, ...) |

#### Nummernkreis

Wareneingangs-Nummern werden aus einer PostgreSQL-Sequence generiert:
```
WE-{JAHR}-{5-stellige Laufnummer}   →   z.B. WE-2026-00042
```
> Quelle: `GoodsReceiptService.java:48` — `nextReceiptNumber()`

#### Wareneingang aus Restock-Orders

Wareneingänge können direkt aus offenen Nachbestellungen erzeugt werden:

- **Einzelne Order:** Über die UI ausgewählte Bestellungen → `createFromRestockOrders()`
  > Quelle: `GoodsReceiptService.java:212`
- **Nächster Batch:** Automatisch die nächsten N offenen Bestellungen → `createFromNextBatch()`
  > Quelle: `GoodsReceiptService.java:139`

Dabei wird jede Restock-Order **atomar** per `UPDATE ... WHERE delivered = false` beansprucht, sodass kein paralleler Prozess dieselbe Bestellung doppelt verarbeiten kann.

**Mengeneinheit:** Wareneingangs-Positionen werden in **Paletten** geführt. Die Umrechnung von Stücken auf Paletten erfolgt über `piecesPerPallet`.

#### Abschluss der Prüfung (`completeInspection`)

1. Prüfung ob noch Positionen `IN_PRUEFUNG` sind → falls ja, Fehler
2. Freigegebene Mengen (in Paletten) werden auf `ArticleInfo.reservePallets` addiert
3. Gesamtstatus: `FREIGEGEBEN` (alle ok) oder `GEPRUEFT` (mind. eine abgelehnt)
4. `GoodsReceiptApprovedEvent` wird ausgelöst (Observer Pattern)

> Quelle: `GoodsReceiptService.java:374` — `completeInspection()`
> Reserveeinbuchung: `GoodsReceiptService.java:446` — `applyApprovedItemsToReserve()`

---

### 4.3 Kommissionierung

**Services:**
- `KommissionService` — `src/main/java/com/example/application/services/KommissionService.java`
- `WeeklyKommissionScheduler` — `src/main/java/com/example/application/services/WeeklyKommissionScheduler.java`
- `SonderkommissionSchedueler` — `src/main/java/com/example/application/services/SonderkommissionSchedueler.java`

**Entitäten:**
- `Kommission` — `src/main/java/com/example/application/data/orderPicking/Kommission.java`
- `KommissionPosition` — `src/main/java/com/example/application/data/orderPicking/KommissionPosition.java`
- `MessageLogistic` — `src/main/java/com/example/application/data/orderPicking/MessageLogistic.java`

**REST-Controller:** `KommissionController` → `/api/kommissionen`
`src/main/java/com/example/application/api/kommission/KommissionController.java`
**View:** `src/main/java/com/example/application/views/orderPickingView/`

Die Kommissionierung erstellt Picking-Aufträge für Filialen. Jede Kommission (`Kommission`) fasst alle zu liefernden Artikel für eine Filiale zusammen.

#### Ablauf

1. Filialen senden ihre Bestellwünsche als AMQP-Events → werden als `MessageLogistic`-Einträge persistiert
2. Wöchentlich (jeden Montag 10:00 Uhr) erstellt der `WeeklyKommissionScheduler` automatisch Kommissionen
3. Mitarbeiter arbeiten die Kommissionen in der UI ab und schließen sie ab
4. Beim Abschluss (`finishAtomar()`) werden die Lagerbestände abgezogen und Lieferbestätigungen per AMQP versendet

#### Transaktionskonzept

```
WeeklyKommissionScheduler.createWeeklyKommissionen()      ← Zeile 44
  └── für jede Filiale: self.processStore(storeId)        ← Zeile 53, REQUIRES_NEW
        └── SELECT FOR UPDATE (MessageLogistic)
        └── Batch-Load Artikel (findAllByArticleIdIn / findAllByArticleNumberIn)
        └── Kommission anlegen
        └── Messages markieren (processed = true)
        └── msgRepo.saveAll()

KommissionService.finishAtomar(kommission)                ← Zeile 119, REQUIRES_NEW
  └── SELECT FOR UPDATE (MessageLogistic)
  └── Batch-Load Artikel (findAllById)
  └── Stock-Updates in Memory berechnen
  └── articleRepo.saveAll()
  └── msgRepo.saveAll()
  └── kommission.setFinished(true)
  └── AMQP-Events publishen (LogisticEventPublisher)
```

> Quelle: `WeeklyKommissionScheduler.java:44` — `createWeeklyKommissionen()`
> Quelle: `WeeklyKommissionScheduler.java:53` — `processStore()`
> Quelle: `KommissionService.java:119` — `finishAtomar()`

Jede Filiale und jede Kommission läuft in ihrer eigenen Transaktion (`REQUIRES_NEW`), sodass ein Fehler bei einer Filiale die anderen nicht blockiert.

#### Lagerbestandsabzug (Palettenlogik)

```
neuerBestand = alterBestand - abzuMenge
while (neuerBestand <= 0 && reservePallets > 0):
    neuerBestand += piecesPerPallet
    reservePallets -= 1
if neuerBestand < 0: neuerBestand = 0
```
> Quelle: `KommissionService.java:158–176` — Palettenlogik innerhalb `finishAtomar()`

---

### 4.4 Nachbestellung (Restock)

**Services:**
- `RestockService` — `src/main/java/com/example/application/services/RestockService.java`
- `RestockOrderService` — `src/main/java/com/example/application/services/RestockOrderService.java`

**Entität:** `RestockOrder` — `src/main/java/com/example/application/data/restockorder/RestockOrder.java`
**REST-Controller:** `NachbestellungController` → `/api/nachbestellungen`
`src/main/java/com/example/application/api/nachbestellung/NachbestellungController.java`
**View:** `src/main/java/com/example/application/views/restockView/`

Das Restock-Modul erkennt Artikel, deren Lagerbestand unter den Mindestbestand gefallen ist, und ermöglicht die Auslösung von Nachbestellungen.

**Erkennung:** `RestockService.getArticlesToRestock()` ruft alle Artikel ab, bei denen `stockLevel < minStock`.
> Quelle: `RestockService.java:22` — `getArticlesToRestock()`
> DB-Query: `ArticleInfoRepository.findAllRequiringRestock()`

**Bestellprozess:**
1. Liste der Nachbestellkandidaten wird in der UI angezeigt
2. Disponenten können Bestellungen genehmigen (Bulk oder einzeln)
3. Genehmigte Bestellungen (`approved = true, delivered = false`) können direkt in einen Wareneingang überführt werden

**Export:** Der `RestockView` bietet eine Export-Funktion für die Nachbestellliste.

---

### 4.5 Lagerplatzverwaltung

**Service:** `StorageLocationService`
`src/main/java/com/example/application/services/StorageLocationService.java`
**Entität:** `StorageLocation`
`src/main/java/com/example/application/data/storageLocation/`
**REST-Controller:** `LagerplatzController` → `/api/lagerplaetze`
`src/main/java/com/example/application/api/lagerplatz/LagerplatzController.java`
**View:** `src/main/java/com/example/application/views/storageLocationView/`
**Dialog:** `src/main/java/com/example/application/views/components/StorageLocationPickerDialog.java`

Lagerplätze sind in einer dreistufigen Hierarchie organisiert:

```
Zone  →  Regal (Shelf)  →  Fach (Compartment)
```

Die Kombination aus Zone + Regal + Fach ist eindeutig (Unique Constraint). Ein berechnetes Feld `generalId` fasst alle drei zu einem lesbaren Bezeichner zusammen.

Lagerplätze können in der UI ausgewählt und Artikeln zugewiesen werden (über den `StorageLocationPickerDialog`).

---

### 4.6 Messaging & Events

**Konfiguration:** `RabbitConfig`
`src/main/java/com/example/application/amqp/RabbitConfig.java`
**Listener:**
- `EinkaufEventListener` — `src/main/java/com/example/application/amqp/einkaufEvents/EinkaufEventListener.java`
- `LogisticOrderListener` — `src/main/java/com/example/application/amqp/einkaufEvents/LogisticOrderListener.java`

**Publisher:**
- `EinkaufEventPublisher` — `src/main/java/com/example/application/amqp/einkaufEvents/EinkaufEventPublisher.java`
- `LogisticEventPublisher` — `src/main/java/com/example/application/amqp/einkaufEvents/LogisticEventPublisher.java`

**Interne Events (Observer Pattern):**
- `GoodsReceiptApprovedEvent` — `src/main/java/com/example/application/events/GoodsReceiptApprovedEvent.java`
- `RestockCheckListener` — `src/main/java/com/example/application/events/RestockCheckListener.java`

Das System kommuniziert über einen zentralen RabbitMQ-Exchange (`central`) mit anderen Systemen.

#### Eingehende Events

| Event | Quelle | Aktion |
|---|---|---|
| `NewQuotaEvent` | Einkauf | Neues Kontingent anlegen |
| `DeleteQuotaEvent` | Einkauf | Kontingent löschen |
| Logistische Bestellevents | Filialen | `MessageLogistic`-Einträge anlegen |

#### Ausgehende Events

| Event | Ziel | Auslöser |
|---|---|---|
| `ArticleDeliveryEvent` | Filialen | Kommission abgeschlossen |
| Einkauf-bezogene Events | Einkauf | Bestellvorgänge |

> Quelle: `LogisticEventPublisher.java` — `publishArticleDelivery()`, aufgerufen in `KommissionService.java:182`

#### Interner Observer (Spring ApplicationEvent)

Neben AMQP werden intern Spring ApplicationEvents genutzt. `GoodsReceiptService` veröffentlicht nach `completeInspection()` ein `GoodsReceiptApprovedEvent`. `RestockCheckListener` reagiert via `@EventListener` — ohne dass der Service seinen Listener kennt.

> Quellen: `events/GoodsReceiptApprovedEvent.java`, `events/RestockCheckListener.java`

#### Event-Persistenz

Alle verarbeiteten AMQP-Events werden als `MessagingEvent`-Einträge gespeichert. Die `MessagingView` zeigt dieses Protokoll in der UI an.
> Quellen: `data/messagingEvent/MessagingEvent.java`, `services/MessagingEventService.java`, `views/messaging/`

#### Hinweis zu Spring AMQP 4.0

Die `RabbitConfig` enthält einen expliziten Workaround für einen Null-Check-Bug in Spring AMQP 4.0, der die Priority-Konfiguration betrifft.
> Quelle: `amqp/RabbitConfig.java`

---

### 4.7 Monitoring

**View:** `MonitoringView`
`src/main/java/com/example/application/views/monitoringView/`
**Endpunkte:** `/actuator/health`, `/actuator/prometheus`, `/actuator/metrics`
**Konfiguration:** `src/main/resources/application.properties` (Zeilen 9–15)
**Prometheus-Konfiguration:** `monitoring/prometheus.yml`
**Grafana-Provisioning:** `monitoring/grafana/provisioning/`

Das System ist vollständig für Observability ausgerüstet:

- **Health-Check:** `/actuator/health` gibt den Zustand der Anwendung und der Datenbankverbindung zurück
- **Prometheus:** `/actuator/prometheus` liefert alle Metriken im Prometheus-Format (Scrape-Intervall: 5s)
- **Grafana:** Dashboards sind unter Port 3000 verfügbar und in die `MonitoringView` als iFrame eingebettet (anonymer Zugriff aktiviert)
- **InfluxDB:** JMeter-Lasttest-Ergebnisse werden in InfluxDB auf Port 8086 geschrieben

Badge-Notifier (`BadgeNotifier`) aktualisiert Zähler im Navigationsmenü in Echtzeit (Vaadin Push).
> Quelle: `src/main/java/com/example/application/services/BadgeNotifier.java`

---

## 5. Datenmodell

### Übersicht der Entitäten

```
ArticleInfo ──────────────────────────┐
    │ (1:n)                            │
    ├──── GoodsReceiptItem ────── GoodsReceipt
    │
    └──── RestockOrder

Kommission ──── (1:n) ──── KommissionPosition
    │
    └──── (1:n) ──── MessageLogistic

StorageLocation

MessagingEvent
Contingent
ExternalArticle
```

### Entitäten im Detail

#### `ArticleInfo` — Artikelstammdaten

> Quelle: `src/main/java/com/example/application/data/articleInfo/ArticleInfo.java`

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | Long | Interne DB-ID (PK) |
| `articleId` | Long | ID aus dem Einkauf-System |
| `articleNumber` | String (18) | Artikelnummer (eindeutig) |
| `name` | String | Artikelbezeichnung |
| `stockLevel` | Integer | Offener Bestand (Stück im Hauptfach) |
| `minStock` | Integer | Mindestbestand für Nachbestellung |
| `storageLocation` | String | Hauptlagerort |
| `reserveStorageLocation` | String | Reservelagerort |
| `piecesPerPallet` | Integer | Stück pro Palette |
| `reservePallets` | Integer | Anzahl Reservepaletten |
| `totalStock` | Integer | **Berechnet (@Transient):** stockLevel + piecesPerPallet × reservePallets — Zeile 132 |

**Indizes:** `article_number`, `article_id`, `storage_location`

---

#### `GoodsReceipt` — Wareneingang

> Quelle: `src/main/java/com/example/application/data/goodsreceipts/GoodsReceipt.java`

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | Long | PK |
| `receiptNumber` | String (32) | WE-YYYY-NNNNN (aus Sequence) |
| `supplierName` | String (200) | Lieferantenname |
| `deliveryNoteNumber` | String (64) | Lieferscheinnummer |
| `deliveryDate` | LocalDate | Lieferdatum |
| `status` | Enum | `IN_PRUEFUNG`, `GEPRUEFT`, `FREIGEGEBEN` |
| `createdAt` | LocalDateTime | Erstellungszeitpunkt (auto) |
| `updatedAt` | LocalDateTime | Letzter Update (auto via `@PreUpdate`) |

**Index:** `status`

---

#### `GoodsReceiptItem` — Wareneingangsposition

> Quelle: `src/main/java/com/example/application/data/goodsreceipts/GoodsReceiptItem.java`

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | Long | PK |
| `goodsReceipt` | GoodsReceipt | FK → Wareneingang |
| `article` | ArticleInfo | FK → Artikel |
| `expectedQuantity` | Integer | Soll-Menge (in Paletten) |
| `actualQuantity` | Integer | Ist-Menge (in Paletten) |
| `defectNotes` | String | Mängelnotizen |
| `status` | Enum | `IN_PRUEFUNG`, `FREIGEGEBEN`, `ABGELEHNT` |

---

#### `Kommission` — Kommissionsauftrag

> Quelle: `src/main/java/com/example/application/data/orderPicking/Kommission.java`

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | Long | PK |
| `orderPickingNumber` | Integer | Laufende Kommissionsnummer |
| `date` | LocalDateTime | Erstellungsdatum |
| `finished` | Boolean | Abgeschlossen? |
| `store_id` | String | Ziel-Filiale |

**Indizes:** `(finished, date)`, `(store_id, finished)`, `order_picking_nr`

---

#### `KommissionPosition` — Pickposition

> Quelle: `src/main/java/com/example/application/data/orderPicking/KommissionPosition.java`

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | Long | PK |
| `kommission` | Kommission | FK |
| `artikel_number` | String | Artikelnummer |
| `quantity` | Integer | Soll-Menge |
| `menge_picked` | Integer | Tatsächlich gepickte Menge |

---

#### `MessageLogistic` — Filialbestellnachricht

> Quelle: `src/main/java/com/example/application/data/orderPicking/MessageLogistic.java`

Eingehende Bestellanforderungen von Filialen. Werden durch den Scheduler zu Kommissionen zusammengefasst.

| Feld | Typ | Beschreibung |
|---|---|---|
| `id` | Long | PK |
| `articleId` | Long | Artikel-ID (aus Einkauf) |
| `articleNumber` | String | Artikelnummer (Fallback) |
| `quantity` | Long | Bestellmenge |
| `storeId` | String | Filial-ID |
| `processed` | Boolean | Verarbeitet? |
| `kommission` | Kommission | FK (gesetzt nach Verarbeitung) |

---

#### `StorageLocation` — Lagerplatz

> Quelle: `src/main/java/com/example/application/data/storageLocation/`

| Feld | Typ | Beschreibung |
|---|---|---|
| `storageZone` | String | Zone (z.B. "A") |
| `shelfID` | String | Regal |
| `compartmentID` | String | Fach |
| `storageStatus` | String | Status (z.B. "Available") |
| `generalId` | String | Berechneter Bezeichner (Transient) |

**Unique Constraint:** `(storageZone, shelfID, compartmentID)`

---

#### `RestockOrder` — Nachbestellung

> Quelle: `src/main/java/com/example/application/data/restockorder/RestockOrder.java`

| Feld | Typ | Beschreibung |
|---|---|---|
| `articleNumber` | String | Betroffener Artikel |
| `quantity` | Integer | Bestellmenge (Stück) |
| `approved` | Boolean | Freigegeben? |
| `delivered` | Boolean | Geliefert / in Wareneingang überführt? |

---

## 6. REST-API

> **Swagger UI:** `http://localhost:8080/swagger-ui.html` (via Gateway) oder `http://localhost:8081/swagger-ui.html` (direkt)
> **OpenAPI JSON:** `http://localhost:8081/api-docs`

Die REST-API ist in **8 Domain-Controller** gegliedert. Jede Domäne hat ihren eigenen Package und eigene Pfad-Prefix. Alle Anfragen laufen über den API-Gateway auf Port 8080.

### Übersicht aller Endpoints

#### Health — `/api/health`
> `src/main/java/com/example/application/api/health/HealthController.java`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/health` | Service-Status (`{"status":"UP","service":"logistik"}`) |

---

#### Artikel — `/api/artikels`
> `src/main/java/com/example/application/api/artikel/ArtikelController.java`

| Methode | Pfad | Parameter | Beschreibung |
|---|---|---|---|
| `GET` | `/api/artikels` | `page`, `size` | Paginierte Artikelliste |
| `GET` | `/api/artikels/filter` | `name`, `articleNumber`, `minStock`, `storageLocation` | Gefilterte Artikelsuche |
| `POST` | `/api/artikels/{id}/stock` | Body: `{delta, reason}` | Bestand ändern |
| `PUT` | `/api/artikels/{id}/storage-location` | Body: `{storageLocation}` | Lagerort ändern |
| `DELETE` | `/api/artikels/sim` | — | Alle SIM-Artikel und abhängige Daten löschen |

---

#### Wareneingänge — `/api/wareneingaenge`
> `src/main/java/com/example/application/api/wareneingang/WareneingangController.java`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/wareneingaenge` | Alle Wareneingänge |
| `GET` | `/api/wareneingaenge/{id}` | Einzelner Wareneingang |
| `GET` | `/api/wareneingaenge/pending-ids` | IDs aller Wareneingänge im Status IN_PRUEFUNG |
| `GET` | `/api/wareneingaenge/{id}/item-summaries` | Positionsübersicht |
| `GET` | `/api/wareneingaenge/open-orders` | Offene Restock-Orders |
| `POST` | `/api/wareneingaenge` | Neuen Wareneingang anlegen |
| `POST` | `/api/wareneingaenge/from-orders` | WE aus ausgewählten Bestellungen anlegen |
| `POST` | `/api/wareneingaenge/from-next-batch` | WE aus den nächsten 10 offenen Bestellungen |
| `POST` | `/api/wareneingaenge/from-next-order` | WE aus der nächsten einzelnen Bestellung |
| `POST` | `/api/wareneingaenge/{id}/items` | Position hinzufügen |
| `PUT` | `/api/wareneingaenge/{id}/items/{itemId}` | Position aktualisieren |
| `PUT` | `/api/wareneingaenge/{id}/items/{itemId}/status` | Status einer Position setzen |
| `POST` | `/api/wareneingaenge/{id}/approve-all-items` | Alle Positionen freigeben |
| `POST` | `/api/wareneingaenge/{id}/complete` | Prüfung abschließen (löst Observer-Event aus) |
| `DELETE` | `/api/wareneingaenge/{id}` | Wareneingang löschen |

---

#### Kommissionen — `/api/kommissionen`
> `src/main/java/com/example/application/api/kommission/KommissionController.java`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/kommissionen` | Alle Kommissionen |
| `POST` | `/api/kommissionen/trigger` | Wöchentliche Kommissionierung auslösen |
| `POST` | `/api/kommissionen/simulate-store-orders` | Simulierte Filialbestellungen erstellen |
| `PUT` | `/api/kommissionen/{id}/finish` | Kommission abschließen |
| `POST` | `/api/kommissionen/finish-all` | Alle offenen Kommissionen abschließen |
| `PUT` | `/api/kommissionen/{id}/items/{articleId}/quantity` | Menge einer Position setzen |

---

#### Lagerplätze — `/api/lagerplaetze`
> `src/main/java/com/example/application/api/lagerplatz/LagerplatzController.java`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/lagerplaetze` | Alle Lagerplätze |
| `POST` | `/api/lagerplaetze` | Neuen Lagerplatz anlegen |
| `DELETE` | `/api/lagerplaetze/{id}` | Lagerplatz löschen |
| `POST` | `/api/lagerplaetze/sync` | Status mit Artikeln synchronisieren |

---

#### Kontingente — `/api/kontingente`
> `src/main/java/com/example/application/api/kontingent/KontingentController.java`

| Methode | Pfad | Parameter | Beschreibung |
|---|---|---|---|
| `POST` | `/api/kontingente/simulate` | `count` (default 5000) | Simulierte Kontingente generieren |
| `DELETE` | `/api/kontingente/simulate` | — | Alle simulierten Kontingente löschen |

---

#### Nachbestellungen — `/api/nachbestellungen`
> `src/main/java/com/example/application/api/nachbestellung/NachbestellungController.java`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/nachbestellungen` | Artikel unter Mindestbestand |
| `POST` | `/api/nachbestellungen/approve-next` | Nächsten bestellbaren Artikel bestellen |
| `POST` | `/api/nachbestellungen/approve-all` | Alle Nachbestellkandidaten genehmigen |

---

#### Neue Artikel — `/api/neue-artikel`
> `src/main/java/com/example/application/api/neueartikel/NeueArtikelController.java`

| Methode | Pfad | Parameter | Beschreibung |
|---|---|---|---|
| `GET` | `/api/neue-artikel` | `lasttest` (bool) | Neue Artikel aus Kontingenten abrufen |
| `POST` | `/api/neue-artikel/create-next` | — | Nächsten neuen Artikel anlegen |
| `POST` | `/api/neue-artikel/create-next-with-storage` | — | Nächsten Artikel anlegen und Lagerplatz zuweisen |
| `GET` | `/api/neue-artikel/messaging-events` | — | Alle Messaging-Events abrufen |

---

### API Gateway — Filterverhalten

Jede Anfrage über den Gateway (Port 8080) durchläuft zwei Filter:

| Filter | Verhalten |
|---|---|
| `LoggingFilter` | Loggt `>> METHOD PATH` und `<< METHOD PATH STATUS DURATIONms` |
| `RateLimitingFilter` | Max. 10 Anfragen/Sekunde pro IP → HTTP 429 bei Überschreitung |

Der Header `X-Gateway-Source: logistik-gateway` wird allen weitergeleiteten Anfragen hinzugefügt.

---

## 7. Konfiguration & Umgebungen

> Quellen:
> - `src/main/resources/application.properties`
> - `src/main/resources/application-local.properties` (lokales Profil)
> - `src/main/resources/application-neon.properties` (Cloud-Profil)
> - `docker-compose.yml` (lokale Infrastruktur)
> - `gateway/src/main/resources/application.yml` (Gateway-Konfiguration)
> - `monitoring/docker-compose.yml` (Monitoring-Stack)

### Profile

Das System kennt zwei Spring-Profile:

| Profil | Datenbank | Verwendung |
|---|---|---|
| `local` (Standard) | Docker-PostgreSQL (localhost:5432) | Lokale Entwicklung |
| `neon` | Neon Cloud PostgreSQL | Server / Staging |

Das aktive Profil wird gesetzt über:
- **IntelliJ:** Run Configuration → Active Profiles: `local`
- **Server:** Umgebungsvariable `SPRING_PROFILES_ACTIVE=neon`

### Wichtige Konfigurationsparameter (`application.properties`)

```properties
server.port=${PORT:8081}           # Standardport 8081, via Env überschreibbar

# Datenbankpool
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.connection-timeout=60000   # 60 Sekunden

# JPA
spring.jpa.hibernate.ddl-auto=update               # Schema wird automatisch migriert
spring.jpa.properties.hibernate.jdbc.batch_size=50  # Batch-Inserts/-Updates

# Monitoring
management.endpoints.web.exposure.include=health,info,prometheus,metrics

# OpenAPI / Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=alpha
```

### Sicherheitshinweis

Die Datei `application.properties` enthält RabbitMQ-Zugangsdaten im Klartext. Für Produktivumgebungen sollten diese **zwingend** über Umgebungsvariablen oder einen Secrets-Manager (z.B. HashiCorp Vault, Kubernetes Secrets) bereitgestellt werden:

```properties
# Besser: Werte aus Umgebungsvariablen
spring.rabbitmq.username=${RABBITMQ_USER}
spring.rabbitmq.password=${RABBITMQ_PASS}
```

### Datenbankverbindung (lokal)

```
Host:      localhost
Port:      5432
Datenbank: logistik (siehe docker/init.sql)
```

### RabbitMQ

```
Host:         cow.rmq2.cloudamqp.com
Port:         5671 (SSL/TLS)
Exchange:     central
App-Name:     logistik
```

---

## 8. Setup & Inbetriebnahme

### Voraussetzungen

- Java 21 (JDK)
- Maven 3.x (oder `./mvnw` / `gateway/mvnw` nutzen)
- Docker & Docker Compose
- Node.js (wird von Vaadin automatisch verwaltet)

### Lokale Entwicklung (Standard)

**1. Infrastruktur starten (PostgreSQL via Docker):**
```bash
docker-compose up -d db
```

**2. Logistik-Service starten (Port 8081):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
Oder in IntelliJ: Run Configuration mit Active Profile `local`.

**3. API Gateway starten (Port 8080):**
```bash
cd gateway
./mvnw spring-boot:run
```

Die Anwendung ist dann erreichbar:
- **UI:** `http://localhost:8081` (direkt) oder via Gateway `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Health:** `http://localhost:8080/api/health`

**4. Monitoring-Stack starten (optional):**
```bash
cd monitoring
docker-compose up -d
```
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- InfluxDB: `http://localhost:8086`

### Gateway im Docker-Betrieb

```bash
docker-compose up -d db gateway
```

Wenn der Logistik-Service lokal läuft (nicht im Docker), muss die Gateway-Route auf `host.docker.internal` zeigen:
```bash
docker-compose up -d db
# Gateway mit Host-Routing starten:
SPRING_CLOUD_GATEWAY_ROUTES_0_URI=http://host.docker.internal:8081 docker-compose up -d gateway
```

### Production-Build

```bash
# Logistik-Service
./mvnw clean package -Pproduction
java -jar target/logistik-*.jar

# Gateway
cd gateway
./mvnw clean package
java -jar target/logistik-gateway-*.jar
```

Der Production-Build des Logistik-Service kompiliert und bündelt das Frontend (Vaadin + React) in das JAR.

### Datenbankschema

Das Schema wird bei jedem Start automatisch über `spring.jpa.hibernate.ddl-auto=update` aktualisiert. Die initiale Initialisierung für Docker erfolgt über `docker/init.sql`.

---

## 9. Geplante Aufgaben (Scheduler)

| Scheduler | Cron | Quelle | Beschreibung |
|---|---|---|---|
| `WeeklyKommissionScheduler` | `0 0 10 * * MON` | `services/WeeklyKommissionScheduler.java:43` | Montags 10:00 Uhr: Wöchentliche Kommissionen für alle Filialen erstellen |
| `SonderkommissionSchedueler` | *(konfigurierbar)* | `services/SonderkommissionSchedueler.java` | Spezielle / Ad-hoc Kommissionen |

Der `WeeklyKommissionScheduler` verarbeitet alle Filialen, für die unverarbeitete `MessageLogistic`-Einträge vorliegen. Jede Filiale wird in einer eigenen Transaktion verarbeitet.
> Quelle: `WeeklyKommissionScheduler.java:44` — `createWeeklyKommissionen()`, `WeeklyKommissionScheduler.java:53` — `processStore()`

---

## 10. Performance & Qualität

### N+1-Vermeidung

An mehreren Stellen im Code wurde gezielt auf Batch-Queries optimiert:

- **`KommissionService.finishAtomar()`:** Alle benötigten Artikel werden per `findAllById()` in einem einzigen Query geladen, statt für jede `MessageLogistic`-Zeile einzeln nachzuladen.
  > Quelle: `KommissionService.java:125–131`
- **`WeeklyKommissionScheduler.processStore()`:** Artikel werden per `findAllByArticleIdIn()` und `findAllByArticleNumberIn()` in zwei Batch-Queries geladen.
  > Quelle: `WeeklyKommissionScheduler.java:87–92`
- **`GoodsReceiptService.approveAllItemsForReceipt()`:** Bulk-Update via JPQL-Query statt N einzelner `save()`-Aufrufe.
  > Quelle: `GoodsReceiptService.java:349`

### Transaktionssicherheit

- **Race Conditions** beim Wareneingang: Atomares `UPDATE ... WHERE delivered = false` via `restockOrderRepo.markDeliveredIfOpen()`
  > Quelle: `GoodsReceiptService.java:152` und `:231`
- **Pessimistic Locking:** `SELECT FOR UPDATE` für `MessageLogistic` verhindert Doppelverarbeitung
  > Quelle: `KommissionService.java:122` — `findByKommissionIdForUpdate()`
- **Idempotenz:** `setItemStatus()` prüft zuerst, ob der Zielstatus schon gesetzt ist — unnötige DB-Writes werden vermieden
  > Quelle: `GoodsReceiptService.java:329`

### Datenbankindizes

Alle häufig abgefragten Spalten sind durch Indizes abgedeckt:
- `article_info`: `article_number`, `article_id`, `storage_location`
  > Quelle: `ArticleInfo.java:9–13`
- `kommission`: `(finished, date)`, `(store_id, finished)`, `order_picking_nr`
  > Quelle: `Kommission.java:11–15`
- `goods_receipt`: `status`
  > Quelle: `GoodsReceipt.java:9–11`

### Code-Qualität

- **Formatierung:** Spotless-Plugin mit Eclipse-Formatter (automatisch beim Build)
  > Konfiguration: `eclipse-formatter.xml`, `pom.xml` (Spotless-Plugin)
- **Batch-Größe:** Hibernate Batch Size = 50 für Inserts/Updates
  > Quelle: `application.properties` — `spring.jpa.properties.hibernate.jdbc.batch_size=50`
- **Logging:** Produktives SQL-Logging ist deaktiviert (`spring.jpa.show-sql=false`)

---

## 11. Reflexion: Domain Driven Design

Dieses Kapitel analysiert den Ist-Zustand des Systems aus der Perspektive von Domain Driven Design (DDD). Es werden keine Änderungen am Code vorgeschlagen — die Analyse ist bewusst reflektierend und soll die Entscheidungsfindung bei Weiterentwicklungen unterstützen.

---

### 11.1 Bounded Contexts

DDD teilt komplexe Systeme in klar abgegrenzte **Bounded Contexts** auf, innerhalb derer ein Modell konsistent und eine einheitliche Sprache (Ubiquitous Language) gilt.

Das Logistik-System ist selbst ein Bounded Context. Im Verbund lassen sich folgende Kontexte identifizieren:

```
┌─────────────────────┐     AMQP      ┌───────────────────────┐
│  Einkauf-Context    │ ──────────── │  Logistik-Context      │
│                     │              │  (dieses System)        │
│  NewQuotaEvent      │ ──────────── │  Kommissionierung       │
│  DeleteQuotaEvent   │              │  Wareneingang           │
│                     │              │  Artikelverwaltung      │
└─────────────────────┘              │  Lagerplatzverwaltung   │
                                     │  Nachbestellung         │
┌─────────────────────┐     AMQP      └───────────────────────┘
│  Filial-Context     │ ────────────          │
│                     │                       │ AMQP
│  Bestellwünsche     │ ◄─────────────────────┘
│  (MessageLogistic)  │   ArticleDeliveryEvent
└─────────────────────┘
```

**Bewertung:** Die Kontextgrenzen sind durch AMQP klar technisch durchgesetzt — kein direkter Datenbankzugriff über Kontextgrenzen hinweg. Das ist ein starkes DDD-Signal. Die Abhängigkeiten fließen ausschließlich über Events, was einer **Anti-Corruption Layer** entspricht: Externe Konzepte (z.B. `articleId` aus dem Einkauf-System) werden in interne Repräsentationen (`ArticleInfo`) übersetzt, statt direkt übernommen zu werden.

---

### 11.2 Ubiquitous Language

DDD fordert eine **einheitliche Fachsprache**, die sowohl im Code als auch in der Kommunikation mit Stakeholdern verwendet wird.

#### Stärken

Das System verwendet klar domänennahe Begriffe, die im Lagerkontext allgemein verständlich sind:

| Code-Begriff | Domänenbegriff | Bewertung |
|---|---|---|
| `Kommission` | Kommissionsauftrag | direkt aus der Fachsprache |
| `GoodsReceipt` | Wareneingang | Standard-Logistikbegriff |
| `RestockOrder` | Nachbestellung | klar verständlich |
| `StorageLocation` | Lagerplatz | eindeutig |
| `minStock` | Mindestbestand | direkt |

#### Inkonsistenzen

Im Code existieren sprachliche Brüche, die in einem konsequenten DDD-Modell vermieden werden sollten:

| Problem | Beispiel | Erklärung |
|---|---|---|
| Deutsch/Englisch gemischt | `menge_picked`, `store_id` vs. `stockLevel`, `articleNumber` | Unterschiedliche Konventionen innerhalb derselben Entität |
| Technischer statt fachlicher Begriff | `MessageLogistic` | Der Name beschreibt den Transportweg, nicht die Domäne — fachlich wäre „Filialbestellung" treffender |
| Tippfehler im Namen | `SonderkommissionSchedueler` | Schreibfehler (`Schedueler`) im produktiven Service-Namen |

> Quelle: `data/orderPicking/MessageLogistic.java`, `services/SonderkommissionSchedueler.java`

**Fazit:** Die Kerndomäne (Kommission, Wareneingang, Restock) ist sprachlich gut getroffen. Die Ränder des Systems — insbesondere Messaging — weichen davon ab.

---

### 11.3 Aggregate & Aggregate Roots

DDD definiert **Aggregates** als Cluster von Entitäten und Value Objects, die als Einheit behandelt werden. Der **Aggregate Root** ist der einzige erlaubte Einstiegspunkt von außen.

#### Identifizierte Aggregate

| Aggregate Root | zugehörige Entitäten | Bewertung |
|---|---|---|
| `GoodsReceipt` | `GoodsReceiptItem` | gut — der Service arbeitet ausschließlich über `GoodsReceipt`, Items werden nicht direkt von außen erzeugt |
| `Kommission` | `KommissionPosition`, `MessageLogistic` (assoziiert) | teilweise — `MessageLogistic` wird direkt über sein eigenes Repository abgefragt, ohne `Kommission` als Root zu nutzen |
| `ArticleInfo` | — | isolierte Entität, kein explizites Aggregat |
| `RestockOrder` | — | isolierte Entität |

#### Stärken

`GoodsReceipt` ist das am stärksten ausgeprägte Aggregat im System. `GoodsReceiptService` schützt die Invarianten konsequent:
- Items können nur über `addItemToReceipt()` hinzugefügt werden
- Der Gesamtstatus wird automatisch neu berechnet (`recomputeReceiptStatus()`)
- `FREIGEGEBEN` kann nur durch `completeInspection()` gesetzt werden, nie direkt

> Quelle: `GoodsReceiptService.java:284`, `:415`, `:374`

Das ist klassisches Aggregate-Root-Verhalten: die Root schützt die Konsistenz ihrer Kinder.

#### Schwächen

`MessageLogistic` hat ein eigenes Repository (`MessageLogisticRepository`) und wird direkt von `WeeklyKommissionScheduler` und `KommissionService` abgefragt — ohne den Umweg über `Kommission`. In einem strikten DDD-Modell wäre `MessageLogistic` ein Teil des `Kommission`-Aggregats und dürfte nur über diesen Root manipuliert werden.

Praktisch ist die direkte Nutzung verständlich (Performance, `SELECT FOR UPDATE`), verletzt aber die Aggregatgrenze.

> Quelle: `WeeklyKommissionScheduler.java:57`, `KommissionService.java:122`

---

### 11.4 Entitäten & Value Objects

DDD unterscheidet zwischen **Entitäten** (mit Identität) und **Value Objects** (ohne Identität, definiert durch ihre Werte).

#### Gut erkannte Entitäten

Alle Kernklassen (`ArticleInfo`, `GoodsReceipt`, `Kommission`, etc.) sind korrekt als Entitäten mit eindeutiger Datenbankidentität (`id`) modelliert.

#### Verpasste Value Objects

| Konzept | Ist-Zustand | DDD-Ideal |
|---|---|---|
| Lagerort | `storageLocation: String` in `ArticleInfo` | Value Object `StorageLocation(zone, shelf, compartment)` — unveränderlich, vergleichbar über Werte |
| Nummernkreis | `WE-YYYY-NNNNN` als String | Value Object `ReceiptNumber` mit eigener Formatierungslogik |
| Bestandsmenge | `stockLevel: Integer`, `reservePallets: Integer` getrennt | Value Object `Stock(level, reservePallets, piecesPerPallet)` mit `totalStock()` als Domänemethode |

Der auffälligste Fall ist der Lagerort: `ArticleInfo` speichert den Lagerort als einfachen String (`storageLocation`), während gleichzeitig eine vollständige `StorageLocation`-Entität mit Zone/Regal/Fach-Hierarchie existiert. Diese beiden Konzepte sind nicht verbunden — `ArticleInfo.storageLocation` enthält einen freien Text, nicht eine FK-Referenz auf `StorageLocation`. Das ist eine konzeptuelle Lücke.

> Quelle: `ArticleInfo.java:37`, `data/storageLocation/`

#### Berechnete Felder als Domänelogik

`ArticleInfo.getTotalStock()` ist ein `@Transient`-Feld und eine echte Domänenmethode — das ist ein positives DDD-Signal: die Bestandsberechnung gehört zum Artikel selbst, nicht in einen Service.

> Quelle: `ArticleInfo.java:132`

---

### 11.5 Domain Events

DDD setzt auf **Domain Events** zur Kommunikation zwischen Aggregaten und Bounded Contexts.

#### Stärken

Das System nutzt AMQP-Events konsequent für die Kommunikation zwischen den Bounded Contexts:

- **Eingehend:** `NewQuotaEvent`, `DeleteQuotaEvent` (Einkauf → Logistik)
- **Ausgehend:** `ArticleDeliveryEvent` (Logistik → Filialen)
- **Persistiert:** `MessagingEvent` als Audit-Trail aller Events

Das Muster entspricht dem DDD-Konzept von **Published Language** — einer formalen Sprache für die Kommunikation zwischen Kontexten.

Intern wird der Observer Pattern via Spring ApplicationEvents genutzt (`GoodsReceiptApprovedEvent`): Services reagieren auf Events, ohne direkt voneinander zu wissen.

> Quellen: `events/GoodsReceiptApprovedEvent.java`, `events/RestockCheckListener.java`

#### Potential

In einem vollständigen DDD-Modell würde das Abschließen einer Kommission ein internes Event auslösen (z.B. `KommissionAbgeschlossenEvent`), auf das andere Teile des Systems reagieren — statt direkter Methodenaufrufe. Das würde die Kopplung innerhalb des Kontexts weiter reduzieren.

---

### 11.6 Domain Services vs. Application Services

DDD unterscheidet:
- **Domain Services:** Enthalten fachliche Logik, die keiner einzelnen Entität gehört
- **Application Services:** Orchestrieren Use Cases, ohne selbst Fachlogik zu enthalten

#### Ist-Zustand

Die `services/`-Schicht vermischt beide Rollen — das ist in Spring-Anwendungen üblich, entspricht aber nicht dem strikten DDD-Schnitt:

| Service | Charakter | Anmerkung |
|---|---|---|
| `KommissionService.finishAtomar()` | Domain Service | reine Fachlogik: Bestandsabzug, Palettenlogik |
| `GoodsReceiptService.completeInspection()` | Domain Service | Invariantenschutz, Statusübergänge |
| `RestockService.getArticlesToRestock()` | Domain Service | fachliche Abfrage |
| `WeeklyKommissionScheduler` | Application Service | orchestriert, enthält aber auch Batch-Logik |
| `ArticleSyncService` | Application Service | technische Synchronisation |

**Fazit:** Die Services mit Kern-Domänenlogik (`KommissionService`, `GoodsReceiptService`) verhalten sich faktisch wie Domain Services und sind in dieser Funktion gut implementiert. Eine explizite Trennung in DDD-Packages (`domain/`, `application/`) ist nicht vorhanden, wäre aber bei Systemwachstum sinnvoll.

---

### 11.7 Repository-Pattern

DDD fordert, dass Repositories ausschließlich über Aggregate Roots aufgerufen werden und die Persistenztechnologie verbergen.

#### Stärken

Spring Data JPA Repositories sind korrekt je Aggregate Root angelegt und kapseln alle Queries. Die Business-Logik greift nie direkt auf JPA-Queries zu.

#### Schwächen

Wie in Abschnitt 11.3 beschrieben: `MessageLogisticRepository` ist direkt in zwei Services injiziert und wird nicht über `KommissionRepository` vermittelt. Das ist eine pragmatische, aber DDD-widrige Abkürzung.

Außerdem gibt es mehrere `@Autowired`-Injektionen (Feldinjektion) statt Konstruktorinjektion in `KommissionService`, was die Testbarkeit einschränkt.

> Quelle: `KommissionService.java:21–33` (Feldinjektion), vs. `GoodsReceiptService.java:31–38` (Konstruktorinjektion — besser)

---

### 11.8 Gesamtbewertung

| DDD-Prinzip | Umsetzung | Bewertung |
|---|---|---|
| Bounded Contexts | Klar durch AMQP-Grenzen | **gut** |
| Ubiquitous Language | Im Kern konsistent, an den Rändern gebrochen | **mittel** |
| Aggregate Roots | `GoodsReceipt` vorbildlich, `Kommission` teilweise | **mittel** |
| Value Objects | Kaum genutzt, Potential bei `StorageLocation` und `Stock` | **schwach** |
| Domain Events | Extern (AMQP) gut, intern via ApplicationEvent eingeführt | **mittel** |
| Domain vs. Application Services | Implizit getrennt, nicht explizit | **mittel** |
| Repository-Pattern | Grundsätzlich korrekt, eine Grenzüberschreitung | **gut** |

**Zusammenfassung:** Das System zeigt in seinen stärksten Bereichen — insbesondere `GoodsReceipt`/`GoodsReceiptService` und der Event-getriebenen Kontextkommunikation — ein natürliches Alignment mit DDD-Prinzipien. Die größten Abweichungen sind pragmatischer Natur und kein Anzeichen für schlechtes Design. Bei einer Weiterentwicklung wäre das Einführen von Value Objects für `Stock` und `StorageLocation` der wirkungsvollste erste Schritt in Richtung eines saubereren DDD-Modells.

---

## 12. Lose Kopplung: Ist & Soll

Dieses Kapitel bewertet das System anhand der Frage, wie stark seine Bausteine voneinander abhängen. Lose Kopplung ist kein Selbstzweck, sondern dient drei konkreten Zielen: **Änderbarkeit**, **Testbarkeit** und **Verstehbarkeit**.

---

### 12.0 Implementierte Loose-Coupling-Maßnahmen (Mai 2026)

Im Rahmen des Integrationsprojekts wurden folgende Loose-Coupling-Prinzipien konkret umgesetzt:

| Prinzip | Maßnahme | Ergebnis |
|---|---|---|
| **Abstraktion durch Schnittstellen** | OpenAPI via springdoc-openapi 3.0.3 — maschinenlesbare API-Verträge, Swagger UI | Konsumenten können unabhängig vom Backend entwickeln |
| **SOA / Microservices** | API Gateway (Spring Cloud) als einziger Einstiegspunkt | Querschnittsbelange zentralisiert, Business-Code unberührt |
| **Web Service Slicing** | 8 domain-sliced Controller statt 7 monolithischer LoadTest-Controller | Änderungen in einer Domäne betreffen keine andere |
| **Observer / Pub-Sub** | Spring ApplicationEvent (`GoodsReceiptApprovedEvent`) intern; RabbitMQ extern | Services kennen ihre Reaktionspartner nicht |
| **Dependency Injection** | Konstruktorinjektion in allen neuen Controllern und Services | Abhängigkeiten sichtbar und testbar |
| **Asynchrone Kommunikation** | RabbitMQ (bereits vorhanden) | Services müssen nicht auf Antworten warten |

---

### 12.1 Bewertungsskala

| Stufe | Bedeutung |
|---|---|
| 🟢 **lose / gut** | Bausteine kennen voneinander nur das Nötigste. Änderung an A erzwingt keine Änderung an B. |
| 🟡 **mittel** | Funktioniert, mit Reibung. Einzelne Stellen durchbrechen die Trennung, aber kein systematisches Problem. |
| 🔴 **eng** | Bausteine wissen viel voneinander. Eine Änderung pflanzt sich fort. Isoliertes Testen ist schwierig. |
| 🔴 **schwach** | Spezialfall für Abstraktion: Es fehlt der Trenn-Mechanismus (z.B. Interface) zwischen Vertrag und Implementierung. |

---

### 12.2 Bewertungsdimensionen

#### 12.2.1 Dependency Injection

**Ist 🟡** — Konstruktorinjektion dominiert (`GoodsReceiptService.java:31–41`, `ArticleSyncService.java:34–42`, alle neuen Controller). Ausreißer: `KommissionService.java:20–36` nutzt 6× Feldinjektion via `@Autowired`. Das versteckt Abhängigkeiten und erschwert Tests, weil Mocks nur über Reflection injizierbar sind.

**Soll 🟢** — Feldinjektion in `KommissionService` durch finale Konstruktorparameter ersetzen.

#### 12.2.2 Service-zu-Service-Kopplung

**Ist 🔴** — `KommissionService` hat hohen Fan-Out: 5 Repositories, `ArticleInfoService` und `LogisticEventPublisher` werden direkt aufgerufen. `LogisticOrderListener.java:77` ruft `SonderkommissionSchedueler` synchron auf — der AMQP-Listener blockiert bis die Geschäftslogik fertig ist.

**Soll 🟡** — Interne Domain Events (z.B. `KommissionAbgeschlossenEvent` über Spring `ApplicationEventPublisher`) statt direkter Service-Aufrufe. Analog zu `GoodsReceiptApprovedEvent` (bereits umgesetzt).

#### 12.2.3 Interfaces & Abstraktion

**Ist 🟡** — Keine eigenen Service-Interfaces im Projekt. Alle Services werden als konkrete Klassen injiziert. Jedoch: Die REST-API ist jetzt vollständig durch OpenAPI (Swagger UI) dokumentiert — ein maschinenlesbarer API-Vertrag trennt den API-Konsumenten von der Implementierung.

**Soll 🟢** — Interfaces für die Kern-Services (`KommissionService`, `GoodsReceiptService`, `ArticleInfoService`) mit Implementierungen als `*Impl`. Konsumenten injizieren das Interface.

#### 12.2.4 AMQP-/Event-Kopplung

**Ist 🟡** — Publisher sauber abstrahiert über die plaguv-Bibliothek. Aber: Listener enthalten Geschäftslogik. `EinkaufEventListener` ruft `deleteComissionEntrys()`, `deleteStocks()`, `freeStorageLocations()` direkt im Event-Handler auf. `LogisticOrderListener.java:87` greift direkt auf `articleRepo` und `msgRepo` zu.

**Soll 🟢** — Listener nur noch als „Übersetzer" zwischen AMQP-Event und Service-Aufruf. Keine Repository-Zugriffe im Listener.

#### 12.2.5 Controller- & View-Kopplung

**Ist 🟡** — Die neuen Domain-Controller verwenden ausschließlich Services und keine direkten Repository-Injektionen. Views umgehen die Service-Schicht weiterhin an einzelnen Stellen: `GoodsReceiptView.java:231,477` (`articleInfoRepository.findByArticleNumber(...)`, `findAll()`).

**Soll 🟢** — Repository-Aufrufe in den Views durch Service-Methoden ersetzen. Damit gilt durchgängig: View → Service → Repository.

#### 12.2.6 Repository-Kopplung

**Ist 🔴** — `ArticleInfoRepository` wird in **16 Klassen** injiziert. Es gibt keinen „Türsteher" für den Artikel.

**Soll 🟡** — Lesende Repository-Zugriffe von Views und peripheren Services über `ArticleInfoService` lenken.

#### 12.2.7 Konfiguration & Magic Strings

**Ist 🟡** — AMQP-Konfiguration nutzt Library-Konventionen. Einzelne Magic Strings in Geschäftslogik (z.B. `"UNGESETZT"` in `ArticleInfoService.java:221`), aber begrenzt.

**Soll 🟢** — Magic Strings als Konstanten oder Enums extrahieren.

#### 12.2.8 Vaadin-/UI-Kopplung

**Ist 🔴** — Vaadin-Views injizieren sowohl Services als auch Repositories. `NewArticleNotificationService` mischt UI-Notifications mit Service-Logik.

**Soll 🟡** — Repository-Zugriffe aus den Views entfernen. UI-Notification-Logik in eine UI-nahe Komponente verschieben.

---

### 12.3 Gesamtbild

| Kriterium | Ist | Soll | Aufwand |
|---|---|---|---|
| AMQP-Publisher | 🟢 | 🟢 | — |
| OpenAPI / API-Vertrag | 🟢 | 🟢 | done |
| API Gateway | 🟢 | 🟢 | done |
| Web Service Slicing | 🟢 | 🟢 | done |
| Observer / ApplicationEvent | 🟢 | 🟢 | done |
| Konfiguration / Magic Strings | 🟡 | 🟢 | sehr klein |
| Dependency Injection | 🟡 | 🟢 | klein |
| AMQP-Listener | 🟡 | 🟢 | mittel |
| Interfaces & Abstraktion | 🟡 | 🟢 | mittel |
| Controller- & View-Kopplung | 🟡 | 🟢 | mittel |
| Service-zu-Service | 🔴 | 🟡 | mittel–groß |
| Repository-Streuung | 🔴 | 🟡 | groß (verteilt) |
| Vaadin/UI | 🔴 | 🟡 | mittel |

**Ampel-Saldo:**
- Ist: 5× 🟢 · 5× 🟡 · 3× 🔴
- Soll: 9× 🟢 · 4× 🟡 · 0× 🔴

---

### 12.4 Priorisierung & Fazit

Die implementierten Maßnahmen (OpenAPI, API Gateway, domain-sliced Controller, Observer Pattern) haben fünf Kriterien auf 🟢 gehoben. Die verbleibenden roten Punkte (Service-zu-Service-Kopplung, Repository-Streuung, Vaadin-UI) sind strukturell tiefer verankert und erfordern größere Eingriffe.

Die nächsten Quick-Wins: Konstruktorinjektion in `KommissionService`, Reinigung der AMQP-Listener, Umlenkung direkter Repository-Zugriffe aus den Views.

---

## 13. Mögliche Tests

Dieses Kapitel beschreibt, wie die in Kapitel 12 dokumentierten Loose-Coupling-Maßnahmen messbar belegt werden können — statt sich auf Behauptungen zu verlassen. Vier Säulen, davon zwei ohne weiteren Aufwand sofort durchführbar.

### 13.1 Statische Kopplungsmetriken (ohne Code-Änderung)

**a) JDeps (im JDK enthalten)**

Erzeugt eine Package-Abhängigkeitsgrafik des fertigen Builds:

```bash
./mvnw -q -DskipTests package
jdeps -recursive -dotoutput target/jdeps target/logistik-1.0-SNAPSHOT.jar
```

Die erzeugten `.dot`-Dateien können mit Graphviz als PDF/PNG gerendert und als Vorher/Nachher-Diagramm in die Doku eingebettet werden.

**b) Fan-In zählen (per Grep, kein Tool nötig)**

Eine einzige Zahl macht den Effekt sichtbar:

```bash
# Wie viele Klassen kennen ArticleInfoRepository?
grep -rln "ArticleInfoRepository" src/main/java | wc -l
```

Status Mai 2026: **16 Treffer**. Ziel-Wert nach Refactoring der Views + Listener: **≤ 8**.

Die Differenz nach jeder Maßnahme dokumentieren — sie ist der Beweis.

### 13.2 ArchUnit (Architektur-Regeln als ausführbare Tests)

Architekturregeln werden als JUnit-Tests formuliert und scheitern beim nächsten Verstoß — der Build bricht. Das ist der stärkste Hebel, weil er Coupling nicht nur misst, sondern aktiv durchsetzt.

Abhängigkeit (`pom.xml`):

```xml
<dependency>
  <groupId>com.tngtech.archunit</groupId>
  <artifactId>archunit-junit5</artifactId>
  <version>1.3.0</version>
  <scope>test</scope>
</dependency>
```

Beispielregeln passend zum Projekt:

```java
@AnalyzeClasses(packages = "com.example.application")
class LooseCouplingTest {

    @ArchTest
    static final ArchRule views_duerfen_keine_repositories_nutzen =
        noClasses().that().resideInAPackage("..views..")
            .should().dependOnClassesThat().resideInAPackage("..data..");

    @ArchTest
    static final ArchRule listener_duerfen_keine_repositories_nutzen =
        noClasses().that().haveSimpleNameEndingWith("Listener")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");
}
```

Effekt: Vor dem Refactoring → roter Build mit Auflistung jeder verbotenen Abhängigkeit. Nach dem Refactoring → grüner Test als harter Beweis.

### 13.3 Vorher/Nachher-Vergleich pro Maßnahme

Pro Loose-Coupling-Maßnahme aus Kapitel 12.0 wird eine konkrete Zahl als Beleg erhoben:

| Maßnahme | Metrik | Messmethode |
|---|---|---|
| Domain-Slicing (1 → 8 Controller) | Endpoints / max. LOC pro Controller | `wc -l src/main/java/com/example/application/api/*/Controller.java` |
| Observer statt direkter Aufruf | Imports von `RestockService` in `GoodsReceiptService` (vor: 1, nach: 0) | `grep RestockService .../GoodsReceiptService.java` |
| API Gateway | Logging-/RateLimit-Code in Domain-Controllern (Ziel: 0) | `grep -rn "rateLimit\|RateLimiter" src/main/java/com/example/application/api/` |
| OpenAPI | Anzahl maschinenlesbar dokumentierter Endpoints | `curl -s http://localhost:8080/api-docs \| jq '.paths \| length'` |

### 13.4 Empirische Tests (Testbarkeit als direkte Folge loser Kopplung)

- **Anzahl `@Mock` pro Unit-Test:** Viele Mocks deuten auf hohen Fan-Out. `ArticleInfoServiceTest` hat 2 Mocks → 🟢. Ein hypothetischer `KommissionServiceTest` bräuchte aktuell ~8 Mocks → 🔴.
- **Test-Laufzeit:** Die bestehenden Unit-Tests laufen in 0,3–1,2 s (siehe `target/surefire-reports/*.txt`). Spring-Context-Tests bräuchten 5–15 s. Schnelle Tests sind ein indirekter, aber zuverlässiger Indikator loser Kopplung.
- **JaCoCo Coverage:** Nicht direkt Coupling, aber gut testbarer Code → angestrebte Coverage > 70 %.

### 13.5 Live-Demonstration (für Präsentation / Defense)

Drei Dinge, die im Vortrag vorgeführt werden können, statt nur darüber zu reden:

1. `http://localhost:8080/swagger-ui.html` öffnen → die API als formaler, maschinenlesbarer Vertrag.
2. Gateway-Log zeigt `>> GET /api/artikels` — obwohl im Controller kein Logging-Code steht. Beleg für die saubere Trennung von Querschnittsbelang und Business-Logik.
3. Wareneingang freigeben → im Log erscheint `[Observer] Wareneingang X abgeschlossen`, obwohl `GoodsReceiptService` den `RestockCheckListener` nicht kennt. Observer Pattern live.

### 13.6 Empfohlene Reihenfolge

| Schritt | Aufwand | Mehrwert |
|---|---|---|
| 1. ArchUnit-Test mit aktuell gültigen Regeln (13.2) | ~30 Min | Build-Bruch bei Regelverletzung — durchsetzbar |
| 2. Metrik-Tabelle in Kapitel 12 ergänzen (13.1, 13.3) | ~15 Min | Zahlen statt Behauptungen |
| 3. JDeps-Diagramm als PNG nach `docs/` (13.1a) | ~10 Min | Visuelle Evidenz |
| 4. Live-Demo-Skript für Defense (13.5) | ~10 Min | Wirkung im Vortrag |

---

## 14. Quellenverzeichnis

Alle referenzierten Dateien, geordnet nach Paket. Basispfad: `src/main/java/com/example/application/`

### Einstiegspunkt
| Datei | Beschreibung |
|---|---|
| `Application.java` | Spring Boot Einstiegspunkt (`@SpringBootApplication`) |

### Datenmodell (`data/`)
| Datei | Beschreibung |
|---|---|
| `AbstractEntity.java` | Basisklasse aller Entitäten (ID, Version) |
| `data/articleInfo/ArticleInfo.java` | Artikelstammdaten inkl. Palettenlogik |
| `data/articleInfo/ArticleInfoRepository.java` | JPA-Repository für Artikel |
| `data/articleInfo/RestockItem.java` | Projektionsobjekt für Nachbestellkandidaten |
| `data/goodsreceipts/GoodsReceipt.java` | Wareneingang (Kopf) |
| `data/goodsreceipts/GoodsReceiptItem.java` | Wareneingangsposition |
| `data/goodsreceipts/GoodsReceiptStatus.java` | Enum: `IN_PRUEFUNG`, `GEPRUEFT`, `FREIGEGEBEN` |
| `data/goodsreceipts/GoodsReceiptItemStatus.java` | Enum: `IN_PRUEFUNG`, `FREIGEGEBEN`, `ABGELEHNT` |
| `data/goodsreceipts/GoodsReceiptRepository.java` | JPA-Repository Wareneingang |
| `data/goodsreceipts/GoodsReceiptItemRepository.java` | JPA-Repository Positionen |
| `data/orderPicking/Kommission.java` | Kommissionsauftrag |
| `data/orderPicking/KommissionPosition.java` | Pickposition |
| `data/orderPicking/MessageLogistic.java` | Eingehende Filialbestellnachrichten |
| `data/orderPicking/KommissionRepository.java` | JPA-Repository Kommissionen |
| `data/orderPicking/KommissionPositionRepository.java` | JPA-Repository Pickpositionen |
| `data/orderPicking/MessageLogisticRepository.java` | JPA-Repository inkl. `FOR UPDATE`-Queries |
| `data/restockorder/RestockOrder.java` | Nachbestellung |
| `data/restockorder/RestockOrderRepository.java` | JPA-Repository inkl. `markDeliveredIfOpen()` |
| `data/storageLocation/` | Lagerplatz-Entität und Repository |
| `data/contingent/` | Kontingent-Entität und Repository |
| `data/messagingEvent/MessagingEvent.java` | AMQP-Event-Protokoll |
| `data/externalArticle/` | Externe Artikelreferenzen |

### Services (`services/`)
| Datei | Beschreibung |
|---|---|
| `ArticleInfoService.java` | Artikelstammdaten-Verwaltung |
| `ArticleSyncService.java` | Artikeldaten-Synchronisation |
| `GoodsReceiptService.java` | Wareneingangs-Geschäftslogik (publishes `GoodsReceiptApprovedEvent`) |
| `KommissionService.java` | Kommissions-Geschäftslogik inkl. `finishAtomar()` |
| `WeeklyKommissionScheduler.java` | Wöchentlicher Kommissions-Scheduler (Montag 10:00) |
| `SonderkommissionSchedueler.java` | Ad-hoc Kommissions-Scheduler |
| `RestockService.java` | Erkennung von Nachbestellkandidaten |
| `RestockOrderService.java` | Nachbestellungs-Verwaltung |
| `StorageLocationService.java` | Lagerplatzverwaltung |
| `MessagingEventService.java` | AMQP-Event-Protokoll |
| `BadgeNotifier.java` | Echtzeit-Badge-Zähler (Vaadin Push) |
| `NewArticleCandidate.java` | Neue Artikel (Domänenobjekt) |
| `NewArticleCountService.java` | Zähler für neue Artikel (Badge) |
| `NewArticleNotificationService.java` | Benachrichtigung bei neuen Artikeln |

### Events (`events/`)
| Datei | Beschreibung |
|---|---|
| `events/GoodsReceiptApprovedEvent.java` | Spring ApplicationEvent: Wareneingang abgeschlossen |
| `events/RestockCheckListener.java` | `@EventListener`: reagiert auf `GoodsReceiptApprovedEvent` |

### AMQP (`amqp/`)
| Datei | Beschreibung |
|---|---|
| `RabbitConfig.java` | Spring AMQP Konfiguration (inkl. AMQP-4.0-Workaround) |
| `einkaufEvents/EinkaufEventListener.java` | Listener für Einkauf-Events (`NewQuotaEvent`, `DeleteQuotaEvent`) |
| `einkaufEvents/EinkaufEventPublisher.java` | Publisher für Einkauf-Events |
| `einkaufEvents/LogisticOrderListener.java` | Listener für Filialbestellungen |
| `einkaufEvents/LogisticEventPublisher.java` | Publisher für Lieferbestätigungen (`publishArticleDelivery`) |

### REST-API (`api/`)
| Datei | Pfad-Prefix | Beschreibung |
|---|---|---|
| `api/health/HealthController.java` | `/api/health` | Service-Status |
| `api/artikel/ArtikelController.java` | `/api/artikels` | Artikel-Stammdaten und Bestandsverwaltung |
| `api/wareneingang/WareneingangController.java` | `/api/wareneingaenge` | Wareneingänge prüfen und verwalten |
| `api/kommission/KommissionController.java` | `/api/kommissionen` | Kommissionierung und Filialbestellungen |
| `api/lagerplatz/LagerplatzController.java` | `/api/lagerplaetze` | Lagerplatzverwaltung |
| `api/kontingent/KontingentController.java` | `/api/kontingente` | Kontingent-Simulation für Lasttests |
| `api/nachbestellung/NachbestellungController.java` | `/api/nachbestellungen` | Nachbestellung und Restock-Verwaltung |
| `api/neueartikel/NeueArtikelController.java` | `/api/neue-artikel` | Neue Artikel aus Kontingenten anlegen |

### Konfiguration (`config/`)
| Datei | Beschreibung |
|---|---|
| `config/OpenApiConfig.java` | OpenAPI-Metadaten (Titel, Version, Beschreibung) |

### Views (`views/`)
| Verzeichnis/Datei | Beschreibung |
|---|---|
| `MainLayout.java` | Navigationsrahmen, Drawer-Menü, Badge-Integration |
| `logisticMainView/` | Haupt-Dashboard |
| `orderPickingView/` | Kommissionierungs-UI |
| `restockView/` | Nachbestellungs-UI |
| `goodsreceipt/` | Wareneingangs-UI |
| `storageLocationView/` | Lagerplatzverwaltungs-UI |
| `newArticleView/` | Neue-Artikel-UI |
| `messaging/` | AMQP-Event-Protokoll-UI |
| `monitoringView/` | Monitoring-Dashboard (Grafana iFrame) |
| `components/StockChangeDialog.java` | Dialog: Bestandsänderung |
| `components/StorageLocationPickerDialog.java` | Dialog: Lagerplatzauswahl |

### Gateway (`gateway/`)
| Datei | Beschreibung |
|---|---|
| `gateway/pom.xml` | Spring Boot 3.4.5 + Spring Cloud 2024.0.1 |
| `gateway/src/main/resources/application.yml` | Routing-Konfiguration (Port 8080 → 8081) |
| `gateway/.../GatewayApplication.java` | Spring Boot Einstiegspunkt |
| `gateway/.../filter/LoggingFilter.java` | GlobalFilter: Request-/Response-Logging |
| `gateway/.../filter/RateLimitingFilter.java` | GlobalFilter: 10 req/s pro IP, HTTP 429 |
| `gateway/Dockerfile` | 2-Stage Build: eclipse-temurin:21-jdk → jre |

### Konfiguration & Infrastruktur
| Datei | Beschreibung |
|---|---|
| `src/main/resources/application.properties` | Hauptkonfiguration (inkl. OpenAPI-Pfade) |
| `src/main/resources/application-local.properties` | Lokales Profil (Docker-DB) |
| `src/main/resources/application-neon.properties` | Cloud-Profil (Neon-DB) |
| `pom.xml` | Maven Build, Abhängigkeiten (springdoc-openapi 3.0.3) |
| `docker-compose.yml` | PostgreSQL + Gateway (Docker) |
| `docker/init.sql` | Initiales Datenbankschema |
| `monitoring/docker-compose.yml` | Prometheus, Grafana, InfluxDB |
| `monitoring/prometheus.yml` | Prometheus Scrape-Konfiguration |
| `eclipse-formatter.xml` | Code-Formatter Regeln (Spotless) |

---

*Dokumentation erstellt auf Basis des Quellcodes (Stand: Mai 2026)*
