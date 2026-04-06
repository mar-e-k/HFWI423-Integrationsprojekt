package com.example.application.amqp.storeEvents;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.SonderkommissionSchedueler;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AMQP Event Listener für Bestellungen von Filialen.
 *
 * Empfängt Bestellungen über die plaguv-Library:
 * - ArticleOrderEvent:       Normale wöchentliche Bestellung bei Unterbestand
 * - ArticleUrgentOrderEvent: Sonderkommission bei kritischem Bestand (< 5 Artikel)
 *
 * Der Inhalt wird als MessageLogistic in die DB geschrieben,
 * damit der WeeklyKommissionScheduler daraus Kommissionen erzeugen kann.
 */
@Component
public class LogisticOrderListener {

    private static final Logger logger = LoggerFactory.getLogger(LogisticOrderListener.class);

    private final MessageLogisticRepository msgRepo;
    private final ArticleInfoRepository articleRepo;
    private final SonderkommissionSchedueler kommissionService;

    public LogisticOrderListener(MessageLogisticRepository msgRepo,
                                 ArticleInfoRepository articleRepo,
                                 SonderkommissionSchedueler kommissionService) {
        this.msgRepo = msgRepo;
        this.articleRepo = articleRepo;
        this.kommissionService = kommissionService;
    }

    /**
     * 1.1 – Normale Bestellung: Inhalt in DB schreiben.
     */
    @AmqpEventListener
    @Transactional
    public void onLogisticArticleOrder(ArticleOrderEvent order) {
    	System.out.println("hier print 1 wurde aufgerufen ");
        if (order == null) {
            throw new MessageRejectedException("ArticleOrderEvent war null");
        }
        logger.info("📦 Normale Bestellung erhalten - StoreID: {}, ArticleID: {}, Quantity: {}",
                order.storeId(), order.articleId(), order.quantity());

        saveAsMessageLogistic(order.storeId(), order.articleId(), order.quantity());
    }

    /**
     * 1.1 + 1.2 – Dringende Bestellung: Inhalt in DB schreiben
     *             und Sonderkommissionierung auslösen.
     */
    @AmqpEventListener
    @Transactional
    public void onLogisticUrgentArticleOrder(ArticleUrgentOrderEvent order) {
        if (order == null) {
            throw new MessageRejectedException("ArticleUrgentOrderEvent war null");
        }
        logger.info("🚨 Dringende Bestellung erhalten - StoreID: {}, ArticleID: {}, Quantity: {}",
                order.storeId(), order.articleId(), order.quantity());

        // Ganz normal in die DB schreiben (wie bei normaler Bestellung)
        saveAsMessageLogistic(order.storeId(), order.articleId(), order.quantity());

        //Sonderkommissionierungs-Service aufrufen sobald fertig
        kommissionService.createSonderKommission(String.valueOf(order.storeId()));
    }

    /**
     * Wandelt ein eingehendes Event in einen MessageLogistic-Eintrag um.
     * Alte verarbeitete Einträge des Stores werden vorher bereinigt.
     */
    private void saveAsMessageLogistic(long storeId, long articleId, long quantity) {

        // Artikel über die numerische articleId in article_info suchen
        ArticleInfo article = articleRepo.findByArticleId(articleId);
        if (article == null) {
            logger.warn("⚠️ Artikel mit articleId {} nicht in article_info gefunden – Message wird ignoriert.",
                    articleId);
            return;
        }

        String storeIdStr = String.valueOf(storeId);

        // Alte verarbeitete Einträge dieses Stores bereinigen
        msgRepo.deleteProcessedByStore(storeIdStr);

        // Bestehenden unverarbeiteten Eintrag für denselben Artikel+Store ersetzen (kein Duplikat)
        msgRepo.deleteUnprocessedByStoreAndArticleId(storeIdStr, articleId);

        // Neuen Eintrag speichern
        MessageLogistic msg = new MessageLogistic();
        msg.setStoreId(storeIdStr);
        msg.setArticleNumber(article.getArticleNumber());
        msg.setArticleId(articleId);
        msg.setQuantity(quantity);
        msg.setProcessed(false);

        msgRepo.save(msg);

        logger.info("✅ MessageLogistic gespeichert - Store: {}, Artikel: {} ({}), Menge: {}",
                storeIdStr, article.getArticleNumber(), article.getName(), quantity);
    }
}
