package de.fhdw.vendix.store.app.listener;

import de.fhdw.vendix.store.core.store.StoreContext;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Consumes {@link ArticleSentEvent}s from the logistics application and
 * restocks the local store inventory accordingly.
 *
 * <p>The following Prometheus metrics are emitted:
 * <ul>
 *   <li>{@code vendix_article_sent_consumed_total} – events that were
 *       accepted and led to a successful restocking</li>‚
 *   <li>{@code vendix_article_sent_rejected_total} – events that were
 *       discarded (null payload or wrong storeId)</li>
 *   <li>{@code vendix_restock_total_amount} – total units added to stock
 *       across all accepted events</li>
 * </ul>
 */
@Service
public class ArticleSentEventListener {

    private static final Logger log = LoggerFactory.getLogger(ArticleSentEventListener.class);

    private final StoreContext storeContext;
    private final StoreStockService storeStockService;

    private final Counter consumedCounter;
    private final Counter rejectedCounter;
    private final Counter restockAmountCounter;

    public ArticleSentEventListener(
            StoreContext storeContext,
            StoreStockService storeStockService,
            MeterRegistry meterRegistry
    ) {
        this.storeContext = storeContext;
        this.storeStockService = storeStockService;

        this.consumedCounter = Counter.builder("vendix_article_sent_consumed_total")
                .description("Total number of ArticleSentEvents successfully processed")
                .register(meterRegistry);
        this.rejectedCounter = Counter.builder("vendix_article_sent_rejected_total")
                .description("Total number of ArticleSentEvents rejected (wrong store or empty payload)")
                .register(meterRegistry);
        this.restockAmountCounter = Counter.builder("vendix_restock_total_amount")
                .description("Total units added to store stock via ArticleSentEvents")
                .register(meterRegistry);
    }

    @AmqpEventListener
    public void onArticleSentEvent(ArticleSentEvent event) {
        if (event == null) {
            log.atInfo().log("onArticleSentEvent received, but it was empty");
            rejectedCounter.increment();
            throw new MessageRejectedException("Rejecting onArticleSentEvent, as there is no content to be handled");
        }

        log.atInfo().log("onArticleSentEvent: {}", event);

        if (storeContext.getStore() == null || storeContext.getStore().id() == null) {
            throw new IllegalStateException("Cannot handle event, as storeContext is not set properly");
        }

        if (event.storeId() != storeContext.getStore().id()) {
            log.atDebug().log("Rejecting ArticleSentEvent — wrong storeId: expected={}, got={}",
                    storeContext.getStore().id(), event.storeId());
            rejectedCounter.increment();
            throw new MessageRejectedException("Cannot handle event, wrong store received it");
        }

        storeStockService.restockArticle(
                storeContext.getStore().id(),
                event.articleId(),
                event.articleAmount()
        );

        consumedCounter.increment();
        restockAmountCounter.increment(event.articleAmount());
    }
}
