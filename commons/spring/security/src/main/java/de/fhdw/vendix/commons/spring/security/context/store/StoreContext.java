package de.fhdw.vendix.commons.spring.security.context.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.security.context.ContextAlreadySetException;
import org.jspecify.annotations.Nullable;

public interface StoreContext {
    void setStore(StoreDTO store) throws ContextAlreadySetException;

    @Nullable StoreDTO getStore();
}