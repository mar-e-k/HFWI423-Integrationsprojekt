package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.spring.data.service.CrudService;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountService extends CrudService<Account, Long> {
    boolean existsByUuid(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByPhone(String phone);

    Optional<Account> findByEmail(String email);

    List<AccountRole> findAllRolesById(Long id);

    List<AccountRole> findAllRolesByUuid(UUID uuid);
}