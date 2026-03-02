package de.fhdw.vendix.store.core.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUuid(String uuid);
    Optional<Account> findByUsername(String username);
}