package de.fhdw.vendix.store.web.listener;

import de.fhdw.vendix.security.api.context.StoreContext;
import de.fhdw.vendix.store.core.persistance.store_stock.port.StoreStockService;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogisticReceiveStockListener {

    private static final Logger log = LoggerFactory.getLogger(LogisticReceiveStockListener.class);

    private final StoreContext storeContext;
    private final StoreStockService storeStockPort;

    public LogisticReceiveStockListener(StoreContext storeContext, StoreStockService storeStockPort) {
        this.storeContext = storeContext;
        this.storeStockPort = storeStockPort;
    }

    @AmqpEventListener
    public void onArticleSentEvent(ArticleSentEvent event) {
        log.atInfo().log("onArticleSentEvent");
        if (event != null) {
            log.atInfo().log("onArticleSentEvent: {}", event);
        } else {
            log.atInfo().log("onArticleSentEvent received, but it was empty");
        }
//        log.atInfo().log(event.toString());
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