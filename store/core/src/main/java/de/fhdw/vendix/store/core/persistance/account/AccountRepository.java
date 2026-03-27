package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.store.core.persistance.account_role.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByUsername(String username);

    @Query(
            """
            SELECT a.role
            FROM AccountRoleAssignment a
            WHERE a.account.id = :accountId
            """
    )
    Set<AccountRole> findAllRoles(@Param("accountId") Long accountId);
}