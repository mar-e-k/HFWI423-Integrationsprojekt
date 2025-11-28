package de.fhdw.fillialensystem.utility.initializer;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class StoreInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StoreInitializer.class);

    private final StoreService storeService;

    public StoreInitializer(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Deleting all existing stores.");
        storeService.deleteAll();
        log.info("All stores deleted. Creating default store.");
        Store defaultStore = new Store("default-store", "Hauptfiliale");
        storeService.save(defaultStore);
        log.info("Default store created successfully.");
    }
}
