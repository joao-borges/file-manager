package ca.joaoborges.filemanager.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Unit tests for GlobalExceptionHandler
 *
 * Tests exception handling and error response formatting
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleFileManagerException() {
        // Given
        final FileManagerException exception = new FileManagerException("Test error message");

        // When
        final ResponseEntity<Map<String, Object>> response = handler.handleFileManagerException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        final Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Test error message", body.get("message"));
    }

    @Test
    void testHandleSecurityException() {
        // Given
        final SecurityException exception = new SecurityException("Security violation");

        // When
        final ResponseEntity<Map<String, Object>> response = handler.handleSecurityException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        final Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("Security violation"));
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Given
        final IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        final ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        final Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Invalid argument", body.get("message"));
    }

    @Test
    void testHandleValidationExceptions() {
        // Given
        final Object target = new Object();
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
        bindingResult.addError(new FieldError("testObject", "field1", "Field 1 is required"));
        bindingResult.addError(new FieldError("testObject", "field2", "Field 2 is invalid"));

        final MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        final ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        final Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Validation failed", body.get("message"));

        @SuppressWarnings("unchecked")
        final Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertNotNull(errors);
        assertTrue(errors.containsKey("field1"));
        assertTrue(errors.containsKey("field2"));
    }

    @Test
    void testHandleGenericException() {
        // Given
        final Exception exception = new RuntimeException("Unexpected error");

        // When
        final ResponseEntity<Map<String, Object>> response = handler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        final Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("unexpected error"));
        assertEquals("RuntimeException", body.get("error"));
    }

}
