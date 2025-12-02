package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountLinkLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountLinkLockRepository extends JpaRepository<AccountLinkLock, Long> {
    void deleteByAccount(Account account);
}