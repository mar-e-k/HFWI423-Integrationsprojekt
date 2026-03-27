package de.fhdw.vendix.commons.api.domain.account.port;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountQueryPort extends QueryPort {
    Optional<AccountDTO> findByUsername(String username);

    Optional<AccountDTO> findByUUID(UUID uuid);

    Optional<AccountDTO> findByPhone(String phone);

    Optional<AccountDTO> findByEmail(String email);

    Set<AccountRoleDTO> findAllRoles(Long id);
}