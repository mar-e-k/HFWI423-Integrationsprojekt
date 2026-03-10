package de.fhdw.vendix.security.api.auth;

public interface AuthenticationLifecycleHandler {
    void onApplicationStart(AppContext appContext);

    void onApplicationShutdown(AppContext appContext);

    void onAuthenticationSuccess(AuthContext context);

    void onAuthenticationLogout(AuthContext context);
}