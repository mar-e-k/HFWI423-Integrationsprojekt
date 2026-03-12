package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.store.core.persistance.account_role.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUuid(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByRole(AccountRoleEnum role);

    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByUsername(String username);

    Set<AccountRole> findRolesForAccount(long id);
}