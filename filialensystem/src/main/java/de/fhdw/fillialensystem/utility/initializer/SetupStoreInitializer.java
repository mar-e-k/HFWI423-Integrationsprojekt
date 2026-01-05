package de.fhdw.fillialensystem.utility.initializer;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import de.fhdw.fillialensystem.utility.StoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Order(2)
@Component
public class SetupStoreInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SetupStoreInitializer.class);

    private final boolean setupStoreClientOnStartup;
    private final StoreClient storeClient;

    private final StoreService storeService;

    public SetupStoreInitializer(@Value("${spring.filialensystem.startup.setup-store-client}") boolean setupStoreClientOnStartup,
                                 StoreClient storeClient,
                                 StoreService storeService) {
        this.setupStoreClientOnStartup = setupStoreClientOnStartup;
        this.storeClient = storeClient;
        this.storeService = storeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.atInfo().log("spring.filialensystem.startup.setup-store-client-on-startup is: {}", setupStoreClientOnStartup);

        if (!setupStoreClientOnStartup) {
            return;
        }

        //---- Set and get store ----
        Store store;
        if (storeService.count() == 0) {
            log.atWarn().log("No store defined in table [store]. Falling back to default store.");
            store = new Store();
            store.setCountry("Deutschland");
            store.setCity("Wathlingen");
            store.setStreet("Breuerstraße");
            store.setStreetNumber("0815");
            store = storeService.create(store);
        } else {
            store = storeService.findAll().getFirst();
        }
        storeClient.setStore(store);
    }
}