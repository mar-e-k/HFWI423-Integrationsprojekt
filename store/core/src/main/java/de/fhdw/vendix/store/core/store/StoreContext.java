package de.fhdw.vendix.store.core.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.ContextException;
import org.jspecify.annotations.Nullable;

public interface StoreContext {
    void setStore(StoreDTO store) throws ContextException;

    @Nullable StoreDTO getStore();
}