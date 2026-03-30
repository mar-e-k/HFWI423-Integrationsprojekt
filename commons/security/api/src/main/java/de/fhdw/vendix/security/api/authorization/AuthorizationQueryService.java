package de.fhdw.vendix.security.api.authorization;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.commons.api.structure.service.QueryService;

import java.util.Set;

interface AuthorizationQueryService extends QueryService {
    Set<AccountRole> findRolesByAccountId(Long accountId);

    boolean isAccountLocked(Long accountId);
}