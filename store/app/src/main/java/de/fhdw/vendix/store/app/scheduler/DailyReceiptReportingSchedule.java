package de.fhdw.vendix.store.app.scheduler;

import de.fhdw.vendix.commons.spring.security.context.store.StoreContext;
import de.fhdw.vendix.store.core.domain.article.Article;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStock;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DailyReceiptReportingSchedule {

    private static final Logger log = LoggerFactory.getLogger(DailyReceiptReportingSchedule.class);

    private final EventPublisher eventPublisher;
    private final StoreContext storeContext;
    private final ReceiptService receiptService;
    private final StoreStockService storeStockService;

    public DailyReceiptReportingSchedule(
            EventPublisher eventPublisher,
            StoreContext storeContext,
            ReceiptService receiptService,
            StoreStockService storeStockService
    ) {
        this.eventPublisher = eventPublisher;
        this.storeContext = storeContext;
        this.receiptService = receiptService;
        this.storeStockService = storeStockService;
    }


    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Berlin")
    public void sendDailyReceiptReport() {
        log.atInfo().log("[SCHEDULED] Sending daily receipt report...");

        if (storeContext.getStore() == null || storeContext.getStore().id() == null) {
            throw new IllegalStateException("Cannot sent daily report, as store authContext isn't set properly");
        }

        Long storeId;

        storeId = storeContext.getStore().id();

        Set<Receipt> receipts = receiptService.findAllByStoreIdAndCreatedAtToday(storeId);
        Set<Article> articles = new HashSet<>();

        // Get all articles of the day and record their count.
        // This can probably also be a GROUP-BY in SQL, but native SQL is to generally be avoided
        for (Receipt receipt : receipts) {
            receiptService.findAllReceiptLinesByReceiptId(Objects.requireNonNull(receipt.getId())).stream()
                    .map(ReceiptLine::getArticle)
                    .forEach(articles::add);
        }

        // Check the current amount against the stores needed amount and order accordingly
        for (Article article : articles) {
            StoreStock stock = storeStockService.findByStoreIDAndArticleID(storeId, Objects.requireNonNull(article.getId()))
                    .orElseThrow(EntityNotFoundException::new);

            long currentAmount = stock.getCurrentAmount();
            long deltaAmount = Math.max(0, stock.getPreferenceAmount().getMax() - currentAmount);

            if (currentAmount < stock.getPreferenceAmount().getMin()) {
                sendUrgentArticleOrder(storeId, article.getId(), deltaAmount);
            } else if (currentAmount > stock.getPreferenceAmount().getAvg()) {
                log.atInfo().log("Order ignored for article '{}' as current stock '{}' is higher than specified avg '{}'",
                        article,
                        currentAmount,
                        stock.getPreferenceAmount().getAvg()
                        );
            } else {
                sendNormalArticleOrder(storeId, article.getId(), deltaAmount);
            }
        }

        log.atInfo().log("[SCHEDULED] Successfully sent daily receipt report");
    }

    private void sendNormalArticleOrder(long storeId, long articleId, long amount) {
        ArticleOrderEvent event = new ArticleOrderEvent(
                storeId,
                articleId,
                amount
        );
        EventEnvelope eventEnvelope = EventEnvelopeBuilder.defaults()
                .withContent(event)
                .build();
        eventPublisher.publishMessage(eventEnvelope);
    }

    private void sendUrgentArticleOrder(long storeId, long articleId, long amount) {
        ArticleUrgentOrderEvent event = new ArticleUrgentOrderEvent(
                storeId,
                articleId,
                amount
        );
        EventEnvelope eventEnvelope = EventEnvelopeBuilder.defaults()
                .withContent(event)
                .build();
        eventPublisher.publishMessage(eventEnvelope);
    }
}