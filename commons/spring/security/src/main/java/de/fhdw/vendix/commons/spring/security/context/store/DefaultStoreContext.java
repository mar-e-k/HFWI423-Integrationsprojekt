package de.fhdw.vendix.commons.spring.security.context.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.security.context.ContextAlreadySetException;
import de.fhdw.vendix.commons.spring.security.context.ContextIllegalSourceException;
import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;

public final class DefaultStoreContext implements StoreContext {

    private final AppContext appContext;
    private final ApplicationEventPublisher applicationEventPublisher;

    private @Nullable StoreDTO store;

    public DefaultStoreContext(AppContext appContext, ApplicationEventPublisher applicationEventPublisher) {
        this.appContext = appContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public synchronized void setStore(StoreDTO store) throws ContextAlreadySetException {
        if (store == null) {
            throw new IllegalArgumentException("Parameter 'store' cannot be null");
        }
        if (!appContext.getApplicationName().equalsIgnoreCase("store")) {
            throw new ContextIllegalSourceException("Store authContext can only be set by a store application");
        }
        if (this.store != null) {
            throw new ContextAlreadySetException("Store authContext was already set and cannot be changed");
        }
        this.store = store;
        applicationEventPublisher.publishEvent(new StoreContextInitializedEvent(this, store));
    }

    @Override
    public @Nullable StoreDTO getStore() {
        return store;
    }
}