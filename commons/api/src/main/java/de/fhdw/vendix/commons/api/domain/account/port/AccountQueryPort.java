package de.fhdw.vendix.commons.api.domain.account.port;

import de.fhdw.vendix.commons.api.domain.account.web.AccountQueryApi;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Set;

public interface AccountQueryPort extends QueryPort, AccountQueryApi {
    Set<AccountRoleEnum> findAllRoles(Long accountId);
}