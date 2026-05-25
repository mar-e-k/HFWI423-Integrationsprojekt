# Loose Coupling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement 6 loose coupling principles (OpenAPI, domain-sliced controllers, API Gateway, Observer pattern) for the final university presentation with before/after JMeter comparison.

**Architecture:** Logistik-Service (port 8081) exposes domain-sliced REST endpoints documented via springdoc-openapi. A standalone Spring Cloud Gateway (port 8080, `gateway/` folder, Spring Boot 3.4.5) acts as single entry point with request logging and routing. Spring ApplicationEvents demonstrate internal Observer pattern. RabbitMQ (existing) handles async external communication.

**Tech Stack:** Spring Boot 4.0.0 (Logistik), Spring Boot 3.4.5 + Spring Cloud 2024.0.1 (Gateway), springdoc-openapi-starter-webmvc-ui 3.0.3, Docker Compose

---

## File Map

### Logistik-Service — Create
- `src/main/java/com/example/application/config/OpenApiConfig.java`
- `src/main/java/com/example/application/api/artikel/ArtikelController.java`
- `src/main/java/com/example/application/api/kommission/KommissionController.java`
- `src/main/java/com/example/application/api/wareneingang/WareneingangController.java`
- `src/main/java/com/example/application/api/lagerplatz/LagerplatzController.java`
- `src/main/java/com/example/application/api/kontingent/KontingentController.java`
- `src/main/java/com/example/application/api/nachbestellung/NachbestellungController.java`
- `src/main/java/com/example/application/api/neueartikel/NeueArtikelController.java`
- `src/main/java/com/example/application/api/health/HealthController.java`
- `src/main/java/com/example/application/events/GoodsReceiptApprovedEvent.java`
- `src/main/java/com/example/application/events/RestockCheckListener.java`

### Logistik-Service — Modify
- `pom.xml` — add springdoc-openapi dependency
- `src/main/resources/application.properties` — add springdoc config
- `src/main/java/com/example/application/services/GoodsReceiptService.java` — publish event in completeInspection()

### Logistik-Service — Delete (all endpoints moved to domain controllers)
- `src/main/java/com/example/application/api/load/LoadTestArticleController.java`
- `src/main/java/com/example/application/api/load/LoadTestKommissionController.java`
- `src/main/java/com/example/application/api/load/LoadTestGoodsReceiptController.java`
- `src/main/java/com/example/application/api/load/LoadTestStorageController.java`
- `src/main/java/com/example/application/api/load/LoadTestContingentController.java`
- `src/main/java/com/example/application/api/load/LoadTestHealthController.java`
- `src/main/java/com/example/application/api/load/LoadTestMiscController.java`

### Gateway — Create
- `gateway/pom.xml`
- `gateway/src/main/java/com/example/gateway/GatewayApplication.java`
- `gateway/src/main/java/com/example/gateway/filter/LoggingFilter.java`
- `gateway/src/main/java/com/example/gateway/filter/RateLimitingFilter.java`
- `gateway/src/main/resources/application.yml`

### Docker Compose — Modify
- `docker-compose.yml` — add gateway service

---

## Task 1: Add springdoc-openapi to Logistik-Service

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`
- Create: `src/main/java/com/example/application/config/OpenApiConfig.java`

- [ ] **Step 1: Add springdoc dependency to pom.xml**

Open `pom.xml` and add inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

> **Wichtig:** springdoc 2.x ist mit Spring Boot 4.0 (Spring Framework 7) inkompatibel — führt zu `BeanCreationException` beim Start. springdoc 3.0.3 ist die erste kompatible Version für Spring Boot 4.x.

- [ ] **Step 2: Add springdoc config to application.properties**

Append to `src/main/resources/application.properties`:

```properties
# ---- OpenAPI / Swagger ----
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=alpha
```

- [ ] **Step 3: Create OpenApiConfig.java**

Create `src/main/java/com/example/application/config/OpenApiConfig.java`:

```java
package com.example.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logistikOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Logistik-Service API")
                        .description("REST-Schnittstelle des Logistik-Services. Erreichbar via API-Gateway auf Port 8080.")
                        .version("1.0.0"));
    }
}
```

- [ ] **Step 4: Start the service and verify Swagger UI**

Run the Logistik-Service (IntelliJ or `mvn spring-boot:run`). Open:
`http://localhost:8081/swagger-ui.html`

Expected: Swagger UI loads with "Logistik-Service API" title (no endpoints yet — those come in Tasks 2-8).

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main/resources/application.properties src/main/java/com/example/application/config/OpenApiConfig.java
git commit -m "feat: add springdoc-openapi for REST API documentation"
```

---

## Task 2: ArtikelController — /api/artikels

**Files:**
- Create: `src/main/java/com/example/application/api/artikel/ArtikelController.java`
- Delete: `src/main/java/com/example/application/api/load/LoadTestArticleController.java`

Maps all endpoints from `LoadTestArticleController`, URL prefix changes from `/api/load/articles` to `/api/artikels`.

- [ ] **Step 1: Create ArtikelController.java**

```java
package com.example.application.api.artikel;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.goodsreceipts.GoodsReceiptItemRepository;
import com.example.application.data.goodsreceipts.GoodsReceiptRepository;
import com.example.application.data.restockorder.RestockOrderRepository;
import com.example.application.services.ArticleInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/artikels")
@Tag(name = "Artikels", description = "Artikel-Stammdaten und Bestandsverwaltung")
public class ArtikelController {

    private final ArticleInfoService articleInfoService;
    private final ArticleInfoRepository articleInfoRepository;
    private final ContingentRepository contingentRepository;
    private final RestockOrderRepository restockOrderRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;

