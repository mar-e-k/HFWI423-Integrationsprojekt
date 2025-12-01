package de.fhdw.fillialensystem.utility;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import de.fhdw.fillialensystem.persistence.service.StoreLinkStockService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import de.fhdw.fillialensystem.persistence.service.imported.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class StockInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StockInitializer.class);

    private final StoreService storeService;
    private final ArticleService articleService;
    private final StoreLinkStockService storeLinkStockService;

    // Annahme: Es gibt eine definierte Filial-ID, die diese Instanz repräsentiert.
    private static final Long CURRENT_STORE_ID = 1L;

    public StockInitializer(StoreService storeService, ArticleService articleService, StoreLinkStockService storeLinkStockService) {
        this.storeService = storeService;
        this.articleService = articleService;
        this.storeLinkStockService = storeLinkStockService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting stock initialization check...");

        Optional<Store> storeOpt = storeService.findById(CURRENT_STORE_ID);

        if (storeOpt.isEmpty()) {
            logger.warn("Stock initialization skipped: Store with ID {} not found.", CURRENT_STORE_ID);
            return;
        }

        Store currentStore = storeOpt.get();
        String storeIdentifier = String.format("%s, %s", currentStore.getStreet(), currentStore.getCity());

        if (storeLinkStockService.stockExistsForStore(currentStore)) {
            logger.info("Stock for store '{}' (ID: {}) is already initialized. Skipping.", storeIdentifier, currentStore.getId());
            return;
        }

        logger.info("No stock found for store '{}' (ID: {}). Initializing stock now...", storeIdentifier, currentStore.getId());

        List<Article> allArticles = articleService.findAll();

        if (allArticles.isEmpty()) {
            logger.warn("No articles found in the database. Stock initialization will be empty.");
            return;
        }

        List<StoreLinkStock> initialStock = allArticles.stream()
                .map(article -> new StoreLinkStock(currentStore, article, 5L, true))
                .toList();

        storeLinkStockService.saveAll(initialStock);

        logger.info("Successfully initialized stock for {} articles in store '{}'.", initialStock.size(), storeIdentifier);
    }
}
