package de.fhdw.vendix.store.app.listener;

import de.fhdw.vendix.commons.spring.security.context.store.StoreContext;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ArticleSentEventListener {

    private static final Logger log = LoggerFactory.getLogger(ArticleSentEventListener.class);

    private final StoreContext storeContext;
    private final StoreStockService storeStockService;

    public ArticleSentEventListener(StoreContext storeContext, StoreStockService storeStockService) {
        this.storeContext = storeContext;
        this.storeStockService = storeStockService;
    }

    @AmqpEventListener
    public void onArticleSentEvent(ArticleSentEvent event) {
        if (event != null) {
            log.atInfo().log("onArticleSentEvent: {}", event);
        } else {
            log.atInfo().log("onArticleSentEvent received, but it was empty");
            throw new MessageRejectedException("Rejecting onArticleSentEvent, as there is no content to be handled");
        }
        if (storeContext.getStore() == null || storeContext.getStore().id() == null) {
            throw new IllegalStateException("Cannot handle event, as storeContext is not set properly");
        }
        if (event.storeId() != storeContext.getStore().id()) {
            throw new MessageRejectedException("Cannot handle event, wrong store received it");
        }
        storeStockService.restockArticle(
                storeContext.getStore().id(),
                event.articleId(),
                event.articleAmount()
        );
    }
}