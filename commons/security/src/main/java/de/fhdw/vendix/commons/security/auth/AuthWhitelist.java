package de.fhdw.vendix.commons.security.auth;

import java.util.Set;

public final class AuthWhitelist {

    public static final Set<String> API_WHITELIST = Set.of(
            "/",
            "/login",
            "/favicon.ico",
            "/robots.txt",
            "/manifest.webmanifest",
            "/sw.js",
            "/offline-page.html",
            "/icons/**",
            "/images/**",
            "/frontend/**",
            "/webjars/**",
            "/VAADIN/**",
            "/vaadinServlet/**",
            "/connect/**",
            "/UIDL/**",
            "/HEARTBEAT/**"
    );

    private AuthWhitelist() {}
}