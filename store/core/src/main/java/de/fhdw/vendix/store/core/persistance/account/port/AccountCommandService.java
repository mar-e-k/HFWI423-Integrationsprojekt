package de.fhdw.vendix.store.core.persistance.account.port;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.structure.service.CommandService;

interface AccountCommandService extends CommandService {
    AccountDTO create(AccountDTO entity);

    void assignRole(Long accountId, Long roleId);

    void removeRole(Long accountId, Long roleId);
}