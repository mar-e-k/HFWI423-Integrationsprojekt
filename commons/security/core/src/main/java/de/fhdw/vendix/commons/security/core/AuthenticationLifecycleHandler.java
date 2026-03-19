package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.security.api.authentication.AppContext;
import de.fhdw.vendix.security.api.authentication.AuthContext;

public interface AuthenticationLifecycleHandler {
    void onApplicationStart(AppContext appContext);

    void onApplicationShutdown(AppContext appContext);

    void onAuthenticationSuccess(AuthContext context);

    void onAuthenticationLogout(AuthContext context);
}