package de.fhdw.fillialensystem.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;

public class LockFailureReporter implements SpringBootExceptionReporter {

    private static final Logger log = LoggerFactory.getLogger(LockFailureReporter.class);

    public LockFailureReporter(ConfigurableApplicationContext context) {}

    @Override
    public boolean reportException(Throwable failure) {
        if (isStoreLockException(failure)) {
            log.error("          APPLICATION START FAILED: The store is already locked.                ");
            log.error("          Another instance of the application is likely running.                ");
            log.error("          This instance will now shut down.                                     ");
            return true;
        }
        return false;
    }

    private boolean isStoreLockException(Throwable failure) {
        if (failure instanceof IllegalStateException) {
            return failure.getMessage() != null && failure.getMessage().contains("is already locked");
        }
        return false;
    }
}
