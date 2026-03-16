package de.fhdw.vendix.store.core.event.initializer;


public class SetupStoreInitializer {

//    private static final Logger log = LoggerFactory.getLogger(SetupStoreInitializer.class);
//
//    private final boolean setupStoreClientOnStartup;
//    private final StoreContext storeContext;
//
//    private final StoreService storeService;
//
//    public SetupStoreInitializer(@Value("${spring.filialensystem.startup.setup-store-client}") boolean setupStoreClientOnStartup,
//                                 StoreContext storeContext,
//                                 StoreService storeService) {
//        this.setupStoreClientOnStartup = setupStoreClientOnStartup;
//        this.storeContext = storeContext;
//        this.storeService = storeService;
//    }
//
//    // TODO: whole logic seems iffy at best. should be rewritten
//
//    @Override
//    public void run(ApplicationArguments args) {
//        log.atInfo().log("spring.filialensystem.startup.setup-store-client-on-startup is: {}", setupStoreClientOnStartup);
//
//        if (!setupStoreClientOnStartup) {
//            return;
//        }
//
//        Store store;
//        if (storeService.count() == 0) {
//            log.atWarn().log("No store defined in table [store]. Falling back to default store.");
//            store = new Store(
//                    "Deutschland",
//                    "Wathlingen",
//                    "Breuerstraße",
//                    "0815"
//            );
//            store = storeService.create(store);
//        } else {
//            store = storeService.findAll().iterator().next();
//        }
//        storeContext.setStore(store);
//    }
}