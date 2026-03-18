package de.fhdw.vendix.store.core.persistance.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUuid(UUID uuid);

    boolean existsByUsername(String username);

    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByUsername(String username);
}