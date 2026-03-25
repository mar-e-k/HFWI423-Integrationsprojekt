package de.fhdw.vendix.commons.api.domain.account.web;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.structure.web.QueryApi;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountQueryApi extends QueryApi {
    Optional<AccountDTO> findByUsername(String username);

    Optional<AccountDTO> findByUUID(UUID uuid);

    Optional<AccountDTO> findByPhone(String phone);

    Optional<AccountDTO> findByEmail(String email);

    Set<AccountRoleEnum> findAllRoles(Long id);
}