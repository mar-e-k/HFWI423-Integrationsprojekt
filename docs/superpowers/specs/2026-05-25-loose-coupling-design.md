# Loose Coupling Implementation Design
**Date:** 2026-05-25
**Project:** HFWI423 Integrationsprojekt — Logistik
**Scope:** Implement the 6 points of loose coupling for the final presentation

---

## Implementation Status

| Punkt | Status |
|-------|--------|
| 1. Abstraktion durch Schnittstellen (OpenAPI, DTOs) | DONE |
| 2. Event-basierte Kommunikation (RabbitMQ + ApplicationEvent) | DONE |
| 3. Dependency Injection (Spring DI) | DONE |
| 4. SOA / Microservices (API Gateway + domain-sliced Controllers) | DONE |
| 5. Lose gekoppelte Datenhaltung (Single DB, documented trade-off) | DONE |
| 6. Asynchrone Kommunikation (RabbitMQ) | DONE |

---

## Context

3 sessions remain. The final session requires presenting optimizations with load/performance test results compared to previous baselines (available as screenshots/PowerPoint). The goal is to demonstrate understanding and practical application of loose coupling principles — not production-grade infrastructure.

---

## Target Architecture

```
[JMeter / Browser]
        |
        v
[Gateway :8080]          <- standalone Spring Boot 3.4.5 project in gateway/
        |
        v
[Logistik-Service :8081] <- existing service, refactored
        |          |
        v          v
  [PostgreSQL]  [RabbitMQ]
```

The Gateway is the single entry point. In local development the Logistik-Service is reachable directly on port 8081. For Docker deployments the gateway runs in a container; the Logistik-Service runs locally or in a second container.

---

## 6 Loose Coupling Points — Implementation Map

| Point | Strategy | Status |
|-------|----------|--------|
| 1. Abstraktion durch Schnittstellen | springdoc-openapi 3.0.3 (Swagger UI), DTOs separating API from domain | Done |
| 2. Event-basierte Kommunikation | RabbitMQ (existing) + Spring ApplicationEvent (GoodsReceiptApprovedEvent) | Done |
| 3. Dependency Injection | Spring DI throughout (constructor injection in all controllers/services) | Done |
| 4. SOA / Microservices | API Gateway + 8 domain-sliced REST controllers | Done |
| 5. Lose gekoppelte Datenhaltung | Single DB documented as trade-off (acceptable for uni) | Documented |
| 6. Asynchrone Kommunikation | RabbitMQ async messaging (existing) | Done |

---

## Part 1: Logistik-Service Refactoring

### Port
`server.port=${PORT:8081}` — default 8081, overridable via environment variable.

### Web Service Slicing — Controller Restructuring

All old `/api/load/*` controllers were deleted and replaced with domain-sliced controllers:

| Old Controller | New Controller | New Path |
|----------------|---------------|----------|
| `LoadTestArticleController` | `ArtikelController` | `/api/artikels` |
| `LoadTestKommissionController` | `KommissionController` | `/api/kommissionen` |
| `LoadTestGoodsReceiptController` | `WareneingangController` | `/api/wareneingaenge` |
| `LoadTestStorageController` | `LagerplatzController` | `/api/lagerplaetze` |
| `LoadTestContingentController` | `KontingentController` | `/api/kontingente` |
| `LoadTestHealthController` | `HealthController` | `/api/health` |
| `LoadTestMiscController` (Nachbestellung) | `NachbestellungController` | `/api/nachbestellungen` |
| `LoadTestMiscController` (Neue Artikel) | `NeueArtikelController` | `/api/neue-artikel` |

Each controller:
- Lives in its own package: `com.example.application.api.<domain>`
- Uses only service/repository interfaces — no direct entity exposure in API layer
- Annotated with `@Tag(name = "...")` and `@Operation(summary = "...")`

### OpenAPI / Swagger

Dependency in `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

> **Note:** springdoc 2.x targets Spring Boot 3.x (Spring Framework 6). Spring Boot 4.0 uses Spring Framework 7. Use springdoc 3.0.3 or newer for Spring Boot 4.x projects.

Config in `application.properties`:
```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=alpha
```

Swagger UI accessible at:
- Direct: `http://localhost:8081/swagger-ui.html`
- Via Gateway: `http://localhost:8080/swagger-ui.html`

### Internal Observer Pattern (Spring ApplicationEvent)

Demonstrates the Observer pattern within the service — `GoodsReceiptService` does not know that `RestockCheckListener` exists:

- `GoodsReceiptApprovedEvent` — published at the end of `completeInspection()` via `ApplicationEventPublisher`
- `RestockCheckListener` — `@EventListener` reacts and logs the restock check trigger
- Package: `com.example.application.events`

This decouples the goods receipt flow from potential restock logic internally. Any number of listeners can be added without touching `GoodsReceiptService`.

---

## Part 2: API Gateway

### Location
```
HFWI423-Integrationsprojekt/
├── src/                    <- Logistik-Service (Spring Boot 4.0.0)
├── gateway/                <- standalone Spring Boot 3.4.5 project
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/gateway/
│       │   ├── GatewayApplication.java
│       │   └── filter/
│       │       ├── LoggingFilter.java
│       │       └── RateLimitingFilter.java
│       └── resources/application.yml
└── pom.xml                 <- Logistik-Service (unchanged)
```

