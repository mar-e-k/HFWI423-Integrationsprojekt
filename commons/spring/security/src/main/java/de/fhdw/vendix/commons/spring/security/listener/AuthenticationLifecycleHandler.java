package de.fhdw.vendix.commons.spring.security.listener;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.auth.AuthContext;

public interface AuthenticationLifecycleHandler {
    void onApplicationStart(AppContext appContext);

    void onApplicationShutdown(AppContext appContext);

    void onAuthenticationSuccess(AuthContext context);

    void onAuthenticationLogout(AuthContext context);
}