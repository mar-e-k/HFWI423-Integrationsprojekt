package de.fhdw.vendix.commons.spring.security.context;

public class ContextAlreadySetException extends RuntimeException {
    public ContextAlreadySetException(String message) {
        super(message);
    }
}
