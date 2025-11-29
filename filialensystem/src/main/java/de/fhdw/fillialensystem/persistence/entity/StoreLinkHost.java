package de.fhdw.fillialensystem.persistence.entity;

import de.fhdw.commons.persistence.entity.GenericEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
public class StoreLinkHost implements GenericEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true, nullable = false)
    private Store store;

    @NotBlank(message = "Host must not be blank")
    private String host;

    @NotNull
    @Min(value = 0, message = "Port must be >= 0")
    private Integer port;

    @NotNull(message = "Last successful ping cannot be null")
    private Instant lastSuccessfulPing;

    public StoreLinkHost() {
        super();
    }

    public StoreLinkHost(Store store, String host, Integer port, Instant lastSuccessfulPing) {
        this.store = store;
        this.host = host;
        this.port = port;
        this.lastSuccessfulPing = lastSuccessfulPing;
    }

    public StoreLinkHost(Long id, Store store, String host, Integer port, Instant lastSuccessfulPing) {
        this.id = id;
        this.store = store;
        this.host = host;
        this.port = port;
        this.lastSuccessfulPing = lastSuccessfulPing;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Instant getLastSuccessfulPing() {
        return lastSuccessfulPing;
    }

    public void setLastSuccessfulPing(Instant lastSuccessfulPing) {
        this.lastSuccessfulPing = lastSuccessfulPing;
    }
}