package de.fhdw.vendix.security.api.authentication;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationQueryPort extends QueryPort {
    Optional<AccountDTO> findByUsername(String username);

    Optional<AccountDTO> findByUUID(UUID uuid);

    Optional<AccountDTO> findByPhone(String phone);

    Optional<AccountDTO> findByEmail(String email);
}