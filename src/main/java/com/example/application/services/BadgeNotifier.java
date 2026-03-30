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
     * Läuft 2s weiter um auch schnell aufeinanderfolgende Events abzufangen.
     */
    @Async
    public void notifyAfterNewQuota() {
        int previous = countService.getCount();

        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            int current = countService.getCount();
            if (current != previous) {
                logger.info("Badge-Count geaendert: {} -> {} (Versuch {})", previous, current, attempt + 1);
                notificationService.notifyAll(current);
                previous = current;
            }
        }
    }

    /**
     * Sofortige Benachrichtigung aller UIs mit aktuellem Count.
     * Muss nach dem Commit der aufrufenden Transaktion aufgerufen werden.
     */
    public void notifyNow() {
        notificationService.notifyAll(countService.getCount());
    }
}
