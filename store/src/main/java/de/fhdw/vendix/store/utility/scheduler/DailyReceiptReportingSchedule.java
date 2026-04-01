package de.fhdw.vendix.store.utility.scheduler;

import de.fhdw.vendix.store.persistence.entity.Receipt;
import de.fhdw.vendix.store.persistence.entity.ReceiptArticle;
import de.fhdw.vendix.store.persistence.service.ReceiptService;
import de.fhdw.vendix.store.utility.StoreClient;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
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

    private final EventPublisher eventPublisher;

    public DailyReceiptReportingSchedule(ReceiptService receiptService, StoreClient storeClient, EventPublisher eventPublisher) {
        this.receiptService = receiptService;
        this.storeClient = storeClient;
        this.eventPublisher = eventPublisher;
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

        articleAmountMap.forEach((articleId, totalAmount) -> {
                    if (totalAmount < 10) {
                        eventPublisher.publishMessage(
                                EventEnvelopeBuilder.defaults()
                                        .withContent(new ArticleOrderEvent(
                                                storeId,
                                                articleId,
                                                totalAmount
                                        ))
                                        .build());
                    } else {
                        eventPublisher.publishMessage(
                                EventEnvelopeBuilder.defaults()
                                        .withContent(new ArticleUrgentOrderEvent(
                                                storeId,
                                                articleId,
                                                totalAmount
                                        ))
                                        .build());
                    }
                }
        );
        log.atInfo().log("Successfully sent daily receipt report");
    }
}