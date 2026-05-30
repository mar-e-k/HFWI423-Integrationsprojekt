package de.fhdw.vendix.store.app.messaging;

import de.fhdw.vendix.store.core.domain.store_stock_order.ArticleOrderPublisher;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import org.springframework.stereotype.Service;

@Service
class AmqpArticleOrderPublisher implements ArticleOrderPublisher {

    private final EventPublisher eventPublisher;

    AmqpArticleOrderPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishOrder(long storeId, long articleId, long amount, boolean urgent) {
        EventEnvelope envelope = urgent
                ? EventEnvelopeBuilder.defaults()
                        .withContent(new ArticleUrgentOrderEvent(storeId, articleId, amount))
                        .build()
                : EventEnvelopeBuilder.defaults()
                        .withContent(new ArticleOrderEvent(storeId, articleId, amount))
                        .build();

        eventPublisher.publishMessage(envelope);
    }
}
