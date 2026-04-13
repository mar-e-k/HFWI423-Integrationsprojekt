package de.fhdw.vendix.commons.spring.app.context.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import org.springframework.context.ApplicationEvent;


public final class StoreContextInitializedEvent extends ApplicationEvent {

    private final StoreDTO store;

    public StoreContextInitializedEvent(Object source, StoreDTO store) {
        super(source);
        this.store = store;
    }

    public StoreDTO getStore() {
        return store;
    }
}