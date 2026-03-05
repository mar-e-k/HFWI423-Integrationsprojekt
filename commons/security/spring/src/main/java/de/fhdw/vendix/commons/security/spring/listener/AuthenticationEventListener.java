package de.fhdw.vendix.commons.security.spring.listener;

import de.fhdw.vendix.security.api.auth.AuthContext;
import de.fhdw.vendix.security.api.auth.AuthenticationLifecycleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

public final class AuthenticationEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationEventListener.class);

    private final AuthenticationLifecycleHandler authenticationLifecycleHandler;

    public AuthenticationEventListener(AuthenticationLifecycleHandler authenticationLifecycleHandler) {
        this.authenticationLifecycleHandler = authenticationLifecycleHandler;
    }

    @Async
    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof AuthContext authContext) {
            authenticationLifecycleHandler.onAuthenticationSuccess(authContext);
        } else  {
            log.atError().log("Unexpected authentication event type '{}'", event.getAuthentication().getClass());
        }
    }

    @Async
    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof AuthContext authContext) {
            authenticationLifecycleHandler.onAuthenticationLogout(authContext);
        } else  {
            log.atError().log("Unexpected authentication event type '{}'", event.getAuthentication().getClass());
        }
    }
}