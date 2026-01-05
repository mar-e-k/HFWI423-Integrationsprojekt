package de.fhdw.fillialensystem.utility.scheduler;

import de.fhdw.fillialensystem.persistence.service.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockCleanupSchedule {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockCleanupSchedule.class);

    private final DistributedLockService distributedLockService;

    public DistributedLockCleanupSchedule(DistributedLockService distributedLockService) {
        this.distributedLockService = distributedLockService;
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Berlin")
    public void scheduledCleanup() {
        log.atInfo().log("[SCHEDULED] Performing distributed lock cleanup...");

        distributedLockService.deleteAllExpiredLocks();

        log.atInfo().log("[SCHEDULED] Successfully performed distributed lock cleanup");
    }
}