package com.example.application.api.rabbitmq.listener;

import com.example.application.api.rabbitmq.dto.NewContingentArticleMessage;
import com.example.application.api.rabbitmq.producer.DomainQueue;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.NewArticleNotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NewContingentArticleListener {

    private final ArticleSyncService articleSyncService;
    private final NewArticleNotificationService notificationService;

    public NewContingentArticleListener(ArticleSyncService articleSyncService,
                                        NewArticleNotificationService notificationService) {
        this.articleSyncService = articleSyncService;
        this.notificationService = notificationService;
    }

    @RabbitListener(
            queues = "#{T(com.example.application.api.rabbitmq.producer.DomainQueue)" +
                    ".LOGISTIC_NEW_CONTINGENT_ARTICLE.getQueue()}"
    )
    public void handleNewArticle(NewContingentArticleMessage msg) {
        Long articleId = msg.articleId();

        // 1) ArticleInfo für diesen Artikel anlegen (Name/Nummer aus externer Tabelle holen)
        articleSyncService.createArticleInfoForSingleContingentArticle(articleId);

        // 2) Badge-Zähler für das Menü hochzählen
        notificationService.increment();
    }
}
