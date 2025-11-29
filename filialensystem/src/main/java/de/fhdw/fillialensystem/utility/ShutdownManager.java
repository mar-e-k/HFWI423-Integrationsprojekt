package de.fhdw.fillialensystem.utility;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ShutdownManager {

    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);

    public void initiateShutdown() {
        this.shutdownInitiated.set(true);
    }

    public boolean isShutdownInitiated() {
        return this.shutdownInitiated.get();
    }
}
