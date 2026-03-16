package de.fhdw.vendix.store.core.event.scheduler;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptQueryPort;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockQueryPort;
import de.fhdw.vendix.store.commons.context.StoreContext;
import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.LogisticArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.LogisticArticleUrgentOrderEvent;
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

    private final ReceiptQueryPort receiptQueryPort;
    private final StoreStockQueryPort storeStockQueryPort;

    private long storeID;

    public DailyReceiptReportingSchedule(
            EventPublisher eventPublisher,
            StoreContext storeContext,
            ReceiptQueryPort receiptQueryPort,
            StoreStockQueryPort storeStockQueryPort
    ) {
        this.eventPublisher = eventPublisher;
        this.storeContext = storeContext;
        this.receiptQueryPort = receiptQueryPort;
        this.storeStockQueryPort = storeStockQueryPort;
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Berlin")
    public void sendDailyReceiptReport() {
        log.atInfo().log("Sending daily receipt report...");

        if (storeContext.getStore() == null || storeContext.getStore().id() == null) {
            throw new IllegalStateException("Cannot sent daily report, as store context isn't set properly");
        }

        storeID = storeContext.getStore().id();

        Set<ReceiptDTO> receipts = receiptQueryPort.findAllByStoreIdAndCreatedAtToday(storeID);
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
            StoreStockDTO stock = storeStockQueryPort.findByStoreIDAndArticleID(storeID, Objects.requireNonNull(article.id()))
                    .orElseThrow(EntityNotFoundException::new);

            long currentAmount = stock.currentAmount();
            long deltaAmount = Math.max(0, stock.preferenceAmount().max() - currentAmount);

            if (currentAmount < stock.preferenceAmount().minimum()) {
                sendUrgentArticleOrder(article.id(), deltaAmount);
            } else if (currentAmount > stock.preferenceAmount().average()) {
                log.atInfo().log("Order ignored for article '{}' as current stock '{}' is higher than specified average '{}'",
                        article,
                        currentAmount,
                        stock.preferenceAmount().average()
                        );
            } else {
                sendNormalArticleOrder(article.id(), deltaAmount);
            }
        }

        log.atInfo().log("Successfully sent daily receipt report");
    }

    private void sendNormalArticleOrder(long articleID, long amount) {
        LogisticArticleOrderEvent event = new LogisticArticleOrderEvent(
                storeID,
                articleID,
                amount
        );
        EventEnvelope eventEnvelope = EventEnvelopeBuilder.defaults()
                .withContent(event)
                .build();
        eventPublisher.publishMessage(eventEnvelope);
    }

    private void sendUrgentArticleOrder(long articleID, long amount) {
        LogisticArticleUrgentOrderEvent event = new LogisticArticleUrgentOrderEvent(
                storeID,
                articleID,
                amount
        );
        EventEnvelope eventEnvelope = EventEnvelopeBuilder.defaults()
                .withContent(event)
                .build();
        eventPublisher.publishMessage(eventEnvelope);
    }
}