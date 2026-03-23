package de.fhdw.vendix.commons.api.specification.authorization;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.structure.web.QueryApi;

import java.util.Set;

public interface AuthorizationQueryApi extends QueryApi {
    Set<AccountRoleEnum> findRolesByAccountId(Long accountId);

    boolean isAccountLocked(Long accountId);
}