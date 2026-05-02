package com.example.application.services;

import com.example.application.amqp.einkaufEvents.EinkaufEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.articleInfo.RestockItem;
import com.example.application.data.contingent.Contingent;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RestockOrderService {
    private static final int LOW_CONTINGENT_THRESHOLD = 50;

    private final RestockOrderRepository restockOrderRepository;
    private final ContingentRepository contingentRepository;
    private final EinkaufEventPublisher einkaufEventPublisher;
    private final ArticleInfoRepository articleInfoRepository;

    public RestockOrderService(RestockOrderRepository restockOrderRepository,
                               ContingentRepository contingentRepository,
                               EinkaufEventPublisher einkaufEventPublisher,
                               ArticleInfoRepository articleInfoRepository) {
        this.restockOrderRepository = restockOrderRepository;
        this.contingentRepository = contingentRepository;
        this.einkaufEventPublisher = einkaufEventPublisher;
        this.articleInfoRepository = articleInfoRepository;
    }

    /**
     * Vereinfachte Nachbestellung fuer den Lasttest – ohne Kontingent-Pruefung.
     * Lasttest-Artikel haben keine echten Contingent-Eintraege.
     * REQUIRES_NEW: jeder Versuch hat eine eigene Transaktion, damit ein Fehler
     * fuer einen Artikel nicht die aeussere Transaktion als rollback-only markiert.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RestockOrder approveOrderForLoadTest(RestockItem item) {
        ArticleInfo article = item.getArticle();

        // Pessimistischer Lock auf den Artikel-Datensatz – serialisiert konkurrierende Threads
        // pro Artikel. Erst wenn Thread A committed (Lock frei), kann Thread B weitermachen
        // und sieht dann die bereits vorhandene Bestellung.
        articleInfoRepository.findByIdForUpdate(article.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Artikel nicht gefunden: " + article.getId()));

        // Nochmals pruefen (innerhalb des Locks): existiert schon eine offene Bestellung?
        if (restockOrderRepository.existsByArticleNumberAndDeliveredFalse(article.getArticleNumber())) {
            throw new IllegalStateException(
                    "Offene Bestellung fuer " + article.getArticleNumber() + " existiert bereits");
        }

        Integer palletsToOrder = item.getOrderAmount();
        if (palletsToOrder == null || palletsToOrder <= 0) {
            throw new IllegalStateException("Keine gueltige Nachbestellmenge fuer diesen Artikel.");
        }

        Integer piecesPerPallet = article.getPiecesPerPallet();
        if (piecesPerPallet == null || piecesPerPallet <= 0) {
            throw new IllegalStateException("Artikel hat keinen gueltigen Wert fuer pieces_per_pallet.");
        }

        int piecesToOrder = palletsToOrder * piecesPerPallet;

        RestockOrder order = new RestockOrder();
        order.setArticleNumber(article.getArticleNumber());
        order.setArticleName(article.getName());
        order.setQuantity(piecesToOrder);
        order.setCreatedAt(LocalDateTime.now());
        order.setApproved(true);
        order.setDelivered(false);

        return restockOrderRepository.save(order);
    }

    /**
     * Bulk-Freigabe aller uebergebenen Items in 4 Queries statt N*3.
     * Query 1: IN-Abfrage welche Artikel bereits offene Bestellungen haben.
     * Query 2: Alle Kontingente der validen Artikel in einem Batch laden.
     * Query 3: saveAll() fuer alle geaenderten Kontingente.
     * Query 4: saveAll() fuer alle neuen RestockOrders.
     * Items ohne ausreichendes Kontingent werden still uebersprungen.
     */
    @Transactional
    public void approveAllOrders(List<RestockItem> items) {
        List<RestockItem> validItems = items.stream()
                .filter(i -> i.getOrderAmount() != null && i.getOrderAmount() > 0)
                .filter(i -> i.getArticle().getPiecesPerPallet() != null && i.getArticle().getPiecesPerPallet() > 0)
                .toList();

        if (validItems.isEmpty()) return;

        // Query 1: alle Artikelnummern mit offener Bestellung per IN-Query
        List<String> articleNumbers = validItems.stream()
                .map(i -> i.getArticle().getArticleNumber())
                .toList();
        Set<String> alreadyOrdered = new HashSet<>(
                restockOrderRepository.findOpenOrderArticleNumbers(articleNumbers));

        // Nur Items ohne offene Bestellung weiterverarbeiten; contingentKey berechnen
        Map<Long, RestockItem> contingentKeyToItem = new LinkedHashMap<>();
        for (RestockItem item : validItems) {
            ArticleInfo article = item.getArticle();
            if (alreadyOrdered.contains(article.getArticleNumber())) continue;

            Long contingentKey;
            if (article.getArticleId() != null && article.getArticleId() != 0L) {
                contingentKey = article.getArticleId();
            } else {
                try {
                    contingentKey = Long.valueOf(article.getArticleNumber());
                } catch (NumberFormatException e) {
                    continue; // Lasttest-Artikel ohne Kontingent ueberspringen
                }
            }
            contingentKeyToItem.put(contingentKey, item);
        }

        if (contingentKeyToItem.isEmpty()) return;

        // Query 2: alle Kontingente der validen Artikel in einem Batch laden
        List<Contingent> allContingents =
                contingentRepository.findAllByArticleIdIn(contingentKeyToItem.keySet());
        Map<Long, List<Contingent>> contingentsByKey = allContingents.stream()
                .collect(Collectors.groupingBy(Contingent::getArticleId));

        List<Contingent> contingentsToSave = new ArrayList<>();
        List<RestockOrder> ordersToSave = new ArrayList<>();
        List<Long> lowStockKeys = new ArrayList<>();

        for (Map.Entry<Long, RestockItem> entry : contingentKeyToItem.entrySet()) {
            Long contingentKey = entry.getKey();
            RestockItem item = entry.getValue();
            ArticleInfo article = item.getArticle();

            List<Contingent> contingents = contingentsByKey.getOrDefault(contingentKey, List.of());
            if (contingents.isEmpty()) continue;

            contingents.sort(Comparator.comparing(Contingent::getId));

            int piecesToOrder = item.getOrderAmount() * article.getPiecesPerPallet();
            int remainingToRemove = piecesToOrder;

            for (Contingent c : contingents) {
                if (remainingToRemove <= 0) break;
                int available = c.getAvailableQuantity();
                if (available >= remainingToRemove) {
                    c.setAvailableQuantity(available - remainingToRemove);
                    remainingToRemove = 0;
                } else {
                    c.setAvailableQuantity(0);
                    remainingToRemove -= available;
                }
                contingentsToSave.add(c);
            }

            if (remainingToRemove > 0) continue; // nicht genug Kontingent, Item ueberspringen

            // stockAfter in Memory berechnen statt extra DB-Query
            int stockAfter = contingents.stream().mapToInt(Contingent::getAvailableQuantity).sum();
            if (stockAfter < LOW_CONTINGENT_THRESHOLD) {
                lowStockKeys.add(contingentKey);
            }

            RestockOrder order = new RestockOrder();
            order.setArticleNumber(article.getArticleNumber());
            order.setArticleName(article.getName());
            order.setQuantity(piecesToOrder);
            order.setCreatedAt(LocalDateTime.now());
            order.setApproved(true);
            order.setDelivered(false);
            ordersToSave.add(order);
        }

        // Query 3 + 4: Batch-Saves
        contingentRepository.saveAll(contingentsToSave);
        restockOrderRepository.saveAll(ordersToSave);

        // Events fuer niedrigen Kontingentstand (keine DB-Queries)
        for (Long key : lowStockKeys) {
            einkaufEventPublisher.publishNewDeal(key);
        }
    }

    public boolean hasOpenOrderForArticle(ArticleInfo article) {
        return restockOrderRepository.existsByArticleNumberAndDeliveredFalse(
                article.getArticleNumber()
        );
    }

    @Transactional
    public void approveOrder(RestockItem item) {

        ArticleInfo article = item.getArticle();

        Integer palletsToOrder = item.getOrderAmount();
        if (palletsToOrder == null || palletsToOrder <= 0) {
            throw new IllegalStateException("Keine gültige Nachbestellmenge für diesen Artikel.");
        }

        Integer piecesPerPallet = article.getPiecesPerPallet();
        if (piecesPerPallet == null || piecesPerPallet <= 0) {
            throw new IllegalStateException("Artikel hat keinen gültigen Wert für pieces_per_pallet.");
        }

        int piecesToOrder = palletsToOrder * piecesPerPallet;

        // Schlüssel für das Kontingent bestimmen
        Long contingentKey;

        if (article.getArticleId() != null && article.getArticleId() != 0L) {
            // neue Welt: article_id in ArticleInfo/Contingent identisch
            contingentKey = article.getArticleId();
        } else {
            // Fallback für bestehende Daten:
            // Contingent.article_id entspricht der Artikelnummer
            try {
                contingentKey = Long.valueOf(article.getArticleNumber());
            } catch (NumberFormatException e) {
                throw new IllegalStateException(
                        "Artikel '" + article.getArticleNumber() + "' ist ein Lasttest-Artikel ohne Kontingent und kann nicht nachbestellt werden.");
            }
        }

        System.out.println("approveOrder → articleNumber=" + article.getArticleNumber()
                + ", articleId=" + article.getArticleId()
                + ", contingentKey=" + contingentKey);

        Integer stockBefore = contingentRepository.sumAvailableQuantityByArticleId(contingentKey); // NEU
        if (stockBefore == null) { // NEU
            stockBefore = 0; // NEU
        }
        List<Contingent> contingents =
                contingentRepository.findAllByArticleId(contingentKey);

        if (contingents.isEmpty()) {
            throw new IllegalStateException(
                    "Für diesen Artikel existiert kein Kontingent (Key=" + contingentKey + ").");
        }

        contingents.sort(Comparator.comparing(Contingent::getId));

        int remainingToRemove = piecesToOrder;

        for (Contingent c : contingents) {
            if (remainingToRemove <= 0) break;

            int available = c.getAvailableQuantity();

            if (available >= remainingToRemove) {
                c.setAvailableQuantity(available - remainingToRemove);
                remainingToRemove = 0;
            } else {
                c.setAvailableQuantity(0);
                remainingToRemove -= available;
            }

            contingentRepository.save(c);
        }

        if (remainingToRemove > 0) {
            throw new IllegalStateException(
                    "Nicht genug Kontingent verfügbar, es fehlen: " + remainingToRemove + " Stück."
            );
        }

        Integer stockAfter = contingentRepository.sumAvailableQuantityByArticleId(contingentKey); // NEU
        if (stockAfter == null) { // NEU
            stockAfter = 0; // NEU
        }

        if (stockAfter < LOW_CONTINGENT_THRESHOLD) {
            einkaufEventPublisher.publishNewDeal(contingentKey.longValue());
        }

        // 🌟 HIER wird die RestockOrder angelegt
        RestockOrder order = new RestockOrder();
        order.setArticleNumber(article.getArticleNumber());
        order.setArticleName(article.getName());
        order.setQuantity(piecesToOrder);        // in Stück
        order.setCreatedAt(LocalDateTime.now());
        order.setApproved(true);
        order.setDelivered(false);

        RestockOrder saved = restockOrderRepository.save(order);
        System.out.println("RestockOrder gespeichert, id=" + saved.getId());
    }
}
