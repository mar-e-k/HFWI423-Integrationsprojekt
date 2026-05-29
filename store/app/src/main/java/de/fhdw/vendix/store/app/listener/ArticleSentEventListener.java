package de.fhdw.vendix.store.app.listener;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import de.fhdw.vendix.store.core.domain.store_stock_order.StoreStockOrderService;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
    private final StoreStockOrderService storeStockOrderService;

    private final Counter consumedCounter;
    private final Counter rejectedCounter;
    private final Counter restockAmountCounter;

    public ArticleSentEventListener(
            StoreContext storeContext,
            StoreStockService storeStockService,
            StoreStockOrderService storeStockOrderService,
            MeterRegistry meterRegistry
    ) {
        this.storeContext = storeContext;
        this.storeStockService = storeStockService;
        this.storeStockOrderService = storeStockOrderService;

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

        @Nullable StoreDTO currentStore = storeContext.getStore();
        @Nullable Long currentStoreId = currentStore == null ? null : currentStore.id();
        if (currentStoreId != null && !Objects.equals(event.storeId(), currentStoreId)) {
            log.atDebug().log("Ignoring ArticleSentEvent - wrong storeId: expected={}, got={}",
                    currentStoreId, event.storeId());
            rejectedCounter.increment();
            return;
        }

        storeStockService.restockArticle(
                event.storeId(),
                event.articleId(),
                event.articleAmount()
        );
        storeStockOrderService.markReceived(event.storeId(), event.articleId(), event.articleAmount());

        consumedCounter.increment();
        restockAmountCounter.increment(event.articleAmount());
    }
}
