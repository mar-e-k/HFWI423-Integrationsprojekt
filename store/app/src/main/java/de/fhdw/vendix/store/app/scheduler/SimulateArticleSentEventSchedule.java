package de.fhdw.vendix.store.app.scheduler;

import de.fhdw.vendix.store.core.domain.article.Article;
import de.fhdw.vendix.store.core.domain.article.ArticleService;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class SimulateArticleSentEventSchedule {

    private static final Logger log = LoggerFactory.getLogger(SimulateArticleSentEventSchedule.class);

    private final Set<ArticleOrderEvent> articleOrderEvents = Collections.synchronizedSet(new HashSet<>());
    private final Set<ArticleUrgentOrderEvent> articleUrgentOrderEvents = Collections.synchronizedSet(new HashSet<>());

    private final EventPublisher eventPublisher;

    public SimulateArticleSentEventSchedule(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @ConditionalOnBooleanProperty(
            value = "vendix.testing.simulate-random-article-order",
            havingValue = true,
            matchIfMissing = false
    )
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void sendRandomArticleToStore() {
        log.atInfo().log("Sending received articles to store");

        for (ArticleOrderEvent articleOrderEvent : articleOrderEvents) {
            ArticleSentEvent event = new ArticleSentEvent(
                    articleOrderEvent.storeId(),
                    articleOrderEvent.articleId(),
                    articleOrderEvent.quantity()
            );
            EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                    .withContent(event)
                    .build();
            eventPublisher.publishMessage(envelope);
        }
        articleOrderEvents.clear();

        for (ArticleUrgentOrderEvent articleUrgentOrderEvent : articleUrgentOrderEvents) {
            ArticleSentEvent event = new ArticleSentEvent(
                    articleUrgentOrderEvent.storeId(),
                    articleUrgentOrderEvent.articleId(),
                    articleUrgentOrderEvent.quantity()
            );
            EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                    .withContent(event)
                    .build();
            eventPublisher.publishMessage(envelope);
        }
        articleUrgentOrderEvents.clear();

        log.atInfo().log("Successfully sent articles to store");
    }


    @AmqpEventListener
    public void onArticleOrderEvent(ArticleOrderEvent event) {
        articleOrderEvents.add(event);
    }

    @AmqpEventListener
    public void onArticleUrgentOrderEvent(ArticleUrgentOrderEvent event) {
        articleUrgentOrderEvents.add(event);
    }
}