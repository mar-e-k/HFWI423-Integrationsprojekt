package de.fhdw.vendix.store.app.context;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

//TODO: make selection at runtime

@Component
public class StoreContext {

    @Nullable
    private StoreDTO store;

    public StoreContext() {}

    public void setStore(@Nullable StoreDTO store) {
        this.store = store;
    }

    public @Nullable StoreDTO getStore() {
        return store;
    }

    public void clearStore() {
        store = null;
    }
}