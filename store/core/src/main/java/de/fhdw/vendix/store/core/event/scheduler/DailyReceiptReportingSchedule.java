package de.fhdw.vendix.store.core.event.scheduler;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.security.api.context.StoreContext;
import de.fhdw.vendix.store.core.persistance.receipt.port.ReceiptService;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.store.core.persistance.store_stock.port.StoreStockService;
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

    private final ReceiptService receiptQueryPort;
    private final StoreStockService storeStockQueryPort;

    public DailyReceiptReportingSchedule(
            EventPublisher eventPublisher,
            StoreContext storeContext,
            ReceiptService receiptPort,
            StoreStockService storeStockPort
    ) {
        this.eventPublisher = eventPublisher;
        this.storeContext = storeContext;
        this.receiptQueryPort = receiptPort;
        this.storeStockQueryPort = storeStockPort;
    }


    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Berlin")
    public void sendDailyReceiptReport() {
        log.atInfo().log("[SCHEDULED] Sending daily receipt report...");

        if (storeContext.getStore() == null || storeContext.getStore().id() == null) {
            throw new IllegalStateException("Cannot sent daily report, as store context isn't set properly");
        }

        Long storeId;

        storeId = storeContext.getStore().id();

        Set<ReceiptDTO> receipts = receiptQueryPort.findAllByStoreIdAndCreatedAtToday(storeId);
        Set<ArticleDTO> articles = new HashSet<>();

        // Get all articles of the day and record their count.
        // This can probably also be a GROUP-BY in SQL, but native SQL is to generally be avoided
        for (ReceiptDTO receipt : receipts) {
            receiptQueryPort.findAllReceiptLinesByReceiptId(Objects.requireNonNull(receipt.id())).stream()
                    .map(ReceiptLineDTO::article)
                    .forEach(articles::add);
        }

        // Check the current amount against the stores needed amount and order accordingly
        for (ArticleDTO article : articles) {
            StoreStockDTO stock = storeStockQueryPort.findByStoreIDAndArticleID(storeId, Objects.requireNonNull(article.id()))
                    .orElseThrow(EntityNotFoundException::new);

            long currentAmount = stock.currentAmount();
            long deltaAmount = Math.max(0, stock.preferenceAmount().max() - currentAmount);

            if (currentAmount < stock.preferenceAmount().minimum()) {
                sendUrgentArticleOrder(storeId, article.id(), deltaAmount);
            } else if (currentAmount > stock.preferenceAmount().average()) {
                log.atInfo().log("Order ignored for article '{}' as current stock '{}' is higher than specified average '{}'",
                        article,
                        currentAmount,
                        stock.preferenceAmount().average()
                        );
            } else {
                sendNormalArticleOrder(storeId, article.id(), deltaAmount);
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