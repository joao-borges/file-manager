package br.com.joaoborges.filemanager.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the application
 *
 * Provides centralized exception handling for all controllers using @RestControllerAdvice.
 * Handles validation errors, application-specific exceptions, and unexpected errors.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations
     * Returns a map of field names to error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .collect(Collectors.toMap(
                error -> ((FieldError) error).getField(),
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing
            ));

        log.warn("Validation error: {}", errors);

        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "success", false,
                "message", "Validation failed",
                "errors", errors
            ));
    }

    /**
     * Handles FileManagerException - application-specific business logic errors
     */
    @ExceptionHandler(FileManagerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleFileManagerException(
            FileManagerException ex) {

        log.error("FileManager error: {}", ex.getMessage(), ex);

        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "success", false,
                "message", ex.getMessage()
            ));
    }

    /**
     * Handles SecurityException - security-related errors (path traversal, etc.)
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, Object>> handleSecurityException(
            SecurityException ex) {

        log.error("Security violation: {}", ex.getMessage(), ex);

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Map.of(
                "success", false,
                "message", "Security violation: " + ex.getMessage()
            ));
    }

    /**
     * Handles IllegalArgumentException - invalid arguments
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.warn("Invalid argument: {}", ex.getMessage());

        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "success", false,
                "message", ex.getMessage()
            ));
    }

    /**
     * Handles all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {

        log.error("Unexpected error occurred", ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "success", false,
                "message", "An unexpected error occurred. Please try again later.",
                "error", ex.getClass().getSimpleName()
            ));
    }
}
