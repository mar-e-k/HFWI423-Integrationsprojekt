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

import java.util.UUID;
import java.util.function.Supplier;

@Service
public class EinkaufEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(EinkaufEventPublisher.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 200L;
    private static final long BACKOFF_MULTIPLIER = 4L;

    private final EventPublisher eventPublisher;
    private final MetricsRegistry metrics;

    public EinkaufEventPublisher(EventPublisher eventPublisher, MetricsRegistry metrics) {
        this.eventPublisher = eventPublisher;
        this.metrics = metrics;
    }

    @Override
    public void publishNewQuota(long articleId, long amount) {
        UUID eventId = UUID.randomUUID();
        log.info("[AMQP] Publishing NewQuotaEvent eventId={} articleId={} amount={}", eventId, articleId, amount);
        publishWithRetry(eventId, () -> envelope(eventId, new NewQuotaEvent(articleId, amount)),
                "NewQuotaEvent articleId=" + articleId);
    }

    @Override
    public void publishDeleteQuota(long articleId) {
        UUID eventId = UUID.randomUUID();
        log.info("[AMQP] Publishing DeleteQuotaEvent eventId={} articleId={}", eventId, articleId);
        publishWithRetry(eventId, () -> envelope(eventId, new DeleteQuotaEvent(articleId)),
                "DeleteQuotaEvent articleId=" + articleId);
    }

    private EventEnvelope envelope(UUID eventId, Object content) {
        return EventEnvelopeBuilder.defaults()
                .withEventId(eventId)
                .withContent(content)
                .build();
    }

    /**
     * Versucht eine Veröffentlichung mehrfach mit exponentiellem Backoff.
     * Bleibt fire-and-forget: scheitert nach erschöpften Versuchen still
     * (Log + Metrik), damit das lokale Geschäft nicht an einem
     * Messaging-Ausfall hängenbleibt — lose Kopplung an die Außenwelt.
     */
    private void publishWithRetry(UUID eventId, Supplier<EventEnvelope> envelopeSupplier, String description) {
        long backoff = INITIAL_BACKOFF_MS;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                eventPublisher.publishMessage(envelopeSupplier.get());
                metrics.eventsPublished.increment();
                if (attempt > 1) {
                    log.info("[AMQP] {} succeeded on attempt {}/{} (eventId={})", description, attempt, MAX_ATTEMPTS, eventId);
                }
                return;
            } catch (Exception e) {
                if (attempt == MAX_ATTEMPTS) {
                    metrics.eventProcessingErrors.increment();
                    log.error("[AMQP] {} failed permanently after {} attempts (eventId={}): {}",
                            description, MAX_ATTEMPTS, eventId, e.getMessage(), e);
                    return;
                }
                log.warn("[AMQP] {} attempt {}/{} failed (eventId={}): {} — retrying in {} ms",
                        description, attempt, MAX_ATTEMPTS, eventId, e.getMessage(), backoff);
                sleepQuietly(backoff);
                backoff *= BACKOFF_MULTIPLIER;
            }
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
