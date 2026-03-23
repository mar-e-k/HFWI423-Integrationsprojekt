package de.fhdw.vendix.store.api.context;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import org.jspecify.annotations.Nullable;

public interface StoreContext {
    void setStore(@Nullable StoreDTO store);

    @Nullable StoreDTO getStore();

    void clearStore();
}