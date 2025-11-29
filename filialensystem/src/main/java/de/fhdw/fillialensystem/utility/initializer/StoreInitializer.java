package de.fhdw.fillialensystem.utility.initializer;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreWatcher;
import de.fhdw.fillialensystem.persistence.service.StoreLockService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import de.fhdw.fillialensystem.persistence.service.StoreWatcherService;
import de.fhdw.fillialensystem.utility.InstanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Order(1)
@Component
public class StoreInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StoreInitializer.class);

    private final StoreService storeService;
    private final StoreLockService storeLockService;
    private final StoreWatcherService storeWatcherService;
    private final InstanceProvider instanceProvider;
    private final Environment environment;

    @Value("${spring.application.name}")
    private String applicationName;

    public StoreInitializer(StoreService storeService, StoreLockService storeLockService, StoreWatcherService storeWatcherService, InstanceProvider instanceProvider, Environment environment) {
        this.storeService = storeService;
        this.storeLockService = storeLockService;
        this.storeWatcherService = storeWatcherService;
        this.instanceProvider = instanceProvider;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        // This will throw an exception if the lock fails, which is caught by Spring Boot
        // and leads to a clean shutdown.
        cleanupStaleLocksAndWatchers();
        initializeDefaultStore();
        lockStoreAndRegisterWatcher();
    }

    private void cleanupStaleLocksAndWatchers() {
        log.info("Cleaning up stale locks and watchers...");
        RestTemplate restTemplate = new RestTemplate();
        storeWatcherService.findAll().forEach(watcher -> {
            try {
                String url = "http://" + watcher.getHost() + ":" + watcher.getPort() + "/actuator/health";
                restTemplate.getForObject(url, String.class);
                log.info("Instance {} is still alive.", watcher.getInstanceId());
            } catch (Exception e) {
                log.warn("Instance {} is not reachable. Removing watcher and any associated locks.", watcher.getInstanceId());
                storeWatcherService.deleteByInstanceId(watcher.getInstanceId());
                storeLockService.findAll().stream()
                        .filter(lock -> lock.getLockedByInstanceId().equals(watcher.getInstanceId()))
                        .forEach(lock -> {
                            log.warn("Removing stale lock for store {} held by dead instance {}.", lock.getStoreId(), watcher.getInstanceId());
                            storeLockService.deleteByStoreId(lock.getStoreId());
                        });
            }
        });
        log.info("Cleanup complete.");
    }

    private void initializeDefaultStore() {
        if (storeService.count() == 0) {
            log.info("No stores found. Creating default store.");
            Store defaultStore = new Store(new ArrayList<>(), new ArrayList<>(), "Deutschland", "Musterstadt", "Musterstraße", 1);
            storeService.save(defaultStore);
            log.info("Default store created successfully.");
        }
    }

    private void lockStoreAndRegisterWatcher() {
        Optional<Store> storeToLock = storeService.findAll().stream().findFirst();
        if (storeToLock.isEmpty()) {
            log.error("No store found to lock. The application will not be locked.");
            return;
        }

        String storeIdToLock = storeToLock.get().getId().toString(); // Use the database ID
        int port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("local.server.port")));

        // This will throw an IllegalStateException if the lock is already taken
        storeLockService.lock(storeIdToLock, instanceProvider.getInstanceId());
        log.info("Store {} locked successfully by instance {}.", storeIdToLock, instanceProvider.getInstanceId());

        // This code only runs if the lock was successful
        StoreWatcher watcher = new StoreWatcher(instanceProvider.getInstanceId(), "localhost", port, applicationName, Instant.now());
        storeWatcherService.save(watcher);
        log.info("Instance {} registered in watcher.", instanceProvider.getInstanceId());
    }
}
