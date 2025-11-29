package de.fhdw.fillialensystem.utility.initializer;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkHost;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkLock;
import de.fhdw.fillialensystem.persistence.service.StoreLinkHostService;
import de.fhdw.fillialensystem.persistence.service.StoreLinkLockService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import de.fhdw.fillialensystem.utility.StoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Order(2)
@Component
public class SetupStoreInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SetupStoreInitializer.class);

    private final boolean setupStoreClientOnStartup;
    private final StoreClient storeClient;

    private final StoreService storeService;
    private final StoreLinkLockService storeLinkLockService;
    private final StoreLinkHostService storeLinkHostService;

    public SetupStoreInitializer(@Value("${spring.filialensystem.startup.setup-store-client-on-startup}") boolean setupStoreClientOnStartup,
                                 StoreClient storeClient,
                                 StoreService storeService,
                                 StoreLinkLockService storeLinkLockService,
                                 StoreLinkHostService storeLinkHostService) {
        this.setupStoreClientOnStartup = setupStoreClientOnStartup;
        this.storeClient = storeClient;
        this.storeService = storeService;
        this.storeLinkLockService = storeLinkLockService;
        this.storeLinkHostService = storeLinkHostService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.atInfo().log("spring.filialensystem.startup.setup-store-client-on-startup is: {}", setupStoreClientOnStartup);

        if (!setupStoreClientOnStartup) {
            return;
        }

        // ---- Set and get store ----
        Store store;
        if (storeService.count() == 0) {
            log.atWarn().log("No store defined in table [store]. Falling back to default store.");
            store = new Store();
            store.setCountry("Deutschland");
            store.setCity("Wathlingen");
            store.setStreet("Bachtstraße");
            store.setStreetNumber("2");
            store = storeService.save(store);
        } else {
            store = storeService.findAll().getFirst();
        }
        storeClient.setStore(store);

        // ---- Set store lock ----
        log.atDebug().log("Setting up lock for store[{}]", store.getId());
        storeLinkLockService.save(new StoreLinkLock(
                store,
                Instant.now()
        ));
        log.atDebug().log("Successfully set up lock for store[{}]", store.getId());

        // ---- Set store host ----
        log.atDebug().log("Setting up host for store[{}]", store.getId());
        storeLinkHostService.save(new StoreLinkHost(
                storeClient.getStore(),
                storeClient.getHost(),
                storeClient.getPort(),
                Instant.now()
        ));
        log.atDebug().log("Successfully set up host for store[{}]", store.getId());
    }
}