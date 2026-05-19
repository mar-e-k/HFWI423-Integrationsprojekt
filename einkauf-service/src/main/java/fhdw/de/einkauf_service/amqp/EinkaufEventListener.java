package fhdw.de.einkauf_service.amqp;

import fhdw.de.einkauf_service.entity.ReceivedDealNotification;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.ReceivedDealNotificationRepository;
import io.github.plaguv.amqp.api.event.logistic.NewDealEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class EinkaufEventListener {

    private static final Logger log = LoggerFactory.getLogger(EinkaufEventListener.class);

    private final ReceivedDealNotificationRepository notificationRepository;
    private final ArticleRepository articleRepository;

    public EinkaufEventListener(ReceivedDealNotificationRepository notificationRepository,
                                ArticleRepository articleRepository) {
        this.notificationRepository = notificationRepository;
        this.articleRepository = articleRepository;
    }

    @AmqpEventListener
    @Transactional
    public void onNewDealEvent(NewDealEvent event) {
        if (event == null) return;

        LocalDateTime now = LocalDateTime.now();
        String externalEventId = buildExternalEventId(event.articleId(), now);

        if (notificationRepository.existsByExternalEventId(externalEventId)) {
            log.info("[AMQP] Duplicate NewDealEvent ignored (externalEventId={})", externalEventId);
            return;
        }

        log.warn("[AMQP] Received NewDealEvent for articleId={} (externalEventId={})",
                event.articleId(), externalEventId);

        String articleNumber = articleRepository.findArticleNumberById(event.articleId())
                .orElse("GTIN-" + event.articleId());
        String articleName = articleRepository.findNameById(event.articleId())
                .orElse("Artikel #" + event.articleId());

        ReceivedDealNotification notification = new ReceivedDealNotification();
        notification.setArticleId(event.articleId());
        notification.setArticleNumber(articleNumber);
        notification.setArticleName(articleName);
        notification.setReceivedAt(now);
        notification.setExternalEventId(externalEventId);

        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException race) {
            log.info("[AMQP] Concurrent duplicate NewDealEvent rejected by DB (externalEventId={})", externalEventId);
        }
    }

    /**
     * Stabiler Schlüssel für Duplikat-Erkennung. plaguv-amqp 1.3.0 reicht die
     * Envelope-eventId nicht zum Listener durch — daher Schlüssel aus
     * (articleId, auf Sekunde gerundeter Eingang). Eine Redelivery innerhalb
     * derselben Sekunde wird als Duplikat erkannt.
     */
    private static String buildExternalEventId(long articleId, LocalDateTime receivedAt) {
        return "new-deal:" + articleId + "@" + receivedAt.truncatedTo(ChronoUnit.SECONDS);
    }
}
