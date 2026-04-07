package de.fhdw.vendix.commons.spring.security.lifecycle.logout;

import org.springframework.security.authentication.event.LogoutSuccessEvent;

public interface LogoutHandler {
    void onLogoutSuccess(LogoutSuccessEvent event);
}