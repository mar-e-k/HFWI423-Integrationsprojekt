package de.fhdw.vendix.orchestrator.app.scheduled;

import de.fhdw.vendix.orchestrator.core.domain.distributed_lock.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
class ExpiredLocks {

    private static final Logger log = LoggerFactory.getLogger(ExpiredLocks.class);

    private final DistributedLockService distributedLockService;

    ExpiredLocks(DistributedLockService distributedLockService) {
        this.distributedLockService = distributedLockService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void check() {
        log.atDebug().log("Checking expired locks...");

        distributedLockService.deleteAllExpiredLocks();

        log.atDebug().log("Successfully checked expired locks.");
    }
}