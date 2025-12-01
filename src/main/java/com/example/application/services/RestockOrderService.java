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
import java.util.Comparator;
import java.util.List;

@Service
public class RestockOrderService {

    private final RestockOrderRepository restockOrderRepository;
    private final ContingentRepository contingentRepository;

    public RestockOrderService(RestockOrderRepository restockOrderRepository,
                               ContingentRepository contingentRepository) {
        this.restockOrderRepository = restockOrderRepository;
        this.contingentRepository = contingentRepository;
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

        Long articleNumberAsLong = Long.valueOf(article.getArticleNumber());

        List<Contingent> contingents =
                contingentRepository.findAllByArticleId(articleNumberAsLong);

        if (contingents.isEmpty()) {
            throw new IllegalStateException("Für diesen Artikel existiert kein Kontingent.");
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

        // Bestellung speichern — NEUE Version
        RestockOrder order = new RestockOrder();
        order.setArticleNumber(article.getArticleNumber());
        order.setArticleName(article.getName());
        order.setQuantity(piecesToOrder);
        order.setCreatedAt(LocalDateTime.now());
        order.setApproved(true);
        order.setDelivered(false);

        restockOrderRepository.save(order);
    }
}
