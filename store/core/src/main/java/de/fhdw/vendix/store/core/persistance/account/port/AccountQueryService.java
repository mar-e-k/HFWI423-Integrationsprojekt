package de.fhdw.vendix.store.core.persistance.account.port;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.service.QueryService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface AccountQueryService extends QueryService {
    Optional<AccountDTO> findByUsername(String username);

    Optional<AccountDTO> findByUUID(UUID uuid);

    Optional<AccountDTO> findByPhone(String phone);

    Optional<AccountDTO> findByEmail(String email);

    Set<AccountRoleDTO> findAllRoles(Long id);
}