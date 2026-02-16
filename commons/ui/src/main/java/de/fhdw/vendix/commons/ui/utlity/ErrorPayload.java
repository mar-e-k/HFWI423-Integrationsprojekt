package de.fhdw.vendix.commons.ui.utlity;

import java.time.Instant;
import java.util.Arrays;

public record ErrorPayload(
        String title,
        String message,
        String stackTrace,
        Instant timestamp
) {
    public ErrorPayload {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (stackTrace == null || stackTrace.isBlank()) {
            throw new IllegalArgumentException("Stack trace cannot be null or empty");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
    }

    public static ErrorPayload fromThrowable(Throwable throwable) {
        return new ErrorPayload(
                throwable.getClass().getSimpleName(),
                throwable.getMessage() != null ? throwable.getMessage() : "Unexpected error",
                Arrays.toString(throwable.getStackTrace()),
                Instant.now()
        );
    }

    public static ErrorPayload fromException(Exception exception) {
        return new ErrorPayload(
                exception.getClass().getSimpleName(),
                exception.getMessage() != null ? exception.getMessage() : "Unexpected error",
                Arrays.toString(exception.getStackTrace()),
                Instant.now()
        );
    }
}