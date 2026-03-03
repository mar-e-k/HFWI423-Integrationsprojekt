package de.fhdw.vendix.store.core.domain.store_system;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.store.Store;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
public class StoreSystem extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    private Store store;

    @NotBlank
    private String host;

    @NotNull
    @Min(value = 0)
    private Integer port;

    @NotNull
    private Instant lastSuccessfulPing;

    protected StoreSystem() {}

    public StoreSystem(Store store, String host, Integer port, Instant lastSuccessfulPing) {
        this.store = store;
        this.host = host;
        this.port = port;
        this.lastSuccessfulPing = lastSuccessfulPing;
    }

    public Store getStore() {
        return store;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Instant getLastSuccessfulPing() {
        return lastSuccessfulPing;
    }
}