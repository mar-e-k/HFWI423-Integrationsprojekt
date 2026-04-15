package fhdw.de.einkauf_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleAll(Exception ex) {
        log.error("GLOBAL ERROR", ex);
        return ex.getMessage();
    }
}
