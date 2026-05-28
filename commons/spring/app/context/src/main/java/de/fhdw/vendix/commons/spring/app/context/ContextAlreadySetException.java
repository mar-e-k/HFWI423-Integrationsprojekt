package de.fhdw.vendix.commons.spring.app.context;

public class ContextAlreadySetException extends ContextException {
    public ContextAlreadySetException(String message) {
        super(message);
    }
}
