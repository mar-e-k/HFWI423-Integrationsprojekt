package de.fhdw.vendix.security.api.authentication;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.web.QueryApi;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationQueryApi extends QueryApi {
    Optional<AccountDTO> findByUsername(String username);

    Optional<AccountDTO> findByUUID(UUID uuid);

    Optional<AccountDTO> findByPhone(String phone);

    Optional<AccountDTO> findByEmail(String email);
}