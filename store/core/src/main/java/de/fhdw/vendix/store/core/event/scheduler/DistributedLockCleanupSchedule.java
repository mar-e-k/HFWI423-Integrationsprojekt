package de.fhdw.vendix.store.core.event.scheduler;

import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockCleanupSchedule {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockCleanupSchedule.class);

    private final LockCommandPort lockCommandPort;

    public DistributedLockCleanupSchedule(LockCommandPort lockCommandPort) {
        this.lockCommandPort = lockCommandPort;
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Berlin")
    public void scheduledCleanup() {
        log.atInfo().log("[SCHEDULED] Performing distributed lock cleanup...");

        // TODO: This can also be implemented in Postgres, or even better, redis
        lockCommandPort.deleteAllByExpiresAtNow();

        log.atInfo().log("[SCHEDULED] Successfully performed distributed lock cleanup");
    }
}