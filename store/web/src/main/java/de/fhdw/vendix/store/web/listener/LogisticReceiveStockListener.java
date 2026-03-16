package de.fhdw.vendix.store.web.listener;

import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockCommandPort;
import de.fhdw.vendix.store.commons.context.StoreContext;
import io.github.plaguv.amqp.api.event.pos.LogisticArticleOrderEvent;
import io.github.plaguv.amqp.core.listener.AmqpListener;
import org.springframework.stereotype.Service;

@Service
public class LogisticReceiveStockListener {

    private final StoreContext storeContext;
    private final StoreStockCommandPort storeStockCommandPort;

    public LogisticReceiveStockListener(StoreContext storeContext, StoreStockCommandPort storeStockCommandPort) {
        this.storeContext = storeContext;
        this.storeStockCommandPort = storeStockCommandPort;
    }

    @AmqpListener
    public void receiveStock(LogisticArticleOrderEvent event) {
        if (event == null) {
            throw new NullPointerException("Received null event");
        }
        if (storeContext.getStore() == null || storeContext.getStore().id() == null) {
            throw new IllegalStateException("Cannot handle event, as storeContext is not set properly");
        }
        if (event.storeId() != storeContext.getStore().id()) {
            throw new IllegalStateException("Cannot handle event, wrong store received it");
        }
        storeStockCommandPort.restockArticle(
                storeContext.getStore().id(),
                event.articleId(),
                event.quantity()
        );
    }
}