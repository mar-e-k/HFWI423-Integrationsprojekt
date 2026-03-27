package fhdw.de.einkauf_service.amqp;

import fhdw.de.einkauf_service.entity.ReceivedDealNotification;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.ReceivedDealNotificationRepository;
import io.github.plaguv.amqp.api.event.logistic.NewDealEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        log.warn("[AMQP] Received NewDealEvent for articleId={}", event.articleId());

        String articleNumber = articleRepository.findArticleNumberById(event.articleId())
                .orElse("GTIN-" + event.articleId());
        String articleName = articleRepository.findNameById(event.articleId())
                .orElse("Artikel #" + event.articleId());

        ReceivedDealNotification notification = new ReceivedDealNotification();
        notification.setArticleId(event.articleId());
        notification.setArticleNumber(articleNumber);
        notification.setArticleName(articleName);
        notification.setReceivedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }
}
