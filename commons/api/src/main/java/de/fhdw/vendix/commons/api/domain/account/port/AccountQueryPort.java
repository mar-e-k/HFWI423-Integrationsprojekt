package de.fhdw.vendix.commons.api.domain.account.port;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountQueryPort extends QueryPort {
    boolean existsByUuid(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByRole(AccountRoleEnum role);

    Optional<AccountDTO> findByID(long id);

    Optional<AccountDTO> findByUUID(UUID uuid);

    Optional<AccountDTO> findByUsername(String username);

    Set<AccountRoleEnum> findRolesForAccount(long id);
}