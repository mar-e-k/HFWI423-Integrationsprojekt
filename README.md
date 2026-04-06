# HFWI423 – Integrationsprojekt: Vendix Kassensystem

Dieses Projekt implementiert ein verteiltes Kassensystem, bestehend aus einem zentralen **Filialensystem (Store)** und mehreren dezentralen **Kassensystemen (POS)**. Entwickelt im Rahmen des Integrationsprojekts HFWI423 an der FHDW Hannover.

---

## Systemarchitektur

```
┌─────────────────────────────────────────────────────────────────┐
│                        commons (4 Module)                       │
│  core · security · spring · ui                                  │
│  Gemeinsame DTOs, Entitäten, JWT-Auth, Basis-UI-Komponenten     │
└───────────────────┬─────────────────────┬───────────────────────┘
                    │                     │
        ┌───────────▼──────────┐ ┌────────▼───────────────┐
        │   store (Port 8080)  │ │   pos  (Port dynamisch)│
        │   Filialensystem     │ │   Kassensystem         │
        │                      │ │                        │
        │ • Artikelverwaltung  │ │ • Warenkorb            │
        │ • Kassenverwaltung   │ │ • Kaufabschluss        │
        │ • Benutzerverwaltung │ │ • Zahlungsabwicklung   │
        │ • Lasttests          │ │ • Registrierung beim   │
        │ • Tagesberichte      │ │   Filialensystem       │
        └──────────┬───────────┘ └────────────────────────┘
                   │  REST + JWT
        ┌──────────▼───────────┐
        │  PostgreSQL (Neon)   │
        │  RabbitMQ (Cloud)    │
        └──────────────────────┘

        ┌──────────────────────────────────┐
        │  monitoring/                     │
        │  Prometheus · Grafana · JMeter   │
        │  (Docker Compose)                │
        └──────────────────────────────────┘
```

---

## Maven-Module

| Modul | Beschreibung |
|---|---|
| `commons/core` | DTOs, Entitätsklassen, Basis-Services |
| `commons/security` | JWT-Authentifizierung (Generierung + Validierung) |
| `commons/spring` | Spring-Konfiguration, gemeinsame Beans |
| `commons/ui` | Wiederverwendbare Vaadin-UI-Komponenten |
| `store` | Filialensystem – zentrale Verwaltungsanwendung (Port 8080) |
| `pos` | Kassensystem – Point-of-Sale-Anwendung (Port dynamisch) |

---

## Voraussetzungen

- **Java 21** (JDK)
- **Maven 3.9+**
- **Docker Desktop** (für Prometheus + Grafana)
- **Apache JMeter 5.6.3** (nur für Lasttests)

---

## Projekt bauen

```bash
mvn clean install
```

---

## Anwendungen starten

### 1. Filialensystem starten (store)

In IntelliJ: `store/src/main/java/de/fhdw/vendix/store/StoreApplication.java` → grüner Play-Button

Oder per Terminal:
```bash
./mvnw spring-boot:run -pl store
```

Erreichbar unter: `http://localhost:8080`

**Standard-Login:**
- Benutzername: `A` / Passwort: `1234`
- Admin (für Lasttests): `JMeter` / Passwort: `1234`

---

### 2. Kassensystem starten (pos)

In IntelliJ: `pos/src/main/java/de/fhdw/vendix/pos/PosApplication.java` → grüner Play-Button

Das Kassensystem registriert sich automatisch beim Filialensystem und bekommt einen dynamischen Port zugewiesen.

**Standard-Login:**
- Benutzername: `C` / Passwort: `1234`

---

## Funktionsübersicht

### Filialensystem (`store`, Port 8080)

| View | Funktion |
|---|---|
| `MainView` | Dashboard: verbundene Kassen, Online-Status, Links |
| `AdminView` | Artikelverwaltung: Erstellen, Bearbeiten, Suchen |
| `StockView` | Lagerbestandsverwaltung |
| `RoleView` | Benutzerverwaltung und Rollenzuweisung |
| `RegisterAddView` | Kassenverwaltung |
| `DailyReceiptReportingView` | Tagesberichte und Umsatzauswertung |
| `PerformanceTestView` | Lasttest-Steuerung mit Grafana-Dashboard |

### Kassensystem (`pos`, Port dynamisch)

| View | Funktion |
|---|---|
| `CashierView` | Artikelsuche, Warenkorb, Rabatte, Preisfreigabe |
| `PaymentView` | Kaufabschluss und Zahlungsabwicklung |

---

## Monitoring & Lasttests

Prometheus und Grafana laufen per Docker Compose. JMeter wird lokal installiert und über die Vaadin-UI gesteuert.

```bash
cd monitoring/
docker compose up -d
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin / admin)

**Setup-Anleitung:** `monitoring/SETUP.md`

---

## Live-Reload (Entwicklung)

Damit Änderungen am Code sofort ohne Neustart sichtbar werden:

1. IntelliJ → `Settings` → `Build, Execution, Deployment` → `Compiler`
   → **`Build project automatically`** aktivieren

2. IntelliJ → `Settings` → `Advanced Settings`
   → **`Allow auto-make to start even if developed application is currently running`** aktivieren

---

## Technologie-Stack

| Bereich | Technologie |
|---|---|
| Backend | Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend | Vaadin Flow |
| Datenbank | PostgreSQL (Neon Cloud) |
| Messaging | RabbitMQ (CloudAMQP) |
| Authentifizierung | JWT (nimbus-jose-jwt) |
| Monitoring | Prometheus, Grafana, Micrometer |
| Lasttests | Apache JMeter 5.6.3 |
| Infrastruktur | Docker Compose |