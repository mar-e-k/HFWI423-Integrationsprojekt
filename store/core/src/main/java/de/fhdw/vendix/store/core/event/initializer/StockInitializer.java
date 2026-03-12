package de.fhdw.vendix.store.core.event.initializer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local") // TODO
public class StockInitializer {

//    private static final Logger logger = LoggerFactory.getLogger(StockInitializer.class);
//
//    private final StoreService storeService;
//    private final ArticleService articleService;
//    private final StoreStockService storeStockService;
//
//    private static final Long CURRENT_STORE_ID = 1L;
//
//    public StockInitializer(StoreService storeService, ArticleService articleService, StoreStockService storeStockService) {
//        this.storeService = storeService;
//        this.articleService = articleService;
//        this.storeStockService = storeStockService;
//    }
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//        logger.info("Starting stock initialization check...");
//
//        Optional<Store> storeOpt = storeService.findById(CURRENT_STORE_ID);
//
//        if (storeOpt.isEmpty()) {
//            logger.warn("Stock initialization skipped: Store with ID {} not found.", CURRENT_STORE_ID);
//            return;
//        }
//
//        Store currentStore = storeOpt.get();
//        String storeIdentifier = String.format("%s, %s", currentStore.getStreet(), currentStore.getCity());
//
//        if (storeStockService.get(currentStore)) {
//            logger.info("Stock for store '{}' (ID: {}) is already initialized. Skipping.", storeIdentifier, currentStore.getId());
//            return;
//        }
//
//        logger.info("No stock found for store '{}' (ID: {}). Initializing stock now...", storeIdentifier, currentStore.getId());
//
//        List<Article> allArticles = StreamSupport.stream(articleService.findAll().spliterator(), false).toList();
//
//        if (allArticles.isEmpty()) {
//            logger.warn("No articles found in the database. Stock initialization will be empty.");
//            return;
//        }
//
//        List<StoreStock> initialStock = allArticles.stream()
//                .map(article -> new StoreStock(currentStore, article, 0, 5, true))
//                .toList();
//
//        storeStockService.createAll(initialStock);
//
//        logger.info("Successfully initialized stock for {} articles in store '{}'.", initialStock.size(), storeIdentifier);
//    }
}
