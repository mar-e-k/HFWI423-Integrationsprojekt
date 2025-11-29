package fhdw.de.einkauf_service.exception;

/**
 * Exception thrown when attempting to place an article on a shelf level
 * where it would overlap with an existing placement.
 */
public class OverlapException extends RuntimeException {

    public OverlapException(String message) {
        super(message);
    }

    public OverlapException(String message, Throwable cause) {
        super(message, cause);
    }
}
