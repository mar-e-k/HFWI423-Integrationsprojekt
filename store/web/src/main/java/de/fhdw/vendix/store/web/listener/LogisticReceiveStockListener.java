package de.fhdw.vendix.store.web.listener;

import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockCommandPort;
import de.fhdw.vendix.store.api.context.StoreContext;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogisticReceiveStockListener {

    private static final Logger log = LoggerFactory.getLogger(LogisticReceiveStockListener.class);

    private final StoreContext storeContext;
    private final StoreStockCommandPort storeStockCommandPort;

    public LogisticReceiveStockListener(StoreContext storeContext, StoreStockCommandPort storeStockCommandPort) {
        this.storeContext = storeContext;
        this.storeStockCommandPort = storeStockCommandPort;
    }

    @AmqpEventListener
    public void onArticleSentEvent(@Nonnull ArticleSentEvent event) {
        log.atInfo().log("onArticleSentEvent");
//        if (storeContext.getStore() == null || storeContext.getStore().id() == null) {
//            throw new IllegalStateException("Cannot handle event, as storeContext is not set properly");
//        }
//        if (event.storeId() != storeContext.getStore().id()) {
//            throw new IllegalStateException("Cannot handle event, wrong store received it");
//        }
//        storeStockCommandPort.restockArticle(
//                storeContext.getStore().id(),
//                event.articleId(),
//                event.quantity()
//        );
    }
}