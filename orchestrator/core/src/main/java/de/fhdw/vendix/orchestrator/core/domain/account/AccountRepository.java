package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AccountRepository extends JpaRepository<Account, Long>{

    boolean existsByUuid(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByUsername(String username);

    @Query(
            """
            SELECT ar
            FROM AccountRole ar
            JOIN AccountRoleAssignment ara
                ON ar.id = ara.roleId
            JOIN Account a
                ON a.id = ara.accountId
            WHERE a.id = :accountId
            """
    )
    List<AccountRole> findAllRolesByAccountId(@Param("accountId") Long id);

    @Query(
            """
            SELECT ar
            FROM AccountRole ar
            JOIN AccountRoleAssignment ara
                ON ar.id = ara.roleId
            JOIN Account a
                ON a.id = ara.accountId
            WHERE a.uuid = :accountUuid
            """
    )
    List<AccountRole> findAllRolesByAccountUuid(@Param("accountUuid") UUID uuid);
}