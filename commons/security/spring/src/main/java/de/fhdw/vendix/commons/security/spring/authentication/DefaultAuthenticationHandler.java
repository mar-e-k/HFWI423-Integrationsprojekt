package de.fhdw.vendix.commons.security.spring.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

public class DefaultAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultAuthenticationHandler.class);

    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public DefaultAuthenticationHandler(ApplicationEventPublisher eventPublisher, AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.eventPublisher = eventPublisher;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public void login(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("Parameter 'authentication' cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Parameter 'request' cannot be null");
        }
        if (response == null) {
            throw new IllegalArgumentException("Parameter 'response' cannot be null");
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.atDebug().log("Overwriting existing login");
        }

        // Publishes AuthenticationSuccessEvent / AuthenticationFailureEvent
        Authentication trusted = authenticationManager.authenticate(authentication);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(trusted);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        if (request == null) {
            throw new IllegalArgumentException("Parameter 'request' cannot be null");
        }
        if (response == null) {
            throw new IllegalArgumentException("Parameter 'response' cannot be null");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.atDebug().log("Logout attempt failed. Cannot log out as nothing is logged in");
        } else {
            SecurityContextHolder.clearContext();
            securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);
            eventPublisher.publishEvent(new LogoutSuccessEvent(authentication));
        }
    }
}