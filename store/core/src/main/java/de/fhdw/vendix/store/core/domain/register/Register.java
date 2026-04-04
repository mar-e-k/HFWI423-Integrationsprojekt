package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.store.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.jspecify.annotations.Nullable;

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

    @Default
    protected Register(@Nullable Long id, Store store) {
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