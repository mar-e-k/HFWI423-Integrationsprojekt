package de.fhdw.vendix.commons.spring.web.core;

public enum RoutingHeader {
    STORE_ROUTING("X-Store-ID"),
    REGISTER_ROUTING("X-Register-ID");

    private final String header;

    RoutingHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}