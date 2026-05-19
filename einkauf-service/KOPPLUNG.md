# Lose Kopplung im `einkauf-service`

Dieses Dokument bildet die im Briefing geforderten Prinzipien der **losen Kopplung** auf konkrete Stellen im Code ab. Es dient als Audit-Artefakt für die Abgabe.

---

## 1. Abstraktion durch Schnittstellen und Verträge

Konsumenten kennen nur die fachliche Schnittstelle, nie die konkrete Implementierung.

| Schnittstelle | Implementierung |
|---|---|
| `service/ArticleCategoryService.java` | `serviceImpl/ArticleCategoryServiceImpl.java` |
| `service/ArticleService.java` | `serviceImpl/ArticleServiceImpl.java` |
| `service/ContingentService.java` | `serviceImpl/ContingentServiceImpl.java` |
| `service/PurchaseOrderService.java` | `serviceImpl/PurchaseOrderServiceImpl.java` |
| `service/ShelfPlacementService.java` | `serviceImpl/ShelfPlacementServiceImpl.java` |
| `service/ShelfService.java` | `serviceImpl/ShelfServiceImpl.java` |
| `service/ShoppingCartService.java` | `serviceImpl/ShoppingCartServiceImpl.java` |
| `service/SupplierService.java` | `serviceImpl/SupplierServiceImpl.java` |
| `amqp/EventPublisherPort.java` | `amqp/EinkaufEventPublisher.java` |

Alle Controller und alle aufrufenden Services injizieren ausschließlich die **Interface-Typen** — Wechsel der Implementierung ist ohne Anpassung der Aufrufer möglich.

`serviceImpl/ContingentCleanupService.java` ist ein interner, geplanter Aufräumjob ohne externe Konsumenten — bewusst ohne Interface gehalten.

---

## 2. Event-basierte Kommunikation (asynchrone Kommunikation)

Integration mit anderen Services erfolgt **nicht** synchron über HTTP, sondern **asynchron** über RabbitMQ-Events. Dadurch entkoppeln sich Verfügbarkeit und Geschwindigkeit der beteiligten Services voneinander.

- **Publisher**: `amqp/EinkaufEventPublisher.java` — sendet `NewQuotaEvent`, `DeleteQuotaEvent` über `EventPublisherPort`
- **Listener**: `amqp/EinkaufEventListener.java` — konsumiert `NewDealEvent` aus dem Logistik-Service
- **Robustheit**: Publisher führt **drei Versuche mit exponentiellem Backoff** durch (200ms → 800ms → 3200ms). Lokale Transaktionen werden bei AMQP-Ausfall **nicht** zurückgerollt (fire-and-forget — bewusste lose Kopplung gegenüber der Außenwelt).
- **Idempotenz**: Listener berechnet eine `externalEventId` aus `(articleId, sekundengenauer Eingangszeit)` und prüft sie gegen die unique-constraint auf `received_deal_notification.external_event_id`. Redeliveries innerhalb derselben Sekunde werden verworfen.
- **Listener-Retry/DLQ**: In `application.properties` aktiviert (`spring.rabbitmq.listener.simple.retry.*`, `default-requeue-rejected=false`). Drei Versuche, danach landet die Nachricht im DLX-Pfad (RabbitMQ default).

---

## 3. Dependency Injection

Alle Beans (`@Service`, `@Component`, `@RestController`) werden ausschließlich per **Konstruktor-Injektion** verdrahtet — keine `@Autowired`-Felder, keine `@Inject`-Setter. Beispiele:

- `controller/CategoryController.java`
- `controller/OrderController.java`
- `serviceImpl/PurchaseOrderServiceImpl.java`
- `amqp/EinkaufEventListener.java`

Vorteile: ungekoppelte Tests (Mockito `@InjectMocks` greift sauber), Final-Felder garantieren Initialisierung, keine versteckten Abhängigkeiten.

---

## 4. Serviceorientierte Architektur / Microservices

`einkauf-service` ist ein eigenständig deploybarer Spring-Boot-Service:

- Eigener Build (`pom.xml`)
- Eigene Spring-Boot-Application: `PurchaseServiceApplication.java`
- Eigene PostgreSQL-Datenbank (siehe `docker-compose.yml`, Service `postgres` — `appdb`)
- REST-API unter `/api/v1/*` mit konsistenter Versionierung
- Health- und Metrik-Endpoints (`/actuator/health`, `/actuator/prometheus`)
- Containerisiert: `docker-compose.yml` Profil `app`

