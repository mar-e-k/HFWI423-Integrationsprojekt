package de.fhdw.vendix.commons.spring.app.lifecycle.login;

import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

public interface LoginHandler {
    void onLoginSuccess(InteractiveAuthenticationSuccessEvent event);
}
