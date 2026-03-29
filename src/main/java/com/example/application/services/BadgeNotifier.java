package com.example.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class BadgeNotifier {

    private static final Logger logger = LoggerFactory.getLogger(BadgeNotifier.class);

    private final NewArticleCountService countService;
    private final NewArticleNotificationService notificationService;

    public BadgeNotifier(NewArticleCountService countService,
                         NewArticleNotificationService notificationService) {
        this.countService = countService;
        this.notificationService = notificationService;
    }

    /**
     * Wartet asynchron, bis der neue Artikel in der DB sichtbar ist,
     * dann benachrichtigt alle UIs. Für eingehende AMQP-Nachrichten.
     */
    @Async
    public void notifyAfterNewQuota() {
        int previousCount = countService.getCount();

        for (int attempt = 0; attempt < 10; attempt++) {
            int currentCount = countService.getCount();

            if (currentCount > previousCount) {
                logger.info("Neuer Artikel sichtbar nach {} Versuch(en), Count={}",
                        attempt + 1, currentCount);
                notificationService.notifyAll(currentCount);
                return;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Fallback nach 2 Sekunden
        logger.warn("Artikel nach 2s noch nicht sichtbar – sende Fallback-Count");
        notificationService.notifyAll(countService.getCount());
    }

    /**
     * Sofortige Benachrichtigung aller UIs mit aktuellem Count.
     * Für manuelle Aufrufe (z.B. nach Anlegen eines Artikels in NewArticlesView).
     */
    @Async
    public void notifyNow() {
        notificationService.notifyAll(countService.getCount());
    }
}