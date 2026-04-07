package fhdw.de.einkauf_service.amqp;

import fhdw.de.einkauf_service.metrics.MetricsRegistry;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.payment.DeleteQuotaEvent;
import io.github.plaguv.amqp.api.event.payment.NewQuotaEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EinkaufEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EinkaufEventPublisher.class);

    private final EventPublisher eventPublisher;
    private final MetricsRegistry metrics;

    public EinkaufEventPublisher(EventPublisher eventPublisher, MetricsRegistry metrics) {
        this.eventPublisher = eventPublisher;
        this.metrics = metrics;
    }

    public void publishNewQuota(long articleId, long amount) {
        log.warn("[AMQP] Sending NewQuotaEvent for articleId={}, amount={}", articleId, amount);
        try {
            NewQuotaEvent event = new NewQuotaEvent(articleId, amount);
            EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                    .withContent(event)
                    .build();
            eventPublisher.publishMessage(envelope);

            // 📊 TRACKING: Event veröffentlicht
            metrics.eventsPublished.increment();

            log.warn("[AMQP] NewQuotaEvent sent successfully for articleId={}, amount={}", articleId, amount);
        } catch (Exception e) {
            // 📊 TRACKING: Event-Fehler
            metrics.eventProcessingErrors.increment();

            log.error("[AMQP] Failed to publish NewQuotaEvent for articleId={}, amount={}: {}", articleId, amount, e.getMessage(), e);
        }
    }

    public void publishDeleteQuota(long articleId) {
        log.warn("[AMQP] Sending DeleteQuotaEvent for articleId={}", articleId);
        try {
            DeleteQuotaEvent event = new DeleteQuotaEvent(articleId);
            EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                    .withContent(event)
                    .build();
            eventPublisher.publishMessage(envelope);

            // 📊 TRACKING: Event veröffentlicht
            metrics.eventsPublished.increment();

            log.warn("[AMQP] DeleteQuotaEvent sent successfully for articleId={}", articleId);
        } catch (Exception e) {
            // 📊 TRACKING: Event-Fehler
            metrics.eventProcessingErrors.increment();

            log.error("[AMQP] Failed to publish DeleteQuotaEvent for articleId={}: {}", articleId, e.getMessage(), e);
        }
    }
}
