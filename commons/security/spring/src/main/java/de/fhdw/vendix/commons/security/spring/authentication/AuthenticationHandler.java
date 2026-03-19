package de.fhdw.vendix.commons.security.spring.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface AuthenticationHandler {

    void login(Authentication authentication, HttpServletRequest req, HttpServletResponse res) throws AuthenticationException;

    void logout(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException;
}