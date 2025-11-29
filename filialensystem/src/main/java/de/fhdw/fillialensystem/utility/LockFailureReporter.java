package de.fhdw.fillialensystem.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;

public class LockFailureReporter implements SpringBootExceptionReporter {

    private static final Logger log = LoggerFactory.getLogger(LockFailureReporter.class);

    // This constructor is required for Spring to be able to instantiate it.
    public LockFailureReporter(ConfigurableApplicationContext context) {}

    @Override
    public boolean reportException(Throwable failure) {
        // Check if the failure is the specific lock exception we are looking for.
        if (isStoreLockException(failure)) {
            log.error("""

            **********************************************************************************
            *                                                                                *
            *          APPLICATION START FAILED: The store is already locked.                *
            *          Another instance of the application is likely running.                *
            *          This instance will now shut down.                                       *
            *                                                                                *
            **********************************************************************************
            """);
            // Return true to indicate that we have handled the reporting.
            // Spring Boot will not print the default stack trace.
            return true;
        }
        // If it's any other exception, return false to let the default reporter handle it.
        return false;
    }

    private boolean isStoreLockException(Throwable failure) {
        if (failure instanceof IllegalStateException) {
            return failure.getMessage() != null && failure.getMessage().contains("is already locked");
        }
        return false;
    }
}