---

## 5. Lose gekoppelte Datenhaltung

Der `einkauf-service` betreibt seine **eigene** PostgreSQL-Instanz und teilt sich **keine** Tabellen mit anderen Services. Fremde Daten (z. B. eingehende Deals) werden lokal als eigene Entity gespiegelt:

- `entity/ReceivedDealNotification.java` — lokale Repräsentation von Logistik-Events
- `entity/Contingent.java` — eigene Domänenrepräsentation, kein Direktzugriff externer Services

DB-Zugang ausschließlich über Spring-Data-Repositories unter `repository/*` — kein roher SQL-Code in Controllern oder Views.

---

## 6. Standardisierte Schnittstellendokumentation (OpenAPI)

springdoc-openapi liefert eine versionierte, maschinenlesbare API-Spezifikation und macht Verträge inspizierbar — die zentrale Voraussetzung für unabhängige Entwicklung parallel arbeitender Teams.

- **Konfiguration**: `config/OpenApiConfig.java`, `application.properties` (springdoc.\*-Block)
- **UI**: `http://localhost:8080/swagger-ui` (Try-it-out aktiv)
- **Spec**: `http://localhost:8080/v3/api-docs`
- **Annotationen**: alle Controller unter `controller/*` tragen `@Tag`, `@Operation`, `@ApiResponse`. Request-DTOs unter `dto/*` tragen `@Schema` mit `description`/`example`/`requiredMode`.
- **Smoke-Test**: `src/test/java/fhdw/de/einkauf_service/OpenApiSmokeTest.java` prüft Vorhandensein der Spec und aller Controller-Pfade.

---

## 7. Strukturierter Fehlervertrag (RFC 7807 Problem Details)

Konsumenten erhalten Fehler **konsistent** als `application/problem+json` mit den Feldern `type`, `title`, `status`, `detail`, `timestamp`. Damit ist der Fehlervertrag genauso stabil wie der Erfolgsvertrag.

- **Handler**: `exception/GlobalExceptionHandler.java` — Mapping für Validation, IllegalArgument, IllegalState, NotFound, Overlap, OutOfBounds, DataIntegrityViolation, Fallback (500 mit `traceId`).
- **Test**: `src/test/java/fhdw/de/einkauf_service/exception/GlobalExceptionHandlerTest.java` verifiziert je Exception-Typ Status und ProblemDetail-Struktur.

---

## 8. Best Practices aus dem Briefing

| Best Practice | Umsetzung |
|---|---|
| Schnittstellen + Versionierung | URL-Versionierung `/api/v1/*`; OpenAPI-Version `v1` |
| Komponenten greifen nicht direkt auf interne Datenstrukturen anderer zu | DTO-Schicht trennt Entities von der API; eigene DB pro Service |
| Komponenten isoliert testen | Unit-Tests mit Mockito unter `src/test/java/fhdw/de/einkauf_service/serviceImpl/*Test.java` |
| Gesamt-System testen | Integration-Tests `*IntegrationTest.java` + JMeter-Lasttests im Repo-Root |
| Fehlerbehandlung + Monitoring | `GlobalExceptionHandler`, Actuator, Prometheus + Grafana |
| Automatisiertes Deployment | `docker-compose.yml` Profil `app`, Maven-Build |

---

## Verifikations-Checkliste

- [ ] `mvn clean test` läuft grün
- [ ] `docker-compose --profile app up` startet den Service erfolgreich
- [ ] `GET /swagger-ui` zeigt alle 8 Tags (Categories, Articles, Shelves, …)
- [ ] `GET /v3/api-docs` liefert valides OpenAPI 3 JSON
- [ ] `POST /api/v1/categories` mit leerem Body → 400 + ProblemDetail mit `errors`-Map
- [ ] `GET /api/v1/orders/999999` (nicht existent) → 404 + ProblemDetail
- [ ] Listener: zwei identische `NewDealEvent`s innerhalb einer Sekunde → genau eine Zeile in `received_deal_notification`
- [ ] Publisher: bei RabbitMQ-Ausfall → drei Versuche, danach `eventProcessingErrors`-Metrik +1, lokale Transaktion bleibt committet
