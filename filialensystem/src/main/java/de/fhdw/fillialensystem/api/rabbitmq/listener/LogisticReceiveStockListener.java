package de.fhdw.fillialensystem.api.rabbitmq.listener;

import de.fhdw.commons.api.dto.LogisticMessageDTO;
import de.fhdw.commons.api.rabbitmq.DomainQueue;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import de.fhdw.fillialensystem.persistence.service.StoreLinkStockService;
import de.fhdw.fillialensystem.persistence.service.imported.ArticleService;
import de.fhdw.fillialensystem.utility.StoreClient;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LogisticReceiveStockListener {

    private static final Logger log = LoggerFactory.getLogger(LogisticReceiveStockListener.class);

    private final StoreClient storeClient;
    private final ArticleService articleService;
    private final StoreLinkStockService storeLinkStockService;

    public LogisticReceiveStockListener(StoreClient storeClient, ArticleService articleService, StoreLinkStockService storeLinkStockService) {
        this.storeClient = storeClient;
        this.articleService = articleService;
        this.storeLinkStockService = storeLinkStockService;
    }

    @RabbitListener(queues = "#{T(de.fhdw.commons.api.rabbitmq.DomainQueue).STORE_LOGISTIC_RESTOCK.getQueue()}")
    public void handleStock(LogisticMessageDTO logisticMessageDTO) {
        log.atInfo().log("[MESSAGE] Processing LogisticMessageDTO from queue [%s]...".formatted(DomainQueue.STORE_LOGISTIC_RESTOCK.getQueue()));
        try {
            validateDTO(logisticMessageDTO);

            if (storeClient.getStore() == null) {
                throw new IllegalStateException("Store not initialized yet");
            }

            if (!storeClient.getStore().getId().equals(logisticMessageDTO.getStoreId())) {
                return;
            }

            Article article = articleService.findById(logisticMessageDTO.getArticleId())
                    .orElseThrow(EntityNotFoundException::new);
            StoreLinkStock storeLinkStock = storeLinkStockService.findByStoreAndArticle(storeClient.getStore(), article)
                    .orElseThrow(EntityNotFoundException::new);
            storeLinkStock.setAmount(storeLinkStock.getAmount() + logisticMessageDTO.getQuantity().intValue());

            storeLinkStockService.update(storeLinkStock);
        } catch (Exception e) {
            log.atError().log("[MESSAGE] Error while processing LogisticMessageDTO from queue [%s]. Error: [%s]".formatted(DomainQueue.STORE_LOGISTIC_RESTOCK.getQueue(), e.getMessage()));
        }
        log.atInfo().log("[MESSAGE] Processed a LogisticMessageDTO from queue [%s]".formatted(DomainQueue.STORE_LOGISTIC_RESTOCK.getQueue()));
    }

    private void validateDTO(LogisticMessageDTO logisticMessageDTO) {
        if (logisticMessageDTO.getStoreId() == null || logisticMessageDTO.getStoreId() <= 0) {
            throw new IllegalStateException("DTOs Store ID is null or <= 0");
        }
        if (logisticMessageDTO.getArticleId() == null || logisticMessageDTO.getArticleId() <= 0) {
            throw new IllegalStateException("DTOs Article ID is null or <= 0");
        }
        if (logisticMessageDTO.getQuantity() == null || logisticMessageDTO.getQuantity() <= 0) {
            throw new IllegalStateException("DTOs Quantity is null or <= 0");
        }
    }
}