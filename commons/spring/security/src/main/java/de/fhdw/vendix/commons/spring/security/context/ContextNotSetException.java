package de.fhdw.vendix.commons.spring.security.context;

public class ContextNotSetException extends RuntimeException {
    public ContextNotSetException(String message) {
        super(message);
    }
}
