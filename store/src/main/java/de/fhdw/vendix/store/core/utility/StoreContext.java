package de.fhdw.vendix.store.core.utility;

import de.fhdw.vendix.commons.api.domain.store.port.StoreQueryPort;
import de.fhdw.vendix.store.core.domain.store.Store;
import org.springframework.stereotype.Component;

@Component
public final class StoreContext {

    private final StoreQueryPort storeQueryPort;

    private final Store store;

    public StoreContext(StoreQueryPort storeQueryPort) {
        // TODO: replace store selection
        this.storeQueryPort = storeQueryPort;
        store = new Store(
                "Deutschland",
                "Wathlingen",
                "Bachstraße",
                "2"
        );
    }

    public Store getStore() {
        return store;
    }
}