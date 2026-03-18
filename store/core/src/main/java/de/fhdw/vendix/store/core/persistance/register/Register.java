package de.fhdw.vendix.store.core.persistance.register;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.receipt.Receipt;
import de.fhdw.vendix.store.core.persistance.store.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.*;

@Entity
public class Register extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Store store;

    @OneToMany(mappedBy = "register")
    private Set<Receipt> receipts = new HashSet<>();

    protected Register() {}

    protected Register(Store store) {
        this.store = store;
    }

    protected Register(Long id, Store store) {
        super(id);
        this.store = store;
    }

    public Store getStore() {
        return store;
    }

    public Set<Receipt> getReceipts() {
        return Collections.unmodifiableSet(receipts);
    }
}