package de.fhdw.fillialensystem.utility.initializer;

import de.fhdw.fillialensystem.persistence.entity.StoreLinkHost;
import de.fhdw.fillialensystem.persistence.service.StoreLinkHostService;
import de.fhdw.fillialensystem.persistence.service.StoreLinkLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Order(1)
@Component
public class CleanupStoreHostsAndLocksInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CleanupStoreHostsAndLocksInitializer.class);

    private final boolean cleanupLocksHostsOnStartup;
    private final WebClient webClient;

    private final StoreLinkLockService storeLinkLockService;
    private final StoreLinkHostService storeLinkHostService;

    public CleanupStoreHostsAndLocksInitializer(@Value("${spring.filialensystem.startup.cleanup-locks-hosts-on-startup:true}") boolean cleanupLocksHostsOnStartup,
                                                StoreLinkLockService storeLinkLockService,
                                                StoreLinkHostService storeLinkHostService) {
        this.cleanupLocksHostsOnStartup = cleanupLocksHostsOnStartup;
        this.webClient = WebClient.builder().build();
        this.storeLinkLockService = storeLinkLockService;
        this.storeLinkHostService = storeLinkHostService;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.atInfo().log("spring.filialensystem.startup.cleanup-locks-hosts-on-startup is: {}", cleanupLocksHostsOnStartup);

        if (!cleanupLocksHostsOnStartup) {
            return;
        }

        for (StoreLinkHost host : storeLinkHostService.findAll()) {
            try {
                webClient.get()
                        .uri("http://%s:%d/actuator/health".formatted(host.getHost(), host.getPort()))
                        .retrieve()
                        .toBodilessEntity()
                        .block(Duration.ofSeconds(10));
                log.atDebug().log("Host [{}:{}] is alive", host.getHost(), host.getPort());
            } catch (Exception e) {
                removeHostAndLock(host);
            }
        }
    }

    private void removeHostAndLock(StoreLinkHost host) {
        log.atWarn().log("Removing host [{}:{}]. Reason: not pingable", host.getHost(), host.getPort());
        storeLinkHostService.deleteByStore(host.getStore());
        log.atWarn().log("Successfully removed host [{}:{}]", host.getHost(), host.getPort());

        log.atWarn().log("Removing hosts lock");
        storeLinkLockService.deleteByStore(host.getStore());
        log.atWarn().log("Successfully removed hosts lock");
    }
}