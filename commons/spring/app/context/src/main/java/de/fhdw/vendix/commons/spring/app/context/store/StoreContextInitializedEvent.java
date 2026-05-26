package de.fhdw.vendix.commons.spring.app.context.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;

public final class StoreContextInitializedEvent extends DomainContextEvent {

    public StoreContextInitializedEvent(Object source, StoreDTO store) {
        super(source, store);
    }
}