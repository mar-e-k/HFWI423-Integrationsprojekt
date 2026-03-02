package de.fhdw.vendix.store.core.utility.scheduler;

import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptArticle;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import de.fhdw.vendix.store.core.utility.StoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyReceiptReportingSchedule {

    private static final Logger log = LoggerFactory.getLogger(DailyReceiptReportingSchedule.class);
    private final ReceiptService receiptService;
    private final StoreClient storeClient;

    public DailyReceiptReportingSchedule(ReceiptService receiptService, StoreClient storeClient) {
        this.receiptService = receiptService;
        this.storeClient = storeClient;
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Berlin")
    public void sendDailyReceiptReport() {
        log.atInfo().log("Sending daily receipt report...");

        List<Receipt> receipts = receiptService.findAll().stream()
                .filter(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now()))
                .filter(r -> r.getStore().equals(storeClient.getStore()))
                .filter(r -> !r.isDepositOnly())
                .toList();

        List<ReceiptArticle> allArticles = receipts.stream()
                .flatMap(r -> receiptService.getReceiptLinkArticles(r).stream())
                .toList();

        Map<Long, Long> articleAmountMap = allArticles.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getArticle().getId(),
                        Collectors.summingLong(ReceiptArticle::getAmount)));

        Long storeId = storeClient.getStore().getId();
//                 TODO: rewrite with new publisher
//        articleAmountMap.forEach((articleId, totalAmount) ->
//                commandSender.fire(
//                        DomainQueue.LOGISTIC_STORE_RESTOCK,
//                        DomainCommand.STORE_RESTOCK,
//                        new LogisticMessageDTO(storeId, articleId, totalAmount, false)));
//        );
        log.atInfo().log("Successfully sent daily receipt report");
    }
}