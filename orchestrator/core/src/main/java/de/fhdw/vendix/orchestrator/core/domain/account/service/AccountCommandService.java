package de.fhdw.vendix.orchestrator.core.domain.account.service;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface AccountCommandService extends CrudCommandService<AccountDTO, Long> {
    void assignRole(long accountId, long roleId);

    void removeRole(long accountId, long roleId);
}