package de.fhdw.vendix.commons.spring.security.context.auth;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.Role;

import java.util.Set;

public interface AuthContext {
    AccountDTO account();

    Set<Role> roles();
}