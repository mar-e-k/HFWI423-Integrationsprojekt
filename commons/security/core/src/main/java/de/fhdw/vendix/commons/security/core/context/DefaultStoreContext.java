package de.fhdw.vendix.commons.security.core.context;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.security.api.context.ContextAlreadySetException;
import de.fhdw.vendix.security.api.context.StoreContext;
import org.jspecify.annotations.Nullable;

public class DefaultStoreContext implements StoreContext {

    private @Nullable StoreDTO store;

    @Override
    public void setStore(@Nullable StoreDTO store) throws ContextAlreadySetException {
        if (this.store == null) {
            this.store = store;
        } else {
            throw new ContextAlreadySetException("Store Context was already set");
        }
    }

    @Override
    public @Nullable StoreDTO getStore() {
        return store;
    }
}