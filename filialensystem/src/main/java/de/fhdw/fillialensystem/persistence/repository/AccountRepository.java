package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
    Optional<Account> findByUuid(String uuid);
    boolean existsByAccountRole_Role(AccountRoleEnum role);
}