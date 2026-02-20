package de.fhdw.vendix.commons.api.domain.account.port;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;

public interface AccountQueryPort extends QueryPort {
    Optional<AccountDTO> findByID(String id);
    Optional<AccountDTO> findByUUID(String id);
    Optional<AccountDTO> findByUsername(String username);
}