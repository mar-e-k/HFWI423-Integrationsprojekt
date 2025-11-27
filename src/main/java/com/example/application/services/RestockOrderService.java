package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.article.RestockItem;
import com.example.application.data.contingent.Contingent;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RestockOrderService {

    private final RestockOrderRepository restockOrderRepository;
    private final ContingentRepository contingentRepository;

    public RestockOrderService(RestockOrderRepository restockOrderRepository,
                               ContingentRepository contingentRepository) {
        this.restockOrderRepository = restockOrderRepository;
        this.contingentRepository = contingentRepository;
    }

    // Prüfen, ob bereits eine Bestellung für diesen Artikel läuft
    public boolean hasOpenOrderForArticle(ArticleInfo article) {
        return restockOrderRepository.existsByArticle_IdAndDeliveredFalse(article.getId());
    }

    @Transactional
    public void approveOrder(RestockItem item) {

        ArticleInfo article = item.getArticle();
        Integer amount = item.getOrderAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalStateException("Keine gültige Nachbestellmenge für diesen Artikel.");
        }

        // Kontingent für diesen Artikel finden
        Contingent contingent = contingentRepository.findByArticle(article)
                .orElseThrow(() -> new IllegalStateException(
                        "Für diesen Artikel existiert kein Kontingent. (Einkauf hat noch nichts hinterlegt)"
                ));

        int available = contingent.getAvailableQuantity();

        if (available < amount) {
            throw new IllegalStateException(
                    "Nicht genug Kontingent verfügbar. Verfügbar: " + available
            );
        }

        // Kontingent reduzieren
        contingent.setAvailableQuantity(available - amount);
        contingentRepository.save(contingent);

        // Bestellung erzeugen
        RestockOrder order = new RestockOrder();
        order.setArticle(article);
        order.setQuantity(amount);
        order.setCreatedAt(LocalDateTime.now());
        order.setApproved(true);
        order.setDelivered(false);

        restockOrderRepository.save(order);
    }
}
