package fhdw.de.einkauf_service.exception;

/**
 * Exception thrown when attempting to place an article on a shelf level
 * at a position that extends beyond the shelf boundaries.
 */
public class OutOfBoundsException extends RuntimeException {

    public OutOfBoundsException(String message) {
        super(message);
    }

    public OutOfBoundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
