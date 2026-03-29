package com.example.application.services;

import com.example.application.amqp.einkaufEvents.EinkaufEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.RestockItem;
import com.example.application.data.contingent.Contingent;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class RestockOrderService {
    private static final int LOW_CONTINGENT_THRESHOLD = 50;

    private final RestockOrderRepository restockOrderRepository;
    private final ContingentRepository contingentRepository;
    private final EinkaufEventPublisher einkaufEventPublisher;

    public RestockOrderService(RestockOrderRepository restockOrderRepository,
                               ContingentRepository contingentRepository,
                               EinkaufEventPublisher einkaufEventPublisher) {
        this.restockOrderRepository = restockOrderRepository;
        this.contingentRepository = contingentRepository;
        this.einkaufEventPublisher = einkaufEventPublisher;
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
            contingentKey = Long.valueOf(article.getArticleNumber());
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
