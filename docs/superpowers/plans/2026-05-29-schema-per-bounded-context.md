# Schema-per-Bounded-Context Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Separate the single shared PostgreSQL database into 7 domain-specific schemas, one per bounded context, demonstrating "Lose gekoppelte Datenhaltung" (Punkt 5 of Loser Kopplung).

**Architecture:** All entities currently live in PostgreSQL's default `public` schema. We add `schema=` to each entity's `@Table` annotation, matching the existing Java package structure (e.g. `data/articleInfo/` → schema `artikel`). The shared `idgenerator` sequence stays in `public` (accessible via PostgreSQL's default `search_path`) — no change to `AbstractEntity` needed.

**Tech Stack:** Spring Boot 4 / Hibernate 6, PostgreSQL 16 (Docker), `@Table(schema=...)` JPA annotation, `docker/init.sql` for schema creation.

---

## Bounded-Context → Schema Mapping

| Java package subfolder | PostgreSQL Schema | Tables |
|---|---|---|
| `data/articleInfo/` | `artikel` | `article_info` |
| `data/externalArticle/` | `artikel` | `article` |
| `data/storageLocation/` | `lager` | `storage_location` |
| `data/goodsreceipts/` | `wareneingang` | `goods_receipt`, `goods_receipt_item` |
| `data/orderPicking/` | `kommission` | `kommission`, `kommission_position`, `message_logistic` |
| `data/contingent/` | `kontingent` | `contingent`, `contingent_lasttest` |
| `data/restockorder/` | `nachbestellung` | `restock_order` |
| `data/messagingEvent/` | `messaging` | `messaging_event` |

---

## Files Modified

- `docker/init.sql` — CREATE SCHEMA statements
- `src/main/java/com/example/application/data/articleInfo/ArticleInfo.java`
- `src/main/java/com/example/application/data/externalArticle/ExternalArticle.java`
- `src/main/java/com/example/application/data/storageLocation/StorageLocation.java`
- `src/main/java/com/example/application/data/goodsreceipts/GoodsReceipt.java`
- `src/main/java/com/example/application/data/goodsreceipts/GoodsReceiptItem.java`
- `src/main/java/com/example/application/data/orderPicking/Kommission.java`
- `src/main/java/com/example/application/data/orderPicking/KommissionPosition.java`
- `src/main/java/com/example/application/data/orderPicking/MessageLogistic.java`
- `src/main/java/com/example/application/data/contingent/Contingent.java`
- `src/main/java/com/example/application/data/contingent/ContingentLasttest.java`
- `src/main/java/com/example/application/data/restockorder/RestockOrder.java`
- `src/main/java/com/example/application/data/messagingEvent/MessagingEvent.java`

---

## Task 1: Add Schema Creation to init.sql

**Files:**
- Modify: `docker/init.sql`

- [ ] **Step 1: Replace init.sql content**

Replace the entire file with:

```sql
-- Schemas pro Bounded Context (Loser Kopplung: eigene Datenhaltung pro Domaene)
CREATE SCHEMA IF NOT EXISTS artikel;
CREATE SCHEMA IF NOT EXISTS lager;
CREATE SCHEMA IF NOT EXISTS wareneingang;
CREATE SCHEMA IF NOT EXISTS kommission;
CREATE SCHEMA IF NOT EXISTS kontingent;
CREATE SCHEMA IF NOT EXISTS nachbestellung;
CREATE SCHEMA IF NOT EXISTS messaging;

-- Legacy sequence (public schema, geteilt fuer AbstractEntity-Subklassen via search_path)
CREATE SEQUENCE IF NOT EXISTS goods_receipt_seq START 1;
```

- [ ] **Step 2: Commit**

```bash
git add docker/init.sql
git commit -m "feat: add bounded-context schemas to PostgreSQL init script"
```

---

## Task 2: Artikel Schema — ArticleInfo & ExternalArticle

**Files:**
- Modify: `src/main/java/com/example/application/data/articleInfo/ArticleInfo.java`
- Modify: `src/main/java/com/example/application/data/externalArticle/ExternalArticle.java`

- [ ] **Step 1: Add schema to ArticleInfo**

In `ArticleInfo.java`, change:
```java
@Table(name = "article_info", indexes = {
```
to:
```java
@Table(name = "article_info", schema = "artikel", indexes = {
```

- [ ] **Step 2: Add schema to ExternalArticle**

In `ExternalArticle.java`, change:
```java
@Table(name = "article")
```
to:
```java
@Table(name = "article", schema = "artikel")
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/application/data/articleInfo/ArticleInfo.java
git add src/main/java/com/example/application/data/externalArticle/ExternalArticle.java
git commit -m "feat: move artikel entities to 'artikel' schema"
```

---

## Task 3: Lager Schema — StorageLocation & RestockOrder

**Files:**
- Modify: `src/main/java/com/example/application/data/storageLocation/StorageLocation.java`
- Modify: `src/main/java/com/example/application/data/restockorder/RestockOrder.java`

- [ ] **Step 1: Add schema to StorageLocation**

In `StorageLocation.java`, change:
```java
@Table(
    name = "storage_location",
    uniqueConstraints = {
```
to:
```java
@Table(
    name = "storage_location",
    schema = "lager",
    uniqueConstraints = {
```

- [ ] **Step 2: Add schema to RestockOrder**

In `RestockOrder.java`, change:
```java
@Table(name = "restock_order", indexes = {
```
to:
```java
@Table(name = "restock_order", schema = "nachbestellung", indexes = {
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/application/data/storageLocation/StorageLocation.java
git add src/main/java/com/example/application/data/restockorder/RestockOrder.java
git commit -m "feat: move storage and restock entities to 'lager'/'nachbestellung' schemas"
```

---

## Task 4: Wareneingang Schema — GoodsReceipt & GoodsReceiptItem

**Files:**
- Modify: `src/main/java/com/example/application/data/goodsreceipts/GoodsReceipt.java`
- Modify: `src/main/java/com/example/application/data/goodsreceipts/GoodsReceiptItem.java`

- [ ] **Step 1: Add schema to GoodsReceipt**

In `GoodsReceipt.java`, change:
```java
@Table(name = "goods_receipt", indexes = {
```
to:
```java
@Table(name = "goods_receipt", schema = "wareneingang", indexes = {
```

- [ ] **Step 2: Add schema to GoodsReceiptItem**

In `GoodsReceiptItem.java`, change:
```java
@Table(name = "goods_receipt_item", indexes = {
```
to:
```java
@Table(name = "goods_receipt_item", schema = "wareneingang", indexes = {
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/application/data/goodsreceipts/GoodsReceipt.java
git add src/main/java/com/example/application/data/goodsreceipts/GoodsReceiptItem.java
git commit -m "feat: move goods receipt entities to 'wareneingang' schema"
```

---

## Task 5: Kommission Schema — Kommission, KommissionPosition, MessageLogistic

**Files:**
- Modify: `src/main/java/com/example/application/data/orderPicking/Kommission.java`
- Modify: `src/main/java/com/example/application/data/orderPicking/KommissionPosition.java`
- Modify: `src/main/java/com/example/application/data/orderPicking/MessageLogistic.java`

- [ ] **Step 1: Add schema to Kommission**

In `Kommission.java`, change:
```java
@Table(name = "kommission", indexes = {
```
to:
```java
@Table(name = "kommission", schema = "kommission", indexes = {
```

- [ ] **Step 2: Add schema to KommissionPosition**

In `KommissionPosition.java`, change:
```java
@Table(name = "kommission_position")
```
to:
```java
@Table(name = "kommission_position", schema = "kommission")
```

- [ ] **Step 3: Add schema to MessageLogistic**

In `MessageLogistic.java`, change:
```java
@Table(name = "message_logistic", indexes = {
```
to:
```java
@Table(name = "message_logistic", schema = "kommission", indexes = {
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/application/data/orderPicking/Kommission.java
git add src/main/java/com/example/application/data/orderPicking/KommissionPosition.java
git add src/main/java/com/example/application/data/orderPicking/MessageLogistic.java
git commit -m "feat: move order picking entities to 'kommission' schema"
```

---

## Task 6: Kontingent Schema — Contingent & ContingentLasttest

**Files:**
- Modify: `src/main/java/com/example/application/data/contingent/Contingent.java`
- Modify: `src/main/java/com/example/application/data/contingent/ContingentLasttest.java`

- [ ] **Step 1: Add schema to Contingent**

In `Contingent.java`, change:
```java
@Table(name = "contingent", indexes = {
```
to:
```java
@Table(name = "contingent", schema = "kontingent", indexes = {
```

- [ ] **Step 2: Add schema to ContingentLasttest**

In `ContingentLasttest.java`, change:
```java
@Table(name = "contingent_lasttest", indexes = {
```
to:
```java
@Table(name = "contingent_lasttest", schema = "kontingent", indexes = {
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/application/data/contingent/Contingent.java
git add src/main/java/com/example/application/data/contingent/ContingentLasttest.java
git commit -m "feat: move contingent entities to 'kontingent' schema"
```

---

## Task 7: Messaging Schema — MessagingEvent

**Files:**
- Modify: `src/main/java/com/example/application/data/messagingEvent/MessagingEvent.java`

- [ ] **Step 1: Add @Table annotation with schema to MessagingEvent**

`MessagingEvent.java` currently has no `@Table` annotation. Add it and add the import.

Change:
```java
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
```
(the import already covers `@Table` via wildcard — no change needed to imports)

Change:
```java
@Entity
public class MessagingEvent extends AbstractEntity {
```
to:
```java
@Entity
@Table(name = "messaging_event", schema = "messaging")
public class MessagingEvent extends AbstractEntity {
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/example/application/data/messagingEvent/MessagingEvent.java
git commit -m "feat: move messaging event entity to 'messaging' schema"
```

---

## Task 8: Reset Database and Verify

**Why:** Hibernate DDL `update` mode adds new tables to the new schemas but does NOT drop old tables from `public`. To get a clean state with all tables in the correct schemas, the database volume must be wiped and recreated.

- [ ] **Step 1: Stop containers and remove volumes**

```bash
docker compose down -v
```

Expected output: containers stopped, volumes removed (including `pgdata`).

- [ ] **Step 2: Start containers fresh**

```bash
docker compose up -d
```

Expected output: PostgreSQL starts, `init.sql` runs and creates all 7 schemas.

- [ ] **Step 3: Start the Spring Boot application**

Run the app with the `local` profile (as usual). Watch the startup log for Hibernate DDL output — you should see statements like:
```
create table artikel.article_info (...)
create table wareneingang.goods_receipt (...)
create table kommission.kommission (...)
```
(exact log depends on `spring.jpa.show-sql` setting)

- [ ] **Step 4: Verify schemas in psql**

```bash
docker exec -it pg psql -U app -d appdb
```

Then run in psql:
```sql
-- List all schemas
\dn

-- Verify tables per schema
\dt artikel.*
\dt lager.*
\dt wareneingang.*
\dt kommission.*
\dt kontingent.*
\dt nachbestellung.*
\dt messaging.*
```

Expected output for `\dt artikel.*`:
```
           List of relations
 Schema  |    Name      | Type  | Owner
---------+--------------+-------+-------
 artikel | article      | table | app
 artikel | article_info | table | app
```

- [ ] **Step 5: Verify app works end-to-end**

Open `http://localhost:8080/swagger-ui/` and call a few endpoints (e.g. GET /api/artikels, GET /api/lagerplaetze). All should return 200.

- [ ] **Step 6: Final commit**

```bash
git add docker/init.sql
git commit -m "feat: complete schema-per-bounded-context migration — DB verified"
```

---

## Notes for Production (NeonDB)

The `application-neon.properties` profile points to NeonDB (serverless PostgreSQL). The schemas must also be created there manually before deploying. Run the following once against the NeonDB connection:

```sql
CREATE SCHEMA IF NOT EXISTS artikel;
CREATE SCHEMA IF NOT EXISTS lager;
CREATE SCHEMA IF NOT EXISTS wareneingang;
CREATE SCHEMA IF NOT EXISTS kommission;
CREATE SCHEMA IF NOT EXISTS kontingent;
CREATE SCHEMA IF NOT EXISTS nachbestellung;
CREATE SCHEMA IF NOT EXISTS messaging;
```

Hibernate DDL `update` will then create the tables in the correct schemas on first startup.
