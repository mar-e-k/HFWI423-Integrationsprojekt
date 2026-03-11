package de.fhdw.vendix.store.application.listener;

import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockCommandPort;
import de.fhdw.vendix.store.application.context.StoreContext;
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
        if (event.storeId() != storeContext.getStore().id()) {
            throw new IllegalStateException("Wrong store");
        }
        storeStockCommandPort.restockArticle(
                storeContext.getStore().id(),
                event.articleId(),
                event.quantity()
        );
    }
}