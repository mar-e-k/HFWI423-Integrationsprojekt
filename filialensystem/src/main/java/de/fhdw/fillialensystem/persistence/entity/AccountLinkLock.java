package de.fhdw.fillialensystem.persistence.entity;

import de.fhdw.commons.persistence.entity.GenericEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
public class AccountLinkLock implements GenericEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true)
    private Account account;

    @CreatedDate
    @NotNull(message = "Lock acquired at cannot be null")
    private Instant lockAcquiredAt;

    public AccountLinkLock() {
        super();
    }

    public AccountLinkLock(Account account) {
        this.account = account;
    }

    public AccountLinkLock(Long id, Account account) {
        this.id = id;
        this.account = account;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Instant getLockAcquiredAt() {
        return lockAcquiredAt;
    }
}