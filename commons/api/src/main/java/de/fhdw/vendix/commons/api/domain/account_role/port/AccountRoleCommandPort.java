package de.fhdw.vendix.commons.api.domain.account_role.port;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

public interface AccountRoleCommandPort extends CommandPort {

    AccountRoleDTO create(AccountRoleDTO entity);
}