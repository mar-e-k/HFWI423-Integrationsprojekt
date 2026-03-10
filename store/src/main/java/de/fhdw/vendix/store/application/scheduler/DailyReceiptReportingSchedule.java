package de.fhdw.vendix.store.application.scheduler;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptQueryPort;
import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockQueryPort;
import de.fhdw.vendix.store.application.context.StoreContext;
import io.github.plaguv.core.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyReceiptReportingSchedule {

    private static final Logger log = LoggerFactory.getLogger(DailyReceiptReportingSchedule.class);

    private final EventPublisher eventPublisher;
    private final StoreContext storeContext;

    private final ReceiptQueryPort receiptQueryPort;
    private final StoreStockQueryPort storeStockQueryPort;

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

        List<ReceiptDTO> receipts = receiptQueryPort.findAllByStoreToday(storeContext.getStore().storeId());

        for (ReceiptDTO receipt : receipts) {
            // TODO
            // loop through all and do a check with the stock with
            // storeStockQueryPort.getArticle().amount() > receipt.getArticle() or something similar
            // note that article have to be grouped since one with a different discount is listed twice
        }

//        EventEnvelope event = EventEnvelopeBuilder.defaults()
//                .withContent(new Object()) //TODO: replace with actual contents / dto
//                .build();

        log.atInfo().log("Successfully sent daily receipt report");
    }
}