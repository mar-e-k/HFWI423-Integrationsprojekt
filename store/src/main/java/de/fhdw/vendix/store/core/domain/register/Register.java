package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.store.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Register extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Store store;

    @OneToMany(mappedBy = "register")
    private List<Receipt> receipts = new ArrayList<>();

    protected Register() {}

    public Register(Store store, List<Receipt> receipts) {
        this.store = store;
        this.receipts = receipts;
    }

    public Store getStore() {
        return store;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }
}