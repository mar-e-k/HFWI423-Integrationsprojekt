package de.fhdw.vendix.store.core.event.scheduler;

import de.fhdw.vendix.store.core.persistance.lock.port.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockCleanupSchedule {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockCleanupSchedule.class);

    private final LockService lockPort;

    public DistributedLockCleanupSchedule(LockService lockPort) {
        this.lockPort = lockPort;
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Berlin")
    public void scheduledCleanup() {
        log.atInfo().log("[SCHEDULED] Performing distributed lock cleanup...");

        lockPort.deleteAllExpiredLocks();

        log.atInfo().log("[SCHEDULED] Successfully performed distributed lock cleanup");
    }
}