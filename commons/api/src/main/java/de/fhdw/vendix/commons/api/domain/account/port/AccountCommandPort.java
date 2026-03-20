package de.fhdw.vendix.commons.api.domain.account.port;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

public interface AccountCommandPort extends CommandPort {

    AccountDTO create(AccountDTO entity);

    void assignRole(Long accountId, Long roleId);

    void removeRole(Long accountId, Long roleId);

    boolean hasRole(Long accountId, Long roleId);
}