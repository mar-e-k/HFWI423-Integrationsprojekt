package de.fhdw.vendix.commons.spring.web.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public final class DefaultAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception
    ) throws IOException, ServletException {
        String errorCode = switch (exception) {
            case BadCredentialsException e -> "bad_credentials";
            case LockedException e -> "account_locked";
            case DisabledException e -> "account_disabled";
            default -> "auth_failed";
        };
        log.atInfo().log("Login attempt failed. Reason: {}.", errorCode);
        request.getSession().setAttribute(SessionAttribute.LOGIN_ERROR.name(), errorCode);
        response.sendRedirect("/login");
    }
}