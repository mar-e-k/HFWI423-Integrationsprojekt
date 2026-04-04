package de.fhdw.vendix.commons.spring.security.context.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.security.context.ContextAlreadySetException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;

public final class DefaultStoreContext implements StoreContext {

    private final ApplicationEventPublisher applicationEventPublisher;

    private @Nullable StoreDTO store;

    public DefaultStoreContext(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public synchronized void setStore(StoreDTO store) throws ContextAlreadySetException {
        if (store == null) {
            throw new IllegalArgumentException("Parameter 'store' cannot be null");
        }
        if (this.store != null) {
            throw new ContextAlreadySetException("Store context was already set and cannot be changed");
        }
        this.store = store;
        applicationEventPublisher.publishEvent(new StoreContextInitializedEvent(this, store));
    }

    @Override
    public @Nullable StoreDTO getStore() {
        return store;
    }
}