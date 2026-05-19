package fhdw.de.einkauf_service.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Liefert strukturierte Fehler nach RFC 7807 (Problem Details for HTTP APIs).
 * Damit erhalten Konsumenten der API einen stabilen, maschinenlesbaren Fehlervertrag.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final URI BASE_TYPE = URI.create("https://einkauf-service/problems");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation-failed",
                "Validierung fehlgeschlagen",
                "Mindestens ein Feld ist ungültig.");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errors.put(fe.getField(), fe.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraint(ConstraintViolationException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "constraint-violation",
                "Constraint-Verletzung", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "illegal-argument",
                "Ungültiges Argument", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(OutOfBoundsException.class)
    public ResponseEntity<ProblemDetail> handleOutOfBounds(OutOfBoundsException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "out-of-bounds",
                "Platzierung außerhalb der Regalgrenzen", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(OverlapException.class)
    public ResponseEntity<ProblemDetail> handleOverlap(OverlapException ex) {
        ProblemDetail pd = problem(HttpStatus.CONFLICT, "placement-overlap",
                "Platzierung überschneidet sich", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler({ EntityNotFoundException.class, NoSuchElementException.class })
    public ResponseEntity<ProblemDetail> handleNotFound(RuntimeException ex) {
        ProblemDetail pd = problem(HttpStatus.NOT_FOUND, "not-found",
                "Ressource nicht gefunden", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail pd = problem(HttpStatus.CONFLICT, "illegal-state",
                "Aktion im aktuellen Zustand nicht möglich", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        ProblemDetail pd = problem(HttpStatus.CONFLICT, "data-integrity",
                "Datenkonflikt",
                "Die Operation verletzt eine Datenbank-Constraint.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAll(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unbehandelter Fehler [traceId={}]", traceId, ex);
        ProblemDetail pd = problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                "Interner Serverfehler",
                "Ein unerwarteter Fehler ist aufgetreten. Bitte traceId mitteilen.");
        pd.setProperty("traceId", traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    private static ProblemDetail problem(HttpStatus status, String typeSlug, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setType(BASE_TYPE.resolve("/" + typeSlug));
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
