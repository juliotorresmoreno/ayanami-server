package us.onnasoft.ayanami.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        errors.put("message", "Validation failed");
        errors.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        errors.put("path", request.getRequestURI());
        errors.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        if (ex instanceof ApiErrorResponse rse) {
            return ResponseEntity.status(rse.getStatus()).body(rse);
        }
        final var apiErrorResponse = new ApiErrorResponse(ex.getMessage(), ex);
        return ResponseEntity.status(apiErrorResponse.getStatus()).body(apiErrorResponse);
    }
}
