package de.fhdw.vendix.commons.spring.web.client;

import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import jakarta.annotation.Nullable;

public final class StoreClientHolder {

    private @Nullable InstanceDetailsDTO instance;

    public StoreClientHolder() {}

    @Nullable
    public InstanceDetailsDTO getInstance() {
        return instance;
    }

    public void setInstance(@Nullable InstanceDetailsDTO instance) {
        this.instance = instance;
    }
}