package de.fhdw.vendix.security.api.context;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;

import java.util.Set;
import java.util.UUID;

public interface AuthContext {

    Long accountId();

    UUID accountUUID();

    String accountUsername();

    String accountPassword();

    Set<AccountRoleEnum> accountRoles();
}