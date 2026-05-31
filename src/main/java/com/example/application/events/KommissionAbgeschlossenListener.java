package com.example.application.events;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class KommissionAbgeschlossenListener {

    private static final Logger log = LoggerFactory.getLogger(KommissionAbgeschlossenListener.class);

    private final LogisticEventPublisher logisticEventPublisher;

    public KommissionAbgeschlossenListener(LogisticEventPublisher logisticEventPublisher) {
        this.logisticEventPublisher = logisticEventPublisher;
    }

    @EventListener
    public void onKommissionAbgeschlossen(KommissionAbgeschlossenEvent event) {
        for (KommissionAbgeschlossenEvent.ArticleDelivery delivery : event.getDeliveries()) {
            try {
                logisticEventPublisher.publishArticleDelivery(
                    event.getStoreId(), delivery.articleId(), delivery.quantity()
                );
            } catch (Exception e) {
                log.warn("[KommissionAbgeschlossenListener] AMQP-Publish fehlgeschlagen - StoreId: {}, ArticleId: {}",
                    event.getStoreId(), delivery.articleId());
            }
        }
    }
}
