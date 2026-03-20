package de.fhdw.vendix.commons.security.spring.listener;

import de.fhdw.vendix.commons.security.spring.context.DefaultUser;
import de.fhdw.vendix.commons.security.core.AuthenticationLifecycleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

public final class AuthenticationEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationEventListener.class);

    private final AuthenticationLifecycleHandler authenticationLifecycleHandler;

    public AuthenticationEventListener(AuthenticationLifecycleHandler authenticationLifecycleHandler) {
        this.authenticationLifecycleHandler = authenticationLifecycleHandler;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        log.atInfo().log(event.toString());
        log.atInfo().log(event.getAuthentication().toString());
        if (event.getAuthentication().getPrincipal() instanceof DefaultUser userDetails) {
            authenticationLifecycleHandler.onAuthenticationSuccess(userDetails.ctx());
        } else  {
            log.atError().log("Unexpected authentication event type '{}'", event.getAuthentication().getClass());
            log.atError().log("Unexpected authentication event type '{}'", event.getAuthentication().getPrincipal());
        }
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        log.atInfo().log(event.toString());
        log.atInfo().log(event.getAuthentication().toString());
        if (event.getAuthentication().getPrincipal() instanceof DefaultUser userDetails) {
            authenticationLifecycleHandler.onAuthenticationLogout(userDetails.ctx());
        } else  {
            log.atError().log("Unexpected authentication event type '{}'", event.getAuthentication().getClass());
            log.atError().log("Unexpected authentication event type '{}'", event.getAuthentication().getPrincipal());
        }
    }
}