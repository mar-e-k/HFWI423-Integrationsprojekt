package de.fhdw.vendix.commons.api.domain.account.port;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;
import java.util.UUID;

public interface AccountQueryPort extends QueryPort {
    Optional<AccountDTO> findByAccount(AccountDTO accountDTO);
    Optional<AccountDTO> findByID(String id);
    Optional<AccountDTO> findByUUID(UUID uuid);
    Optional<AccountDTO> findByUsername(String username);
}