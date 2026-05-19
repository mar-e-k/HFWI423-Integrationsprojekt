package fhdw.de.einkauf_service.amqp;

import fhdw.de.einkauf_service.metrics.MetricsRegistry;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EinkaufEventPublisherTest {

    @Mock
    private EventPublisher eventPublisher;

    private MetricsRegistry metrics;
    private EinkaufEventPublisher publisher;

    @BeforeEach
    void setUp() {
        metrics = new MetricsRegistry(new SimpleMeterRegistry());
        publisher = new EinkaufEventPublisher(eventPublisher, metrics);
    }

    @Test
    void publishNewQuota_happyPath_publishesOnceAndIncrementsSuccessCounter() {
        publisher.publishNewQuota(10L, 5L);

        verify(eventPublisher, times(1)).publishMessage(any(EventEnvelope.class));
        assertThat(metrics.eventsPublished.count()).isEqualTo(1.0);
        assertThat(metrics.eventProcessingErrors.count()).isZero();
    }

    @Test
    void publishNewQuota_alwaysFails_retriesThreeTimesAndStaysSilent() {
        doThrow(new RuntimeException("AMQP down"))
                .when(eventPublisher).publishMessage(any(EventEnvelope.class));

        assertThatCode(() -> publisher.publishNewQuota(10L, 5L))
                .doesNotThrowAnyException();

        verify(eventPublisher, times(3)).publishMessage(any(EventEnvelope.class));
        assertThat(metrics.eventProcessingErrors.count()).isEqualTo(1.0);
        assertThat(metrics.eventsPublished.count()).isZero();
    }

    @Test
    void publishDeleteQuota_happyPath_publishesOnce() {
        publisher.publishDeleteQuota(10L);

        verify(eventPublisher, times(1)).publishMessage(any(EventEnvelope.class));
        assertThat(metrics.eventsPublished.count()).isEqualTo(1.0);
    }

    @Test
    void publishDeleteQuota_alwaysFails_retriesAndStaysSilent() {
        doThrow(new RuntimeException("AMQP down"))
                .when(eventPublisher).publishMessage(any(EventEnvelope.class));

        assertThatCode(() -> publisher.publishDeleteQuota(10L))
                .doesNotThrowAnyException();

        verify(eventPublisher, times(3)).publishMessage(any(EventEnvelope.class));
        assertThat(metrics.eventProcessingErrors.count()).isEqualTo(1.0);
    }
}
