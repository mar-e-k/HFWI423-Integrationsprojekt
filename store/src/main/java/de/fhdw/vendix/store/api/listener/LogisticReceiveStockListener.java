package de.fhdw.vendix.store.api.listener;

import de.fhdw.vendix.store.persistence.entity.StoreLinkStock;
import de.fhdw.vendix.store.persistence.entity.imported.Article;
import de.fhdw.vendix.store.persistence.service.StoreLinkStockService;
import de.fhdw.vendix.store.persistence.service.imported.ArticleService;
import de.fhdw.vendix.store.utility.StoreClient;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @AmqpEventListener
    public void handleStock(ArticleSentEvent articleSentEvent) {
        log.atInfo().log("[MESSAGE] Processing ArticleSentEvent");
        if (articleSentEvent == null) {
            log.atWarn().log("[MESSAGE] Rejecting Message, as ArticleSentEvent is empty");
            throw new MessageRejectedException("Message cannot be processed, as contents are empty/null");
        }
        try {
            if (storeClient.getStore() == null) {
                throw new IllegalStateException("Store not initialized yet");
            }

            if (!storeClient.getStore().getId().equals(articleSentEvent.storeId())) {
                log.atWarn().log("[MESSAGE] Message cannot be processed, as the stores do not match up. {} vs {}",
                        storeClient.getStore().getId(),
                        articleSentEvent.storeId()
                );
                throw new MessageRejectedException("Message cannot be processed, as the stores do not match up");
            }

            Article article = articleService.findById(articleSentEvent.articleId())
                    .orElseThrow(EntityNotFoundException::new);
            StoreLinkStock storeLinkStock = storeLinkStockService.findByStoreAndArticle(storeClient.getStore(), article)
                    .orElseThrow(EntityNotFoundException::new);
            storeLinkStock.setAmount(storeLinkStock.getAmount() + (int) articleSentEvent.articleAmount());

            storeLinkStockService.update(storeLinkStock);
        } catch (Exception e) {
            log.atError().log("[MESSAGE] cannot process message. exception : {}", e.getMessage(), e);
        }
        log.atInfo().log("[MESSAGE] Successfully processed ArticleSentEvent");
    }
}