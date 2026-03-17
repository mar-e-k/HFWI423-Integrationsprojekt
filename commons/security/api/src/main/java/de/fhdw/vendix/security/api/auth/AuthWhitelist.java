package de.fhdw.vendix.security.api.auth;

import java.util.Set;

public final class AuthWhitelist {

    public static final Set<String> API_WHITELIST = Set.of(
            "/login",
            "/logout",
            "/favicon.ico",
            "/robots.txt",
            "/manifest.webmanifest",
            "/sw.js",
            "/offline-page.html",
            "/icons/**",
            "/images/**"
    );

    private AuthWhitelist() {}
}