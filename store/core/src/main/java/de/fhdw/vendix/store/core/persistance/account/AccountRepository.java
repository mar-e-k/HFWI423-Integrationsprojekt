package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.store.core.persistance.account_role.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUuid(UUID uuid);

    boolean existsByUsername(String username);

    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByUsername(String username);

    @Query(
                """
                SELECT ara.role
                FROM AccountRoleAssignment ara
                WHERE ara.account.id = :accountId
                """
    )
    Set<AccountRole> findAllAccountRolesByAccountId(@Param("accountId") Long id);
}