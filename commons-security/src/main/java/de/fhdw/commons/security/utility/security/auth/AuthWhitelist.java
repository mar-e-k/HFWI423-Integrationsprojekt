package de.fhdw.commons.security.utility.security.auth;

public final class AuthWhitelist {

    public static final String[] WHITELIST = {
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
            "/HEARTBEAT/**",
    };

    private AuthWhitelist() {}
}