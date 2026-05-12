# Logistik-System — Technische Dokumentation

**Version:** 1.0
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
12. [Quellenverzeichnis](#12-quellenverzeichnis)

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

### Schichtenmodell

Die Anwendung folgt einem klassischen Drei-Schichten-Modell:

```
┌────────────────────────────────────────────┐
│           Präsentationsschicht             │
│    Vaadin Views (Server-Side Rendering)    │
│    + React-Komponenten (Vaadin Pro)        │
├────────────────────────────────────────────┤
│             Geschäftslogik                 │
│     Services (Spring @Service Beans)       │
│     Scheduler (@Scheduled)                 │
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

**Event-Driven statt direkter API-Aufrufe**
Die Kommunikation zwischen den Systemen (Einkauf, Logistik, Filialen) erfolgt über RabbitMQ. Damit sind die Systeme entkoppelt und können unabhängig voneinander skaliert oder deployed werden.

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

### Backend

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
| plaguv-amqp | — | Eigene AMQP-Event-Bibliothek |
| Spring Actuator + Micrometer | (via Boot) | Metriken & Health-Checks |
| Maven | 3.x | Build-Tool |
| Spotless (Eclipse-Formatter) | — | Code-Formatierung |

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
**View:** `GoodsReceiptView`
`src/main/java/com/example/application/views/goodsreceipt/`

Der Wareneingang bildet den gesamten Prozess vom Eingang einer Lieferung bis zur Einbuchung in den Reservebestand ab.

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

> Quellen: `src/main/java/com/example/application/api/load/`
> - `LoadTestHealthController.java`
> - `LoadTestArticleController.java`
> - `LoadTestGoodsReceiptController.java`
> - `LoadTestKommissionController.java`
> - `LoadTestContingentController.java`
> - `LoadTestStorageController.java`
> - `LoadTestMiscController.java`

Die REST-API ist unter dem Pfad `/api/load/` erreichbar. Sie dient primär der **Lasttest-Integration** mit JMeter und kann auch für Systemintegrationstests genutzt werden.

> **Hinweis:** Die API ist nicht für den produktiven Endbetrieb als externe Schnittstelle vorgesehen. Sie bietet keine Authentifizierung.

### Health

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/load/health` | Einfacher Verfügbarkeitscheck |

### Artikel

| Methode | Pfad | Parameter | Beschreibung |
|---|---|---|---|
| `GET` | `/api/load/articles` | `page`, `size` | Paginierte Artikelliste |
| `GET` | `/api/load/articles/filter` | Filter-Parameter | Gefilterte Artikelsuche |
| `POST` | `/api/load/articles/{id}/stock` | Body: `{change}` | Bestand ändern |
| `PUT` | `/api/load/articles/{id}/storage-location` | Body: Location | Lagerort ändern |
| `DELETE` | `/api/load/articles/sim` | — | Simulationsdaten löschen |

### Wareneingang

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/load/goods-receipts` | Alle Wareneingänge |
| `GET` | `/api/load/goods-receipts/{id}` | Einzelner Wareneingang |
| `POST` | `/api/load/goods-receipts` | Wareneingang anlegen |
| `POST` | `/api/load/goods-receipts/{id}/items` | Position hinzufügen |
| `GET` | `/api/load/goods-receipts/{id}/item-summaries` | Positionsübersicht |
| `GET` | `/api/load/goods-receipts/pending-ids` | IDs offener Wareneingänge |
| `GET` | `/api/load/goods-receipts/open-orders` | Offene Restock-Orders |
| `POST` | `/api/load/goods-receipts/from-orders` | WE aus Bestellungen anlegen |
| `POST` | `/api/load/goods-receipts/from-next-batch` | WE aus nächstem Batch |
| `POST` | `/api/load/goods-receipts/from-next-order` | WE aus nächster Order |

### Kommission

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/load/kommission/simulate` | Kommission simulieren |
| `DELETE` | `/api/load/kommission/simulate` | Simulation zurücksetzen |

### Kontingent

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/load/contingent/simulate` | Kontingent simulieren |
| `DELETE` | `/api/load/contingent/simulate` | Simulation zurücksetzen |

### Lagerplatz

| Methode | Pfad | Beschreibung |
|---|---|---|
| *(siehe Controller)* | `/api/load/storage/...` | Lagerplatz-Operationen |

---

## 7. Konfiguration & Umgebungen

> Quellen:
> - `src/main/resources/application.properties`
> - `src/main/resources/application-local.properties` (lokales Profil)
> - `src/main/resources/application-neon.properties` (Cloud-Profil)
> - `docker-compose.yml` (lokale Infrastruktur)
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
- Maven 3.x (oder `./mvnw` nutzen)
- Docker & Docker Compose
- Node.js (wird von Vaadin automatisch verwaltet)

### Lokale Entwicklung

**1. Infrastruktur starten (PostgreSQL via Docker):**
```bash
docker-compose up -d
```

**2. Anwendung starten (Entwicklungsmodus):**
```bash
./mvnw spring-boot:run
```
Die Anwendung ist dann unter `http://localhost:8081` erreichbar. Vaadin öffnet den Browser automatisch.

**3. Monitoring-Stack starten (optional):**
```bash
cd monitoring
docker-compose up -d
```
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- InfluxDB: `http://localhost:8086`

### Production-Build

```bash
./mvnw clean package -Pproduction
java -jar target/logistik-*.jar
```

Der Production-Build kompiliert und bündelt das Frontend (Vaadin + React) in das JAR.

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
| Irreführendes Prefix | `LoadTest*` bei den Controllern | Klingt nach Testinfrastruktur, ist aber produktiver API-Code |
| Tippfehler im Namen | `SonderkommissionSchedueler` | Schreibfehler (`Schedueler`) im produktiven Service-Namen |

> Quelle: `data/orderPicking/MessageLogistic.java`, `services/SonderkommissionSchedueler.java`, `api/load/LoadTest*.java`

**Fazit:** Die Kerndomäne (Kommission, Wareneingang, Restock) ist sprachlich gut getroffen. Die Ränder des Systems — insbesondere Messaging und API — weichen davon ab.

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

#### Schwächen

Innerhalb des Logistik-Kontexts werden keine internen Domain Events genutzt. Stattdessen rufen Services direkt andere Services auf:

```
// KommissionService.finishAtomar() ruft direkt auf:
logisticEventPublisher.publishArticleDelivery(storeId, articleId, qty)   // AMQP → OK
articleRepo.saveAll(articles)                                             // direkt  → OK
```

In einem konsequenten DDD-Modell würde das Abschließen einer Kommission ein internes Event auslösen (z.B. `KommissionAbgeschlossenEvent`), auf das andere Teile des Systems reagieren — statt direkter Methodenaufrufe. Das würde die Kopplung innerhalb des Kontexts weiter reduzieren.

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
| Domain Events | Extern (AMQP) gut, intern nicht vorhanden | **mittel** |
| Domain vs. Application Services | Implizit getrennt, nicht explizit | **mittel** |
| Repository-Pattern | Grundsätzlich korrekt, eine Grenzüberschreitung | **gut** |

**Zusammenfassung:** Das System ist kein explizit DDD-entwickeltes System, zeigt aber in seinen stärksten Bereichen — insbesondere `GoodsReceipt`/`GoodsReceiptService` und der Event-getriebenen Kontextkommunikation — ein natürliches Alignment mit DDD-Prinzipien. Die größten Abweichungen sind pragmatischer Natur (Performance bei Batch-Queries, fehlende Value Objects) und kein Anzeichen für schlechtes Design. Bei einer Weiterentwicklung wäre das Einführen von Value Objects für `Stock` und `StorageLocation` der wirkungsvollste erste Schritt in Richtung eines saubereren DDD-Modells.

---

## 12. Quellenverzeichnis

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
| `GoodsReceiptService.java` | Wareneingangs-Geschäftslogik |
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

### AMQP (`amqp/`)
| Datei | Beschreibung |
|---|---|
| `RabbitConfig.java` | Spring AMQP Konfiguration (inkl. AMQP-4.0-Workaround) |
| `einkaufEvents/EinkaufEventListener.java` | Listener für Einkauf-Events (`NewQuotaEvent`, `DeleteQuotaEvent`) |
| `einkaufEvents/EinkaufEventPublisher.java` | Publisher für Einkauf-Events |
| `einkaufEvents/LogisticOrderListener.java` | Listener für Filialbestellungen |
| `einkaufEvents/LogisticEventPublisher.java` | Publisher für Lieferbestätigungen (`publishArticleDelivery`) |

### REST-API (`api/load/`)
| Datei | Beschreibung |
|---|---|
| `LoadTestHealthController.java` | `GET /api/load/health` |
| `LoadTestArticleController.java` | Artikel-Endpunkte |
| `LoadTestGoodsReceiptController.java` | Wareneingangs-Endpunkte |
| `LoadTestKommissionController.java` | Kommissions-Endpunkte |
| `LoadTestContingentController.java` | Kontingent-Endpunkte |
| `LoadTestStorageController.java` | Lagerplatz-Endpunkte |
| `LoadTestMiscController.java` | Sonstige Endpunkte |

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

### Konfiguration & Infrastruktur
| Datei | Beschreibung |
|---|---|
| `src/main/resources/application.properties` | Hauptkonfiguration |
| `src/main/resources/application-local.properties` | Lokales Profil (Docker-DB) |
| `src/main/resources/application-neon.properties` | Cloud-Profil (Neon-DB) |
| `pom.xml` | Maven Build, Abhängigkeiten, Plugins |
| `docker-compose.yml` | PostgreSQL lokal |
| `docker/init.sql` | Initiales Datenbankschema |
| `monitoring/docker-compose.yml` | Prometheus, Grafana, InfluxDB |
| `monitoring/prometheus.yml` | Prometheus Scrape-Konfiguration |
| `eclipse-formatter.xml` | Code-Formatter Regeln (Spotless) |

---

*Dokumentation erstellt auf Basis des Quellcodes (Stand: Mai 2026)*
