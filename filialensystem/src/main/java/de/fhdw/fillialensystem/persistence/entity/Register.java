package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Register extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Store store;

    @OneToMany(mappedBy = "register")
    private List<Receipt> receipts = new ArrayList<>();

    public Register() {
        super();
    }

    public Register(Long id) {
        super(id);
    }

    public Register(Store store, List<Receipt> receipts) {
        this.store = store;
        this.receipts = receipts;
    }

    public Register(Long id, Store store, List<Receipt> receipts) {
        super(id);
        this.store = store;
        this.receipts = receipts;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(getId(), ((Register) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(store, receipts);
    }
}