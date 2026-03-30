package de.fhdw.vendix.security.api.context;

public class ContextAlreadySetException extends RuntimeException {
    public ContextAlreadySetException(String message) {
        super(message);
    }
}
