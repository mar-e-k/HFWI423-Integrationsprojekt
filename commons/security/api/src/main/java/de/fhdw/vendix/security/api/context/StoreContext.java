package de.fhdw.vendix.security.api.context;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import org.jspecify.annotations.Nullable;

public interface StoreContext {
    void setStore(@Nullable StoreDTO store) throws ContextAlreadySetException;

    @Nullable StoreDTO getStore();
}