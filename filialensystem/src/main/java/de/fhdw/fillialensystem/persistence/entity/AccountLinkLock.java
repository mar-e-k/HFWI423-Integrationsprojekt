package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.*;

@Entity
public class AccountLinkLock extends AbstractLock {

    @ManyToOne
    private Store store;

    @ManyToOne
    private Register register;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true)
    private Account account;

    public AccountLinkLock() {
        super();
    }

    public AccountLinkLock(Store store, Register register, Account account) {
        this.store = store;
        this.register = register;
        this.account = account;
    }

    public AccountLinkLock(Long id, Store store, Register register, Account account) {
        super(id);
        this.store = store;
        this.register = register;
        this.account = account;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}