package de.fhdw.vendix.commons.spring.app.context.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.ContextAlreadySetException;
import de.fhdw.vendix.commons.spring.app.context.ContextException;
import de.fhdw.vendix.commons.spring.app.context.ContextIllegalSourceException;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DefaultStoreContext implements StoreContext {

    private final SystemContext systemContext;
    private final ApplicationEventPublisher applicationEventPublisher;

    private @Nullable StoreDTO store;

    public DefaultStoreContext(SystemContext systemContext, ApplicationEventPublisher applicationEventPublisher) {
        this.systemContext = systemContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public synchronized void setStore(StoreDTO store) throws ContextException {
        if (store == null) {
            throw new IllegalArgumentException("Parameter 'store' cannot be null");
        }
        if (!systemContext.getApplicationName().equalsIgnoreCase("store")) {
            throw new ContextIllegalSourceException("Store Context can only be set by a store application");
        }
        if (this.store != null) {
            throw new ContextAlreadySetException("Store Context was already set and cannot be changed");
        }
        this.store = store;
        applicationEventPublisher.publishEvent(new StoreContextInitializedEvent(this, store));
    }

    @Override
    public @Nullable StoreDTO getStore() {
        return store;
    }
}