    public ArtikelController(ArticleInfoService articleInfoService,
                             ArticleInfoRepository articleInfoRepository,
                             ContingentRepository contingentRepository,
                             RestockOrderRepository restockOrderRepository,
                             GoodsReceiptItemRepository goodsReceiptItemRepository,
                             GoodsReceiptRepository goodsReceiptRepository) {
        this.articleInfoService = articleInfoService;
        this.articleInfoRepository = articleInfoRepository;
        this.contingentRepository = contingentRepository;
        this.restockOrderRepository = restockOrderRepository;
        this.goodsReceiptItemRepository = goodsReceiptItemRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    record StockChangeRequest(int delta, String reason) {}
    record StorageLocationUpdateRequest(String storageLocation) {}

    @Operation(summary = "Artikelliste paginiert abrufen")
    @GetMapping
    public Page<ArticleInfo> articles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return articleInfoService.list(PageRequest.of(page, size), null);
    }

    @Operation(summary = "Artikel mit Filterparametern abrufen")
    @GetMapping("/filter")
    public Page<ArticleInfo> articlesFiltered(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String articleNumber,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) String storageLocation) {
        Specification<ArticleInfo> spec = buildFilterSpec(name, articleNumber, minStock, storageLocation);
        return articleInfoService.list(PageRequest.of(page, size), spec);
    }

    @Operation(summary = "Bestand eines Artikels aendern")
    @PostMapping("/{id}/stock")
    public ArticleInfo changeStock(@PathVariable Long id, @RequestBody StockChangeRequest req) {
        try {
            ArticleInfo article = articleInfoService.findById(id);
            return articleInfoService.applyStockChange(article, req.delta());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Lagerplatz eines Artikels aktualisieren")
    @PutMapping("/{id}/storage-location")
    public ArticleInfo updateStorageLocation(@PathVariable Long id,
                                              @RequestBody StorageLocationUpdateRequest req) {
        try {
            return articleInfoService.updateStorageLocation(id, req.storageLocation());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Alle SIM-Artikel und abhaengige Daten loeschen")
    @DeleteMapping("/sim")
    @Transactional
    public java.util.Map<String, Object> deleteSimArticles() {
        List<Long> simIds = articleInfoRepository.findAll().stream()
                .filter(a -> a.getArticleNumber() != null && a.getArticleNumber().startsWith("SIM-"))
                .map(ArticleInfo::getId)
                .toList();
        if (!simIds.isEmpty()) {
            goodsReceiptItemRepository.deleteByArticleIdIn(simIds);
            goodsReceiptRepository.deleteReceiptsWithNoItems();
            contingentRepository.deleteByArticleIdIn(simIds);
        }
        restockOrderRepository.deleteAllSimOrders();
        int deleted = articleInfoRepository.deleteAllSimArticles();
        return java.util.Map.of("deleted", deleted);
    }

    private Specification<ArticleInfo> buildFilterSpec(String name, String articleNumber,
                                                        Integer minStock, String storageLocation) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            if (articleNumber != null && !articleNumber.isBlank())
                predicates.add(cb.like(cb.lower(root.get("articleNumber")), "%" + articleNumber.toLowerCase() + "%"));
            if (minStock != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("stockLevel"), minStock));
            if (storageLocation != null && !storageLocation.isBlank())
                predicates.add(cb.equal(root.get("storageLocation"), storageLocation));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

- [ ] **Step 2: Delete the old controller**

Delete the file: `src/main/java/com/example/application/api/load/LoadTestArticleController.java`

- [ ] **Step 3: Verify build compiles**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/application/api/artikel/
git rm src/main/java/com/example/application/api/load/LoadTestArticleController.java
git commit -m "feat: replace LoadTestArticleController with domain-sliced ArtikelController (/api/artikels)"
```

---

## Task 3: KommissionController — /api/kommissionen

**Files:**
- Create: `src/main/java/com/example/application/api/kommission/KommissionController.java`
- Delete: `src/main/java/com/example/application/api/load/LoadTestKommissionController.java`

- [ ] **Step 1: Create KommissionController.java**

```java
package com.example.application.api.kommission;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.KommissionRepository;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.KommissionService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.WeeklyKommissionScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/kommissionen")
@Tag(name = "Kommissionen", description = "Kommissionierung und Filial-Bestellungen")
public class KommissionController {

    private final KommissionService kommissionService;
    private final KommissionRepository kommissionRepository;
    private final MessageLogisticRepository messageLogisticRepository;
    private final ArticleInfoService articleInfoService;
    private final ArticleInfoRepository articleInfoRepository;
    private final ArticleSyncService articleSyncService;
    private final WeeklyKommissionScheduler weeklyKommissionScheduler;
    private final LogisticEventPublisher logisticEventPublisher;

    public KommissionController(KommissionService kommissionService,
                                KommissionRepository kommissionRepository,
                                MessageLogisticRepository messageLogisticRepository,
                                ArticleInfoService articleInfoService,
                                ArticleInfoRepository articleInfoRepository,
                                ArticleSyncService articleSyncService,
                                WeeklyKommissionScheduler weeklyKommissionScheduler,
                                LogisticEventPublisher logisticEventPublisher) {
        this.kommissionService = kommissionService;
        this.kommissionRepository = kommissionRepository;
        this.messageLogisticRepository = messageLogisticRepository;
        this.articleInfoService = articleInfoService;
        this.articleInfoRepository = articleInfoRepository;
        this.articleSyncService = articleSyncService;
        this.weeklyKommissionScheduler = weeklyKommissionScheduler;
        this.logisticEventPublisher = logisticEventPublisher;
    }

    record SetQuantityRequest(long quantity) {}

    @Operation(summary = "Alle Kommissionen abrufen")
    @GetMapping
    public List<Kommission> kommissionen() {
        return kommissionService.getAlleKommissionen();
    }

    @Operation(summary = "Woechentliche Kommissionierung ausloesen")
    @PostMapping("/trigger")
    public Map<String, String> triggerWeeklyKommissionen() {
        weeklyKommissionScheduler.createWeeklyKommissionen();
        return Map.of("result", "Woechentliche Kommissionierung ausgeloest");
    }

    @Operation(summary = "Kommission abschliessen")
    @PutMapping("/{id}/finish")
    @Transactional
    public Kommission finishKommission(@PathVariable Long id) {
        Kommission kommission = kommissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Kommission " + id + " nicht gefunden"));
        if (Boolean.TRUE.equals(kommission.getFinished())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Kommission " + id + " ist bereits abgeschlossen");
        }
        List<MessageLogistic> items = messageLogisticRepository.findByKommissionId(id);
        for (MessageLogistic msg : items) {
            String articleNumber = msg.getArticleNumber();
            if (articleNumber == null && msg.getArticleId() != null) {
                try {
                    articleNumber = articleInfoService.findById(msg.getArticleId()).getArticleNumber();
                } catch (Exception ignored) {}
            }
            if (articleNumber == null) continue;
            articleInfoService.updateStock(articleNumber, (int) msg.getQuantity());
            try {
                long storeIdLong = Long.parseLong(kommission.getStoreId().replaceAll("[^0-9]", ""));
                Long articleId = msg.getArticleId();
                if (articleId != null) {
                    logisticEventPublisher.publishArticleDelivery(storeIdLong, articleId, msg.getQuantity());
                }
            } catch (Exception ignored) {}
        }
        kommission.setFinished(true);
        return kommissionService.save(kommission);
    }

    @Operation(summary = "Simulierte Filial-Bestellungen erstellen")
    @PostMapping("/simulate-store-orders")
    @Transactional
    public ResponseEntity<Map<String, Object>> simulateStoreOrders(
            @RequestParam(defaultValue = "3") int stores) {
        List<ArticleInfo> articlesWithStock = articleInfoRepository.findAll().stream()
                .filter(a -> a.getTotalStock() != null && a.getTotalStock() > 0 && a.getArticleId() != null)
                .toList();
        List<NewArticleCandidate> newCandidates = articleSyncService.findNewArticlesFromLasttestContingents();
        Random random = new Random();
        List<MessageLogistic> batch = new ArrayList<>();
        for (int s = 1; s <= stores; s++) {
            String storeId = "Lasttest-Store-" + s;
            messageLogisticRepository.deleteProcessedByStore(storeId);
            messageLogisticRepository.deleteAllUnprocessedByStore(storeId);
            for (ArticleInfo article : articlesWithStock) {
                MessageLogistic msg = new MessageLogistic();
                msg.setStoreId(storeId);
                msg.setArticleNumber(article.getArticleNumber());
                msg.setArticleId(article.getArticleId());
                msg.setQuantity(1 + random.nextInt(5));
                msg.setProcessed(false);
                batch.add(msg);
            }
            for (NewArticleCandidate candidate : newCandidates) {
                MessageLogistic msg = new MessageLogistic();
                msg.setStoreId(storeId);
                msg.setArticleNumber(candidate.getArticleNumber());
                msg.setArticleId(candidate.getArticleId());
                msg.setQuantity(1 + random.nextInt(5));
                msg.setProcessed(false);
                batch.add(msg);
            }
        }
        messageLogisticRepository.saveAll(batch);
        return ResponseEntity.ok(Map.of("createdOrders", batch.size(), "stores", stores,
                "fromArticleInfo", articlesWithStock.size(), "fromNewArticles", newCandidates.size()));
    }

    @Operation(summary = "Alle offenen Kommissionen abschliessen")
    @PostMapping("/finish-all")
    public ResponseEntity<Integer> finishAll() {
        List<Kommission> open = kommissionRepository.findAll().stream()
                .filter(k -> !Boolean.TRUE.equals(k.getFinished()))
                .toList();
        int finished = 0;
        for (Kommission kommission : open) {
            try {
                kommissionService.finishAtomar(kommission);
                finished++;
            } catch (Exception e) {
                System.out.println("[finish-all] FEHLER Kommission " + kommission.getId() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(finished);
    }

    @Operation(summary = "Realisierte Menge einer Kommissionsposition setzen")
    @PutMapping("/{id}/items/{articleId}/quantity")
    public MessageLogistic setItemQuantity(@PathVariable Long id,
                                           @PathVariable Long articleId,
                                           @RequestBody SetQuantityRequest req) {
        List<MessageLogistic> items = messageLogisticRepository.findByKommissionId(id);
        MessageLogistic msg = items.stream()
                .filter(m -> articleId.equals(m.getArticleId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Kein Item mit articleId=" + articleId + " in Kommission " + id));
        msg.setQuantity(req.quantity());
        return messageLogisticRepository.save(msg);
    }
}
```

- [ ] **Step 2: Delete old controller**

Delete: `src/main/java/com/example/application/api/load/LoadTestKommissionController.java`

- [ ] **Step 3: Verify build**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/application/api/kommission/
git rm src/main/java/com/example/application/api/load/LoadTestKommissionController.java
git commit -m "feat: replace LoadTestKommissionController with KommissionController (/api/kommissionen)"
```

---

## Task 4: WareneingangController — /api/wareneingaenge

**Files:**
- Create: `src/main/java/com/example/application/api/wareneingang/WareneingangController.java`
- Delete: `src/main/java/com/example/application/api/load/LoadTestGoodsReceiptController.java`

- [ ] **Step 1: Create WareneingangController.java**

```java
package com.example.application.api.wareneingang;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.goodsreceipts.GoodsReceiptItem;
import com.example.application.data.goodsreceipts.GoodsReceiptItemStatus;
import com.example.application.data.goodsreceipts.GoodsReceiptStatus;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.GoodsReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/wareneingaenge")
@Tag(name = "Wareneingaenge", description = "Wareneingaenge pruefen und verwalten")
public class WareneingangController {

    private final GoodsReceiptService goodsReceiptService;
    private final ArticleInfoService articleInfoService;

    public WareneingangController(GoodsReceiptService goodsReceiptService,
                                   ArticleInfoService articleInfoService) {
        this.goodsReceiptService = goodsReceiptService;
        this.articleInfoService = articleInfoService;
    }

    record CreateReceiptRequest(String supplierName, String deliveryNoteNumber, LocalDate deliveryDate) {}
    record CreateFromOrdersRequest(List<Long> restockOrderIds, String supplierName, String deliveryNoteNumber, LocalDate deliveryDate) {}
    record AddItemRequest(Long articleId, Integer expectedQty, Integer actualQty, String defectNotes) {}
    record UpdateItemRequest(Integer actualQty, String defectNotes) {}
    record SetItemStatusRequest(String status) {}
    record ItemSummary(Long itemId, Integer qty) {}

    @Operation(summary = "Alle Wareneingaenge abrufen")
    @GetMapping
    public List<GoodsReceipt> goodsReceipts() {
        return goodsReceiptService.findAll();
    }

    @Operation(summary = "Einzelnen Wareneingang laden")
    @GetMapping("/{id}")
    public GoodsReceipt getById(@PathVariable Long id) {
        try {
            return goodsReceiptService.getById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "IDs aller Wareneingaenge im Status IN_PRUEFUNG")
    @GetMapping("/pending-ids")
    public List<Long> pendingIds() {
        return goodsReceiptService.findAll().stream()
                .filter(gr -> gr.getStatus() == GoodsReceiptStatus.IN_PRUEFUNG)
                .map(GoodsReceipt::getId)
                .toList();
    }

    @Operation(summary = "Item-Zusammenfassung eines Wareneingangs")
    @GetMapping("/{id}/item-summaries")
    public List<ItemSummary> itemSummaries(@PathVariable Long id) {
        return goodsReceiptService.getItemsForReceipt(id).stream()
                .map(item -> new ItemSummary(item.getId(), item.getActualQuantity()))
                .toList();
    }

    @Operation(summary = "Offene Bestellungen fuer Wareneingang-Dialog")
    @GetMapping("/open-orders")
    public List<RestockOrder> openOrders() {
        return goodsReceiptService.findOpenRestockOrders();
    }

    @Operation(summary = "Wareneingang aus ausgewaehlten Bestellungen anlegen")
    @PostMapping("/from-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceipt createFromOrders(@RequestBody CreateFromOrdersRequest req) {
        if (req.restockOrderIds() == null || req.restockOrderIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keine Bestellungen ausgewaehlt");
        }
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        try {
            return goodsReceiptService.createFromRestockOrders(req.restockOrderIds(), supplier, note, date);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Operation(summary = "Wareneingang aus naechsten 10 offenen Bestellungen anlegen")
    @PostMapping("/from-next-batch")
    public ResponseEntity<GoodsReceipt> createFromNextBatch(@RequestBody CreateReceiptRequest req) {
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        GoodsReceipt receipt = goodsReceiptService.createFromNextBatch(10, supplier, note, date);
        if (receipt == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @Operation(summary = "Wareneingang aus naechster einzelner offenen Bestellung anlegen")
    @PostMapping("/from-next-order")
    public ResponseEntity<GoodsReceipt> createFromNextOrder(@RequestBody CreateReceiptRequest req) {
        List<RestockOrder> open = goodsReceiptService.findOpenRestockOrders();
        if (open.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        for (RestockOrder order : open) {
            try {
                GoodsReceipt receipt = goodsReceiptService.createFromRestockOrders(
                        List.of(order.getId()), supplier, note, date);
                return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
            } catch (Exception e) {
                System.out.println("[from-next-order] SKIP orderId=" + order.getId() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Neuen Wareneingang anlegen")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceipt createGoodsReceipt(@RequestBody CreateReceiptRequest req) {
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        return goodsReceiptService.create(supplier, note, date);
    }

    @Operation(summary = "Position zu Wareneingang hinzufuegen")
    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceiptItem addItem(@PathVariable Long id, @RequestBody AddItemRequest req) {
        try {
            ArticleInfo article = articleInfoService.findById(req.articleId());
            return goodsReceiptService.addItemToReceipt(id, article, req.expectedQty(), req.actualQty(), req.defectNotes());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Position aktualisieren")
    @PutMapping("/{id}/items/{itemId}")
    public GoodsReceiptItem updateItem(@PathVariable Long id, @PathVariable Long itemId,
                                        @RequestBody UpdateItemRequest req) {
        try {
            return goodsReceiptService.updateItem(itemId, req.actualQty(), req.defectNotes());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Status einer Position setzen (FREIGEGEBEN / GESPERRT)")
    @PutMapping("/{id}/items/{itemId}/status")
    public GoodsReceiptItem setItemStatus(@PathVariable Long id, @PathVariable Long itemId,
                                           @RequestBody SetItemStatusRequest req) {
        GoodsReceiptItemStatus status;
        try {
            status = GoodsReceiptItemStatus.valueOf(req.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unbekannter Status: " + req.status() + ". Erlaubt: " +
                    java.util.Arrays.toString(GoodsReceiptItemStatus.values()));
        }
        try {
            return goodsReceiptService.setItemStatus(itemId, status);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Alle Positionen eines Wareneingangs freigeben")
    @PostMapping("/{id}/approve-all-items")
    public ResponseEntity<Integer> approveAllItems(@PathVariable Long id) {
        try {
            int approved = goodsReceiptService.approveAllItemsForReceipt(id);
            return ResponseEntity.ok(approved);
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.ok(0);
        }
    }

    @Operation(summary = "Pruefung abschliessen")
    @PostMapping("/{id}/complete")
    public GoodsReceipt completeInspection(@PathVariable Long id) {
        try {
            return goodsReceiptService.completeInspection(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (ObjectOptimisticLockingFailureException e) {
            return goodsReceiptService.getById(id);
        }
    }

    @Operation(summary = "Wareneingang loeschen")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoodsReceipt(@PathVariable Long id) {
        try {
            goodsReceiptService.deleteIfAllowed(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
```

- [ ] **Step 2: Delete old controller**

Delete: `src/main/java/com/example/application/api/load/LoadTestGoodsReceiptController.java`

- [ ] **Step 3: Verify build**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/application/api/wareneingang/
git rm src/main/java/com/example/application/api/load/LoadTestGoodsReceiptController.java
git commit -m "feat: replace LoadTestGoodsReceiptController with WareneingangController (/api/wareneingaenge)"
```

---

## Task 5: Remaining Domain Controllers

**Files:**
- Create: `src/main/java/com/example/application/api/lagerplatz/LagerplatzController.java`
- Create: `src/main/java/com/example/application/api/kontingent/KontingentController.java`
- Create: `src/main/java/com/example/application/api/nachbestellung/NachbestellungController.java`
- Create: `src/main/java/com/example/application/api/neueartikel/NeueArtikelController.java`
- Create: `src/main/java/com/example/application/api/health/HealthController.java`
- Delete: remaining 4 load controllers

- [ ] **Step 1: Create LagerplatzController.java**

```java
package com.example.application.api.lagerplatz;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import com.example.application.services.StorageLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lagerplaetze")
@Tag(name = "Lagerplaetze", description = "Lagerplatzverwaltung")
public class LagerplatzController {

    private final StorageLocationService storageLocationService;
    private final StorageLocationRepository storageLocationRepository;

    public LagerplatzController(StorageLocationService storageLocationService,
                                StorageLocationRepository storageLocationRepository) {
        this.storageLocationService = storageLocationService;
        this.storageLocationRepository = storageLocationRepository;
    }

    @Operation(summary = "Alle Lagerplaetze abrufen")
    @GetMapping
    public List<StorageLocation> storageLocations() {
        return storageLocationService.findAll();
    }

    @Operation(summary = "Neuen Lagerplatz anlegen")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageLocation createStorageLocation(@RequestBody StorageLocation storageLocation) {
        try {
            storageLocation.setId(null);
            return storageLocationService.saveWithDuplicateCheck(storageLocation);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Operation(summary = "Lagerplatz loeschen")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStorageLocation(@PathVariable Long id) {
        StorageLocation existing = storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "StorageLocation " + id + " nicht gefunden"));
        try {
            storageLocationService.delete(existing);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Operation(summary = "Lagerplatz-Status mit Artikeln synchronisieren")
    @PostMapping("/sync")
    public Map<String, Integer> syncStorageLocations() {
        int changed = storageLocationService.syncStatusesWithArticles();
        return Map.of("updated", changed);
    }
}
```

- [ ] **Step 2: Create KontingentController.java**

```java
package com.example.application.api.kontingent;

import com.example.application.data.contingent.ContingentLasttest;
import com.example.application.data.contingent.ContingentLasttestRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/kontingente")
@Tag(name = "Kontingente", description = "Kontingent-Simulation fuer Lasttests")
public class KontingentController {

    private static final Logger logger = LoggerFactory.getLogger(KontingentController.class);
    private static final int CHUNK_SIZE = 500;

    private final ContingentLasttestRepository contingentLasttestRepository;
    private final MessagingEventService messagingEventService;

    public KontingentController(ContingentLasttestRepository contingentLasttestRepository,
                                MessagingEventService messagingEventService) {
        this.contingentLasttestRepository = contingentLasttestRepository;
        this.messagingEventService = messagingEventService;
    }

    record SimulationResult(int requested, int created, int existingArticleMessages,
                            int newArticleMessages, int skippedNewArticle, String info) {}

    private static final String[] ARTIKEL_KATEGORIEN = {
            "Bio-Vollmilch", "Haferflocken", "Mineralwasser", "Orangensaft", "Roggenbrot",
            "Camembert", "Rinderhackfleisch", "Lachsfilet", "Olivenoel", "Basmati-Reis",
            "Tomatenmark", "Griechischer Joghurt", "Cashewkerne", "Dinkelnudeln", "Erdbeerkonfituere"
    };

    @Operation(summary = "Simulierte Kontingent-Nachrichten generieren")
    @PostMapping("/simulate")
    public SimulationResult simulate(@RequestParam(defaultValue = "5000") int count) {
        if (count <= 0 || count > 100_000) {
            throw new IllegalArgumentException("count muss zwischen 1 und 100.000 liegen");
        }
        logger.info("Starte Kontingent-Simulation: {} neue SIM-Artikel", count);
        Random random = new Random();
        List<ContingentLasttest> contingents = new ArrayList<>(count);
        List<MessagingEvent> events = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int menge = 10 + random.nextInt(491);
            contingents.add(buildSyntheticNewArticle(menge, random));
            events.add(buildEvent(-(i + 1L), menge));
        }
        for (int i = 0; i < contingents.size(); i += CHUNK_SIZE) {
            contingentLasttestRepository.saveAll(contingents.subList(i, Math.min(i + CHUNK_SIZE, contingents.size())));
        }
        for (int i = 0; i < events.size(); i += CHUNK_SIZE) {
            messagingEventService.saveAll(events.subList(i, Math.min(i + CHUNK_SIZE, events.size())));
        }
        logger.info("Kontingent-Simulation abgeschlossen: {} SIM-Artikel angelegt", count);
        return new SimulationResult(count, count, 0, count, 0,
                count + " synthetische neue Artikel (SIM-XXXXX) generiert.");
    }

    @Operation(summary = "Alle simulierten Kontingente loeschen")
    @DeleteMapping("/simulate")
    @Transactional
    public Map<String, Object> reset() {
        long contingentCount = contingentLasttestRepository.count();
        contingentLasttestRepository.deleteAllBulk();
        return Map.of("deletedContingents", contingentCount,
                "info", "Alle Kontingente geloescht. MessagingEvents bleiben als Audit-Log erhalten.");
    }

    private ContingentLasttest buildSyntheticNewArticle(int menge, Random random) {
        ContingentLasttest c = new ContingentLasttest();
        c.setArticleId(-(long) (random.nextInt(900_000) + 100_000));
        c.setAvailableQuantity(menge);
        String number = "SIM-" + String.format("%05d", random.nextInt(99_999) + 1);
        String name = ARTIKEL_KATEGORIEN[random.nextInt(ARTIKEL_KATEGORIEN.length)]
                + " " + (100 + random.nextInt(900)) + "g";
        c.setSimArticleNumber(number);
        c.setSimArticleName(name);
        return c;
    }

    private MessagingEvent buildEvent(long articleId, int menge) {
        return new MessagingEvent("NewQuota", articleId, (long) menge, "Lasttest-Simulation");
    }
}
```

- [ ] **Step 3: Create NachbestellungController.java**

```java
package com.example.application.api.nachbestellung;

import com.example.application.data.articleInfo.RestockItem;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import com.example.application.services.RestockOrderService;
import com.example.application.services.RestockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nachbestellungen")
@Tag(name = "Nachbestellungen", description = "Nachbestellung und Restock-Verwaltung")
public class NachbestellungController {

    private final RestockService restockService;
    private final RestockOrderService restockOrderService;
    private final RestockOrderRepository restockOrderRepository;

    public NachbestellungController(RestockService restockService,
                                    RestockOrderService restockOrderService,
                                    RestockOrderRepository restockOrderRepository) {
        this.restockService = restockService;
        this.restockOrderService = restockOrderService;
        this.restockOrderRepository = restockOrderRepository;
    }

    @Operation(summary = "Artikel unter Mindestbestand abrufen")
    @GetMapping
    public List<RestockItem> restock() {
        return restockService.getArticlesToRestock();
    }

    @Operation(summary = "Naechsten bestellbaren Artikel bestellen")
    @PostMapping("/approve-next")
    @Transactional
    public ResponseEntity<RestockOrder> approveNextRestock() {
        int orphansDeleted = restockOrderRepository.deleteOrphanedSimOrders();
        System.out.println("[approve-next] orphansDeleted=" + orphansDeleted);
        List<RestockItem> items = restockService.getArticlesToRestock();
        for (RestockItem item : items) {
            if (item.getOrderAmount() == null || item.getOrderAmount() <= 0) continue;
            if (restockOrderService.hasOpenOrderForArticle(item.getArticle())) continue;
            try {
                RestockOrder order = restockOrderService.approveOrderForLoadTest(item);
                return ResponseEntity.ok(order);
            } catch (Exception e) {
                System.out.println("[approve-next] SKIP " + item.getArticleNumber() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alle nachbestellbaren Artikel genehmigen")
    @PostMapping("/approve-all")
    public ResponseEntity<Integer> approveAllRestock() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        int approved = 0;
        for (RestockItem item : items) {
            if (item.getOrderAmount() == null || item.getOrderAmount() <= 0) continue;
            if (restockOrderService.hasOpenOrderForArticle(item.getArticle())) continue;
            try {
                restockOrderService.approveOrderForLoadTest(item);
                approved++;
            } catch (IllegalStateException ignored) {
            } catch (Exception e) {
                System.out.println("[approve-all] SKIP " + item.getArticleNumber() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(approved);
    }
}
```

> **Note:** `RestockItem` is in `com.example.application.data.articleInfo` (verified from existing controllers).

- [ ] **Step 4: Create NeueArtikelController.java**

```java
package com.example.application.api.neueartikel;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.MessagingEventService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.StorageLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/neue-artikel")
@Tag(name = "Neue Artikel", description = "Neue Artikel aus Kontingenten anlegen")
public class NeueArtikelController {

    private final ArticleSyncService articleSyncService;
    private final MessagingEventService messagingEventService;
    private final StorageLocationService storageLocationService;

    public NeueArtikelController(ArticleSyncService articleSyncService,
                                 MessagingEventService messagingEventService,
                                 StorageLocationService storageLocationService) {
        this.articleSyncService = articleSyncService;
        this.messagingEventService = messagingEventService;
        this.storageLocationService = storageLocationService;
    }

    @Operation(summary = "Neue Artikel aus Kontingenten abrufen")
    @GetMapping
    public List<NewArticleCandidate> newArticles(
            @RequestParam(defaultValue = "false") boolean lasttest) {
        return lasttest
                ? articleSyncService.findNewArticlesFromLasttestContingents()
                : articleSyncService.findNewArticlesFromContingents();
    }

    @Operation(summary = "Naechsten neuen Artikel anlegen")
    @PostMapping("/create-next")
    public ResponseEntity<ArticleInfo> createNextNewArticle() {
        try {
            Optional<ArticleInfo> result = articleSyncService.createNextFromLasttest();
            return result.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Naechsten Artikel anlegen und Lagerplatz zuweisen")
    @PostMapping("/create-next-with-storage")
    public ResponseEntity<ArticleInfo> createNextNewArticleWithStorage() {
        long remaining = articleSyncService.countNewArticlesFromLasttest();
        if (remaining == 0) {
            return ResponseEntity.noContent().build();
        }
        List<StorageLocation> available = storageLocationService.findAllAvailable();
        if (available.isEmpty()) {
            if (articleSyncService.countNewArticlesFromLasttest() == 0) {
                return ResponseEntity.noContent().build();
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Keine freien Lagerplatze verfugbar - bitte zuerst Lagerplatze anlegen.");
        }
        Collections.shuffle(available);
        for (StorageLocation loc : available) {
            try {
                loc.setStorageStatus("Used");
                storageLocationService.save(loc);
                Optional<ArticleInfo> result = articleSyncService.createNextWithStorageLocation(loc.getGeneralId());
                if (result.isPresent()) {
                    return ResponseEntity.ok(result.get());
                }
                loc.setStorageStatus("Available");
                storageLocationService.save(loc);
                if (articleSyncService.countNewArticlesFromLasttest() > 0) {
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.noContent().build();
            } catch (ObjectOptimisticLockingFailureException e) {
                // anderer Thread hat diesen Lagerplatz belegt - naechsten probieren
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Keine freien Lagerplatze verfugbar - bitte zuerst Lagerplatze anlegen.");
    }

    @Operation(summary = "Alle Messaging-Events abrufen")
    @GetMapping("/messaging-events")
    public List<MessagingEvent> messagingEvents() {
        return messagingEventService.findAll();
    }
}
```

- [ ] **Step 5: Create HealthController.java**

```java
package com.example.application.api.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Service-Status")
public class HealthController {

    @Operation(summary = "Service-Status pruefen")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "logistik");
    }
}
```

- [ ] **Step 6: Delete all remaining old load controllers**

```bash
git rm src/main/java/com/example/application/api/load/LoadTestStorageController.java
git rm src/main/java/com/example/application/api/load/LoadTestContingentController.java
git rm src/main/java/com/example/application/api/load/LoadTestHealthController.java
git rm src/main/java/com/example/application/api/load/LoadTestMiscController.java
```

- [ ] **Step 7: Verify build and Swagger UI**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS

Start the service, open `http://localhost:8081/swagger-ui.html`. Expected: All 8 controller groups visible (Artikels, Kommissionen, Wareneingaenge, Lagerplaetze, Kontingente, Nachbestellungen, Neue Artikel, Health).

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/example/application/api/lagerplatz/
git add src/main/java/com/example/application/api/kontingent/
git add src/main/java/com/example/application/api/nachbestellung/
git add src/main/java/com/example/application/api/neueartikel/
git add src/main/java/com/example/application/api/health/
git commit -m "feat: add remaining domain-sliced controllers, delete all load controllers"
```

---

## Task 6: Observer Pattern — Spring ApplicationEvent

**Files:**
- Create: `src/main/java/com/example/application/events/GoodsReceiptApprovedEvent.java`
- Create: `src/main/java/com/example/application/events/RestockCheckListener.java`
- Modify: `src/main/java/com/example/application/services/GoodsReceiptService.java`

This demonstrates the Observer pattern: `GoodsReceiptService` publishes an event when a receipt is approved. `RestockCheckListener` reacts independently — GoodsReceiptService doesn't know it exists.

- [ ] **Step 1: Create GoodsReceiptApprovedEvent.java**

```java
package com.example.application.events;

import org.springframework.context.ApplicationEvent;

public class GoodsReceiptApprovedEvent extends ApplicationEvent {

    private final Long receiptId;

    public GoodsReceiptApprovedEvent(Object source, Long receiptId) {
        super(source);
        this.receiptId = receiptId;
    }

    public Long getReceiptId() {
        return receiptId;
    }
}
```

- [ ] **Step 2: Create RestockCheckListener.java**

```java
package com.example.application.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RestockCheckListener {

    private static final Logger log = LoggerFactory.getLogger(RestockCheckListener.class);

    @EventListener
    public void onGoodsReceiptApproved(GoodsReceiptApprovedEvent event) {
        log.info("[Observer] Wareneingang {} abgeschlossen - Nachbestellpruefung ausgeloest", event.getReceiptId());
        // Hier koennte eine automatische Nachbestellpruefung ausgeloest werden.
        // Durch den Observer-Pattern bleibt GoodsReceiptService von RestockService entkoppelt.
    }
}
```

- [ ] **Step 3: Inject ApplicationEventPublisher into GoodsReceiptService**

Open `src/main/java/com/example/application/services/GoodsReceiptService.java`.

Add field and constructor injection for `ApplicationEventPublisher`:

```java
// Import hinzufuegen:
import com.example.application.events.GoodsReceiptApprovedEvent;
import org.springframework.context.ApplicationEventPublisher;
```

Add field to the class:
```java
private final ApplicationEventPublisher eventPublisher;
```

Extend the constructor to include `ApplicationEventPublisher eventPublisher` as last parameter and assign `this.eventPublisher = eventPublisher;`.

- [ ] **Step 4: Publish event at end of completeInspection()**

Find the `completeInspection(Long id)` method in `GoodsReceiptService`. At the point where the receipt is saved/returned (before the return statement), add:

```java
eventPublisher.publishEvent(new GoodsReceiptApprovedEvent(this, id));
```

- [ ] **Step 5: Verify build**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Test the event fires**

Start the service. Use Swagger UI at `http://localhost:8081/swagger-ui.html`:
1. Create a goods receipt via `POST /api/wareneingaenge`
2. Call `POST /api/wareneingaenge/{id}/complete`

Expected in console log: `[Observer] Wareneingang {id} abgeschlossen - Nachbestellpruefung ausgeloest`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/application/events/
git add src/main/java/com/example/application/services/GoodsReceiptService.java
git commit -m "feat: add Observer pattern via Spring ApplicationEvent (GoodsReceiptApproved)"
```

---

## Task 7: Gateway — Project Setup

**Files:**
- Create: `gateway/pom.xml`
- Create: `gateway/src/main/java/com/example/gateway/GatewayApplication.java`
- Create: `gateway/src/main/resources/application.yml`
- Create: `gateway/src/main/java/com/example/gateway/filter/LoggingFilter.java`

The gateway is a **standalone Spring Boot 3.4.5 project** in the `gateway/` subfolder of the same Git repo. It does NOT share the parent pom.xml — it has its own.

- [ ] **Step 1: Create gateway/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
    </parent>

    <groupId>com.example.gateway</groupId>
    <artifactId>logistik-gateway</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>logistik-gateway</name>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2024.0.1</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create GatewayApplication.java**

Create directories: `gateway/src/main/java/com/example/gateway/`

```java
package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

- [ ] **Step 3: Create application.yml**

Create `gateway/src/main/resources/application.yml`:

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

- [ ] **Step 4: Create LoggingFilter.java**

Create `gateway/src/main/java/com/example/gateway/filter/LoggingFilter.java`:

```java
package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest req = exchange.getRequest();
        log.info(">> {} {}", req.getMethod(), req.getURI().getPath());
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - start;
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;
            log.info("<< {} {} {} {}ms", req.getMethod(), req.getURI().getPath(), status, duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

- [ ] **Step 5: Build the gateway**

```bash
cd gateway
mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Start gateway and verify routing**

In a terminal, start the Logistik-Service first (port 8081). Then start the gateway:

```bash
cd gateway
mvn spring-boot:run
```

Open `http://localhost:8080/api/health`. Expected: `{"status":"UP","service":"logistik"}`

Check gateway console for log lines like:
```
>> GET /api/health
<< GET /api/health 200 12ms
```

Open `http://localhost:8080/swagger-ui.html`. Expected: Swagger UI from Logistik-Service served through gateway.

- [ ] **Step 7: Create RateLimitingFilter.java**

Create `gateway/src/main/java/com/example/gateway/filter/RateLimitingFilter.java`:

```java
package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final int MAX_REQUESTS_PER_SECOND = 10;

    // IP -> (windowStartMs, requestCount)
    private final ConcurrentHashMap<String, long[]> counters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        long now = System.currentTimeMillis();
        long[] window = counters.compute(ip, (k, v) -> {
            if (v == null || now - v[0] > 1000) {
                return new long[]{now, 1};
            }
            v[1]++;
            return v;
        });

        if (window[1] > MAX_REQUESTS_PER_SECOND) {
            log.warn("[RateLimit] IP {} ueberschreitet Limit ({} req/s)", ip, MAX_REQUESTS_PER_SECOND);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
```

- [ ] **Step 8: Verify both filters in gateway**

Restart gateway (`mvn spring-boot:run` in `gateway/`). Call `GET http://localhost:8080/api/health` mehrmals schnell hintereinander (z.B. mit `for i in {1..15}; do curl -s http://localhost:8080/api/health; done`).

Expected: Nach 10 Requests pro Sekunde antwortet Gateway mit HTTP 429.

- [ ] **Step 9: Commit**

```bash
cd ..
git add gateway/
git commit -m "feat: add Spring Cloud Gateway with LoggingFilter and RateLimitingFilter (10 req/s)"
```

---

## Task 8: Update docker-compose.yml

**Files:**
- Modify: `docker-compose.yml`

- [ ] **Step 1: Update docker-compose.yml**

Replace the contents of `docker-compose.yml` with:

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
    environment:
      - SPRING_CLOUD_GATEWAY_ROUTES_0_URI=http://host.docker.internal:8081
    extra_hosts:
      - "host.docker.internal:host-gateway"
    depends_on:
      db:
        condition: service_healthy

volumes:
  pgdata:
```

> **Note on gateway routing in Docker:** When running the gateway in Docker but the Logistik-Service locally (IntelliJ), use `host.docker.internal` as the host. The `SPRING_CLOUD_GATEWAY_ROUTES_0_URI` env var overrides the `localhost:8081` in application.yml for Docker context. For a fully containerized setup both services need their own Dockerfiles.

- [ ] **Step 2: Create gateway/Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    apt-get update && apt-get install -y maven && \
    mvn package -q -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/logistik-gateway-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml gateway/Dockerfile
git commit -m "feat: add gateway service to docker-compose with host routing"
```

---

## Task 9: Update JMeter Tests

**Files:** Existing JMeter `.jmx` files (location: check `jmeter.home` config or project root)

- [ ] **Step 1: Find JMeter test files**

```bash
find . -name "*.jmx" -not -path "./.git/*"
```

- [ ] **Step 2: Update all URLs**

For each `.jmx` file, replace:
- Port `8080` with `8080` (unchanged — gateway now on 8080, same port as before)
- Path prefix `/api/load/articles` → `/api/artikels`
- Path prefix `/api/load/kommissionen` → `/api/kommissionen`
- Path prefix `/api/load/goods-receipts` → `/api/wareneingaenge`
- Path prefix `/api/load/storage-locations` → `/api/lagerplaetze`
- Path prefix `/api/load/contingents` → `/api/kontingente`
- Path prefix `/api/load/restock` → `/api/nachbestellungen`
- Path prefix `/api/load/new-articles` → `/api/neue-artikel`
- Path prefix `/api/load/health` → `/api/health`
- Path prefix `/api/load/messaging-events` → `/api/neue-artikel/messaging-events`

> **Note:** The port stays 8080 — JMeter now goes through the gateway, which is architecturally the key change.

- [ ] **Step 3: Run a smoke test**

Start both services (Logistik-Service port 8081, Gateway port 8080). Run a small JMeter test (10 threads, 1 loop). Verify all requests return 2xx.

- [ ] **Step 4: Commit**

```bash
git add *.jmx  # or the actual path to your jmx files
git commit -m "test: update JMeter paths from /api/load/* to domain-sliced /api/* endpoints"
```

---

## Task 10: Final Verification and Load Test

- [ ] **Step 1: Start full stack**

1. Start PostgreSQL: `docker-compose up db`
2. Start Logistik-Service: Run via IntelliJ on port 8081
3. Start Gateway: `cd gateway && mvn spring-boot:run` (port 8080)

- [ ] **Step 2: Verify Swagger UI via Gateway**

Open `http://localhost:8080/swagger-ui.html`.
Expected: Swagger UI loads, all 8 domain groups visible, routed through gateway.

- [ ] **Step 3: Verify gateway logs**

Gateway console should show request logs for every call through Swagger UI:
```
>> GET /swagger-ui.html
<< GET /swagger-ui.html 200 5ms
```

- [ ] **Step 4: Run full JMeter load test**

Run the same test plan as the previous baseline. Capture screenshots of:
- Response times per endpoint
- Throughput (req/sec)
- Error rate
- Gateway log output (shows all routed requests)

Compare with previous baseline screenshots.

- [ ] **Step 5: Final commit**

```bash
git add .
git commit -m "feat: loose coupling implementation complete - OpenAPI, domain slicing, API Gateway, Observer pattern"
```
