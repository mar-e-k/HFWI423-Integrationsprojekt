package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.*;

@Entity
public class StoreLinkLock extends AbstractLock {

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true)
    private Store store;

    public StoreLinkLock() {
        super();
    }

    public StoreLinkLock(Store store) {
        this.store = store;
    }

    public StoreLinkLock(Long id, Store store) {
        super(id);
        this.store = store;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }}
