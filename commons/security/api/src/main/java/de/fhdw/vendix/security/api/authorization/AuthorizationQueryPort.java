package de.fhdw.vendix.security.api.authorization;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Set;

public interface AuthorizationQueryPort extends QueryPort {
    Set<AccountRoleEnum> findRolesByAccountId(Long accountId);

    boolean isAccountLocked(Long accountId);
}