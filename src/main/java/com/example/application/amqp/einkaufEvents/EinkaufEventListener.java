package com.example.application.amqp.einkaufEvents;

import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import com.example.application.services.NewArticleNotificationService;
import io.github.plaguv.amqp.api.event.payment.DeleteQuotaEvent;
import io.github.plaguv.amqp.api.event.payment.NewQuotaEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EinkaufEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EinkaufEventListener.class);

    private final MessagingEventService messagingEventService;
    private final NewArticleNotificationService newArticleNotificationService;
    private final ContingentRepository contingentRepository;

    public EinkaufEventListener(MessagingEventService messagingEventService,
                                NewArticleNotificationService newArticleNotificationService,
                                ContingentRepository contingentRepository) {
        this.messagingEventService = messagingEventService;
        this.newArticleNotificationService = newArticleNotificationService;
        this.contingentRepository = contingentRepository;
    }

    @AmqpEventListener
    public void onNewQuotaEvent(NewQuotaEvent event) {
        if (event == null) {
            throw new MessageRejectedException("NewQuotaEvent war null - ungültiges Event");
        }

        Long articleId = event.articleId();

        logger.info("Neue Quote/Budget erhalten - ArticleID: {}, Amount: {}",
                articleId, event.amount());

        MessagingEvent messagingEvent = new MessagingEvent(
                "NewQuota",
                articleId,
                Long.valueOf(event.amount()),
                "Neue Quote freigegeben"
        );
        messagingEventService.save(messagingEvent);

        // Prüfen, ob Artikel bereits als Kontingent existiert
        boolean alreadyExists = contingentRepository.existsByArticleId(articleId);

        if (alreadyExists) {
            logger.info("ArticleID {} existiert bereits in contingent -> Nachricht wird für Badge ignoriert", articleId);
            return;
        }

        // Nur wenn articleId noch nicht in contingent existiert:
        logger.info("ArticleID {} ist neu -> Badge-Zähler wird erhöht", articleId);
        newArticleNotificationService.increment();

    }

    @AmqpEventListener
    public void onDeleteQuotaEvent(DeleteQuotaEvent event) {
        if (event == null) {
            throw new MessageRejectedException("DeleteQuotaEvent war null - ungültiges Event");
        }

        logger.info("Quote gelöscht/aufgehoben - ArticleID: {}", event.articleId());

        MessagingEvent messagingEvent = new MessagingEvent(
                "DeleteQuota",
                event.articleId(),
                null,
                "Quote gelöscht"
        );
        messagingEventService.save(messagingEvent);
    }
}
