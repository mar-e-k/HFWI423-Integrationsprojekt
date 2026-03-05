package de.fhdw.vendix.security.api.auth;

public interface AuthenticationLifecycleHandler {
    void onAuthenticationSuccess(AuthContext context);

    void onAuthenticationLogout(AuthContext context);
}