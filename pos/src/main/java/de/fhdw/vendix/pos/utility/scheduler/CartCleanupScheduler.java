package de.fhdw.vendix.pos.utility.scheduler;

import de.fhdw.vendix.pos.view.cashier.CartItemsManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CartCleanupScheduler {

    private final CartItemsManager cartItemsManager;

    public CartCleanupScheduler(CartItemsManager cartItemsManager) {
        this.cartItemsManager = cartItemsManager;
    }

    // Läuft jede Stunde und bereinigt verwaiste leere Warenkörbe
    @Scheduled(fixedDelay = 3_600_000)
    public void cleanupStaleCarts() {
        cartItemsManager.cleanupStaleCarts();
    }
}
