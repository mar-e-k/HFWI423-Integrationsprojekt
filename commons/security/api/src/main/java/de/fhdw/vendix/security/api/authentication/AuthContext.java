package de.fhdw.vendix.security.api.authentication;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;

import java.util.Set;
import java.util.UUID;

public interface AuthContext {
    // Core
    Long accountID();

    UUID accountUUID();

    String accountUsername();

    String accountPassword();

    Set<AccountRoleEnum> accountRoles();

    // Meta-Security
    boolean isAccountNonExpired();

    boolean isAccountNonLocked();

    boolean isCredentialsNonExpired();

    boolean isEnabled();
}