package com.example.application.events;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.events.KommissionAbgeschlossenEvent.ArticleDelivery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KommissionAbgeschlossenListenerTest {

    @Mock
    private LogisticEventPublisher logisticEventPublisher;

    @InjectMocks
    private KommissionAbgeschlossenListener listener;

    @Test
    void publishesDeliveryForEachArticle() {
        var event = new KommissionAbgeschlossenEvent(
            this, 5L,
            List.of(
                new ArticleDelivery(42L, 10L),
                new ArticleDelivery(43L, 5L)
            )
        );

        listener.onKommissionAbgeschlossen(event);

        verify(logisticEventPublisher).publishArticleDelivery(5L, 42L, 10L);
        verify(logisticEventPublisher).publishArticleDelivery(5L, 43L, 5L);
        verifyNoMoreInteractions(logisticEventPublisher);
    }

    @Test
    void continuesOnPublishException() {
        doThrow(new RuntimeException("AMQP down"))
            .when(logisticEventPublisher).publishArticleDelivery(anyLong(), anyLong(), anyLong());

        var event = new KommissionAbgeschlossenEvent(
            this, 5L,
            List.of(
                new ArticleDelivery(42L, 10L),
                new ArticleDelivery(43L, 5L)
            )
        );

        // kein Exception nach aussen -- beide Deliveries werden versucht
        listener.onKommissionAbgeschlossen(event);

        verify(logisticEventPublisher, times(2)).publishArticleDelivery(anyLong(), anyLong(), anyLong());
    }
}
