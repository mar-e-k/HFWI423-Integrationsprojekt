package de.fhdw.vendix.commons.spring.security.authorization;

import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.structure.service.QueryService;

import java.util.Set;

interface AuthorizationQueryService extends QueryService {

    Set<Role> findRolesByAccountId(long accountId);

    boolean isAccountLocked(long accountId);
}