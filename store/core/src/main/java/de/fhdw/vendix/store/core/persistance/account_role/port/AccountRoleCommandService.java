package de.fhdw.vendix.store.core.persistance.account_role.port;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.service.CommandService;

interface AccountRoleCommandService extends CommandService {
    AccountRoleDTO create(AccountRoleDTO entity);
}