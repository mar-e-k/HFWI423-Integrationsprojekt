package de.fhdw.vendix.store.application.context;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store.port.StoreQueryPort;
import org.springframework.stereotype.Component;

@Component
public final class StoreContext {

    private final StoreQueryPort storeQueryPort;

    private final StoreDTO store;

    public StoreContext(StoreQueryPort storeQueryPort) {
        // TODO: replace store selection
        this.storeQueryPort = storeQueryPort;
        store = new StoreDTO(
                0,
                "Deutschland",
                "Wathlingen",
                "Bachstraße",
                "2"
        );
    }

    public StoreDTO getStore() {
        return store;
    }
}