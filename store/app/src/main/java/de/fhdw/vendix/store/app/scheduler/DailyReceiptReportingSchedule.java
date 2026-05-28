package de.fhdw.vendix.store.app.scheduler;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.store.core.store.StoreContext;
import de.fhdw.vendix.store.core.domain.article.Article;
import de.fhdw.vendix.store.core.domain.article.ArticleService;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStock;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import de.fhdw.vendix.store.core.messaging.ArticleOrderMessagingService;
import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs every evening at 22:00 and emits order events for articles that fell
 * below their preferred stock levels during the day.
 *
 * <p>The actual event publishing is delegated to
 * {@link ArticleOrderMessagingService} so that the AMQP library is not
 * referenced here directly, and the same logic can be reused by the test
 * controller.
 */
@Component
public class DailyReceiptReportingSchedule {

    private static final Logger log = LoggerFactory.getLogger(DailyReceiptReportingSchedule.class);

    private final ArticleOrderMessagingService articleOrderMessagingService;
    private final StoreContext storeContext;
    private final ReceiptService receiptService;
    private final StoreStockService storeStockService;
    private final ArticleService articleService;

    public DailyReceiptReportingSchedule(
            ArticleOrderMessagingService articleOrderMessagingService,
            StoreContext storeContext,
            ReceiptService receiptService,
            StoreStockService storeStockService,
            ArticleService articleService
    ) {
        this.articleOrderMessagingService = articleOrderMessagingService;
        this.storeContext = storeContext;
        this.receiptService = receiptService;
        this.storeStockService = storeStockService;
        this.articleService = articleService;
    }


    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Berlin")
    public void sendDailyReceiptReport() {
        log.atInfo().log("[SCHEDULED] Sending daily receipt report...");

        @Nullable StoreDTO currentStore = storeContext.getStore();
        @Nullable Long currentStoreId = currentStore == null ? null : currentStore.id();
        if (currentStoreId == null) {
            log.atWarn().log("[SCHEDULED] Skipping daily receipt report because store context is not initialized");
            return;
        }

        Long storeId = currentStoreId;

        List<Long> soldArticleIds = receiptService.findDistinctArticleIdsSoldTodayByStoreId(storeId);

        // Check the current articleAmount against the stores needed articleAmount and order accordingly
        for (Long articleId : soldArticleIds) {
            Article article = articleService.findById(articleId)
                    .orElseThrow(EntityNotFoundException::new);
            StoreStock stock = storeStockService.findByStoreIdAndArticleId(storeId, articleId)
                    .orElseThrow(EntityNotFoundException::new);

            long currentAmount = stock.getCurrentAmount();
            long deltaAmount = Math.max(0, stock.getPreferenceAmount().getMax() - currentAmount);

            if (currentAmount < stock.getPreferenceAmount().getMin()) {
                articleOrderMessagingService.sendUrgentOrder(storeId, articleId, deltaAmount);
            } else if (currentAmount > stock.getPreferenceAmount().getAvg()) {
                log.atInfo().log("Order ignored for article '{}' as current stock '{}' is higher than specified avg '{}'",
                        article,
                        currentAmount,
                        stock.getPreferenceAmount().getAvg()
                        );
            } else {
                articleOrderMessagingService.sendOrder(storeId, articleId, deltaAmount);
            }
        }

        log.atInfo().log("[SCHEDULED] Successfully sent daily receipt report");
    }
}
