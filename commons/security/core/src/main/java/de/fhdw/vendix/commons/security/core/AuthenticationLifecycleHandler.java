package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.context.AuthContext;

public interface AuthenticationLifecycleHandler {
    void onApplicationStart(AppContext appContext);

    void onApplicationShutdown(AppContext appContext);

    void onAuthenticationSuccess(AuthContext context);

    void onAuthenticationLogout(AuthContext context);
}