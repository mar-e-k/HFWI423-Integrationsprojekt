package de.fhdw.vendix.commons.spring.security.lifecycle.login;

import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

public interface LoginHandler {
    void onLoginSuccess(InteractiveAuthenticationSuccessEvent event);
}
