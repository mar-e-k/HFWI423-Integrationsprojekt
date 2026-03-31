package fhdw.de.einkauf_service.amqp;

import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EinkaufEventPublisherTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private EinkaufEventPublisher publisher;

    // --- publishNewQuota ---

    @Test
    void publishNewQuota_happyPath_callsEventPublisherPublishMessage() {
        publisher.publishNewQuota(10L, 5L);

        verify(eventPublisher, times(1)).publishMessage(any(EventEnvelope.class));
    }

    @Test
    void publishNewQuota_eventPublisherThrows_doesNotPropagateException() {
        doThrow(new RuntimeException("AMQP down"))
                .when(eventPublisher).publishMessage(any(EventEnvelope.class));

        assertThatCode(() -> publisher.publishNewQuota(10L, 5L))
                .doesNotThrowAnyException();
    }

    @Test
    void publishNewQuota_eventPublisherThrows_publishMessageCalledOnce() {
        doThrow(new RuntimeException("AMQP down"))
                .when(eventPublisher).publishMessage(any(EventEnvelope.class));

        publisher.publishNewQuota(10L, 5L);

        verify(eventPublisher, times(1)).publishMessage(any(EventEnvelope.class));
    }

    // --- publishDeleteQuota ---

    @Test
    void publishDeleteQuota_happyPath_callsEventPublisherPublishMessage() {
        publisher.publishDeleteQuota(10L);

        verify(eventPublisher, times(1)).publishMessage(any(EventEnvelope.class));
    }

    @Test
    void publishDeleteQuota_eventPublisherThrows_doesNotPropagateException() {
        doThrow(new RuntimeException("AMQP down"))
                .when(eventPublisher).publishMessage(any(EventEnvelope.class));

        assertThatCode(() -> publisher.publishDeleteQuota(10L))
                .doesNotThrowAnyException();
    }
}
