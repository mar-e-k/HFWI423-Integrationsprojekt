package de.fhdw.vendix.store.app.messaging;

import de.fhdw.vendix.store.core.messaging.ArticleOrderMessagingService;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * AMQP-backed implementation of {@link ArticleOrderMessagingService}.
 *
 * <p>All AMQP library types ({@link EventPublisher}, envelope builders, event
 * records) are confined to this class. The rest of the application only knows
 * the plain interface defined in {@code store-core}.
 *
 * <p>Two Micrometer counters are registered on startup so that Prometheus can
 * track outgoing order volume:
 * <ul>
 *   <li>{@code vendix_article_order_published_total} – normal orders</li>
 *   <li>{@code vendix_article_order_urgent_published_total} – urgent orders</li>
 * </ul>
 */
@Service
public class ArticleOrderMessagingServiceImpl implements ArticleOrderMessagingService {

    private static final Logger log = LoggerFactory.getLogger(ArticleOrderMessagingServiceImpl.class);

    private final EventPublisher eventPublisher;
    private final Counter orderPublishedCounter;
    private final Counter urgentOrderPublishedCounter;

    public ArticleOrderMessagingServiceImpl(EventPublisher eventPublisher, MeterRegistry meterRegistry) {
        this.eventPublisher = eventPublisher;
        this.orderPublishedCounter = Counter.builder("vendix_article_order_published_total")
                .description("Total number of normal ArticleOrderEvents published to AMQP")
                .register(meterRegistry);
        this.urgentOrderPublishedCounter = Counter.builder("vendix_article_order_urgent_published_total")
                .description("Total number of urgent ArticleOrderEvents published to AMQP")
                .register(meterRegistry);
    }

    @Override
    public void sendOrder(long storeId, long articleId, long amount) {
        log.atInfo().log("Publishing ArticleOrderEvent: storeId={}, articleId={}, amount={}",
                storeId, articleId, amount);

        ArticleOrderEvent event = new ArticleOrderEvent(storeId, articleId, amount);
        EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                .withContent(event)
                .build();
        eventPublisher.publishMessage(envelope);
        orderPublishedCounter.increment();
    }

    @Override
    public void sendUrgentOrder(long storeId, long articleId, long amount) {
        log.atInfo().log("Publishing ArticleUrgentOrderEvent: storeId={}, articleId={}, amount={}",
                storeId, articleId, amount);

        ArticleUrgentOrderEvent event = new ArticleUrgentOrderEvent(storeId, articleId, amount);
        EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                .withContent(event)
                .build();
        eventPublisher.publishMessage(envelope);
        urgentOrderPublishedCounter.increment();
    }
}
