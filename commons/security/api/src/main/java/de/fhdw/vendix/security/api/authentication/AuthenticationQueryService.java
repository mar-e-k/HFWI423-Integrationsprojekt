package de.fhdw.vendix.security.api.authentication;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.structure.service.QueryService;

import java.util.Optional;
import java.util.UUID;

interface AuthenticationQueryService extends QueryService {
    Optional<AccountDTO> findByUUID(UUID uuid);

    Optional<AccountDTO> findByUsername(String username);

    Optional<AccountDTO> findByPhone(String phone);

    Optional<AccountDTO> findByEmail(String email);
}