package de.fhdw.vendix.store.utility.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class CleanupStoreHostsAndLocksInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CleanupStoreHostsAndLocksInitializer.class);

    private final boolean cleanupLocksHostsOnStartup;

    public CleanupStoreHostsAndLocksInitializer(@Value("${spring.filialensystem.startup.cleanup-locks-hosts:true}") boolean cleanupLocksHostsOnStartup) {
        this.cleanupLocksHostsOnStartup = cleanupLocksHostsOnStartup;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.atInfo().log("spring.filialensystem.startup.cleanup-locks-hosts-on-startup is: {}", cleanupLocksHostsOnStartup);

        if (!cleanupLocksHostsOnStartup) {
            return;
        }
    }

    // TODO
}