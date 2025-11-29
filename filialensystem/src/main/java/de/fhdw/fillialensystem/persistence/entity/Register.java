package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

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

    public Register(Store store, List<Receipt> receipts) {
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
}