The gateway is a standalone Spring Boot project within the same Git repo. It is NOT a Maven parent-child module to avoid restructuring the existing project.

### Dependencies

- Spring Boot 3.4.5 (parent)
- Spring Cloud 2024.0.1 (`spring-cloud-starter-gateway`)
- `spring-boot-starter-actuator`

> **Why 3.4.5 and not 4.0.0?** Spring Cloud Gateway does not yet support Spring Boot 4.0 (Spring Framework 7). The gateway is intentionally on Boot 3.4.5 to use the stable Spring Cloud release train.

### Routing (`gateway/src/main/resources/application.yml`)
```yaml
server:
  port: 8080

spring:
  application:
    name: logistik-gateway
  cloud:
    gateway:
      routes:
        - id: logistik-api
          uri: http://localhost:8081
          predicates:
            - Path=/api/**
          filters:
            - AddRequestHeader=X-Gateway-Source, logistik-gateway
        - id: swagger-ui
          uri: http://localhost:8081
          predicates:
            - Path=/swagger-ui/**, /api-docs/**

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Filters

**LoggingFilter** (`Ordered.HIGHEST_PRECEDENCE`):
- Logs `>> METHOD PATH` on request and `<< METHOD PATH STATUS DURATIONms` on response
- Demonstrates cross-cutting concern at gateway level

**RateLimitingFilter** (`HIGHEST_PRECEDENCE + 1`):
- ConcurrentHashMap-based sliding window per IP address
- Limit: 10 requests/second per IP
- Returns HTTP 429 on breach
- No Redis required — sufficient for the university demo

---

## Part 3: Docker Compose

```yaml
services:
  db:
    image: postgres:16
    container_name: pg
    environment:
      POSTGRES_USER: app
      POSTGRES_PASSWORD: secret
      POSTGRES_DB: appdb
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./docker/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "app"]
      interval: 5s
      timeout: 3s
      retries: 20

  gateway:
    build:
      context: ./gateway
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    extra_hosts:
      - "host.docker.internal:host-gateway"

volumes:
  pgdata:
```

**Usage pattern for the demo:**
- `docker-compose up db` — starts PostgreSQL
- Logistik-Service runs locally in IntelliJ on port 8081
- Gateway runs locally via `cd gateway && mvn spring-boot:run` on port 8080
- `docker-compose up gateway` — starts gateway in Docker (routes to host via `host.docker.internal:8081`)

> **Docker routing note:** When the gateway runs in Docker but the Logistik-Service runs locally, Docker cannot reach `localhost:8081` (that resolves to the container). `extra_hosts: host.docker.internal:host-gateway` maps `host.docker.internal` to the host machine. To use this, override the route URI at startup: `SPRING_CLOUD_GATEWAY_ROUTES_0_URI=http://host.docker.internal:8081`.

---

## Part 4: JMeter Test Update

All 11 JMeter `.jmx` files were updated (50 path replacements total):

| Old Path | New Path |
|----------|----------|
| `/api/load/articles` | `/api/artikels` |
| `/api/load/kommissionen` | `/api/kommissionen` |
| `/api/load/goods-receipts` | `/api/wareneingaenge` |
| `/api/load/storage-locations` | `/api/lagerplaetze` |
| `/api/load/contingents` | `/api/kontingente` |
| `/api/load/restock` | `/api/nachbestellungen` |
| `/api/load/new-articles` | `/api/neue-artikel` |
| `/api/load/health` | `/api/health` |
| `/api/load/messaging-events` | `/api/neue-artikel/messaging-events` |

Port stays `8080` — JMeter now hits the Gateway, which is the architectural change to highlight.

---

## Presentation Talking Points

1. **OpenAPI** — "Wir haben eine maschinenlesbare Schnittstellendefinition eingeführt. Jedes Team kann unabhängig entwickeln — der Vertrag ist in Swagger UI sichtbar."
2. **Web Service Slicing** — "Jede Domäne hat ihren eigenen Controller. Änderungen an der Kommission berühren den Wareneingang nicht — lose Kopplung durch klare Schnittstellengrenzen."
3. **API Gateway** — "Der Gateway ist der einzige Einstiegspunkt. Rate-Limiting, Routing und Logging sind zentralisiert — komplett getrennt vom Business-Code im Logistik-Service."
4. **Observer / Pub-Sub** — "RabbitMQ entkoppelt unsere Services asynchron. Intern nutzen wir Spring ApplicationEvents: `GoodsReceiptService` weiß nicht, wer auf das `GoodsReceiptApprovedEvent` reagiert."
5. **Load-Test-Vergleich** — "Durch den Gateway-Layer sind alle Requests jetzt im Gateway-Log sichtbar. Bottlenecks lassen sich sofort per IP und Endpoint identifizieren."

---

## Out of Scope

- Separate databases per service (would require splitting the domain — too large for remaining time)
- Kafka (RabbitMQ already fulfils the messaging requirement)
- Kubernetes / container orchestration
- Authentication/Authorization in gateway
- springdoc 3.x for the gateway itself (gateway has no REST endpoints to document)
