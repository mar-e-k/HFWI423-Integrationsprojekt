package de.fhdw.vendix.store.core.persistance.account.port;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface AccountCommandService extends CrudCommandService<AccountDTO, Long> {
    void assignRole(Long accountId, Long roleId);

    void removeRole(Long accountId, Long roleId);
}