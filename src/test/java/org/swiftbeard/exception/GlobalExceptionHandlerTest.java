package org.swiftbeard.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests exception mapping and error response formatting.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new RuntimeException("Test error message");

        Response response = handler.toResponse(exception);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        GlobalExceptionHandler.ErrorResponse error =
            (GlobalExceptionHandler.ErrorResponse) response.getEntity();

        assertNotNull(error);
        assertEquals("Internal server error", error.error);
        assertEquals("Test error message", error.message);
    }

    @Test
    void testHandleNullPointerException() {
        Exception exception = new NullPointerException("Null value encountered");

        Response response = handler.toResponse(exception);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        GlobalExceptionHandler.ErrorResponse error =
            (GlobalExceptionHandler.ErrorResponse) response.getEntity();

        assertNotNull(error);
        assertEquals("Internal server error", error.error);
        assertEquals("Null value encountered", error.message);
    }

    @Test
    void testHandleConstraintViolationException() {
        // Create mock constraint violations
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation1 = createMockViolation("username", "Username is required");
        ConstraintViolation<?> violation2 = createMockViolation("email", "Email must be valid");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Response response = handler.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        GlobalExceptionHandler.ValidationErrorResponse error =
            (GlobalExceptionHandler.ValidationErrorResponse) response.getEntity();

        assertNotNull(error);
        assertEquals("Validation failed", error.error);
        assertNotNull(error.violations);
        assertEquals(2, error.violations.size());
        assertTrue(error.violations.containsKey("username"));
        assertTrue(error.violations.containsKey("email"));
        assertEquals("Username is required", error.violations.get("username"));
        assertEquals("Email must be valid", error.violations.get("email"));
    }

    @Test
    void testHandleConstraintViolationExceptionSingleViolation() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = createMockViolation("title", "Title is required");
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Response response = handler.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        GlobalExceptionHandler.ValidationErrorResponse error =
            (GlobalExceptionHandler.ValidationErrorResponse) response.getEntity();

        assertNotNull(error);
        assertEquals(1, error.violations.size());
        assertEquals("Title is required", error.violations.get("title"));
    }

    @Test
    void testHandleConstraintViolationExceptionWithNestedPropertyPath() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = createMockViolation(
            "userRequest.username",
            "Username must be between 3 and 50 characters"
        );
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Response response = handler.toResponse(exception);

        GlobalExceptionHandler.ValidationErrorResponse error =
            (GlobalExceptionHandler.ValidationErrorResponse) response.getEntity();

        assertNotNull(error);
        // Should extract the last part of the property path
        assertTrue(error.violations.containsKey("username"));
        assertEquals("Username must be between 3 and 50 characters", error.violations.get("username"));
    }

    @Test
    void testHandleConstraintViolationExceptionMultipleViolationsSameField() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation1 = createMockViolation("password", "Password is required");
        ConstraintViolation<?> violation2 = createMockViolation("password", "Password must be at least 8 characters");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Response response = handler.toResponse(exception);

        GlobalExceptionHandler.ValidationErrorResponse error =
            (GlobalExceptionHandler.ValidationErrorResponse) response.getEntity();

        assertNotNull(error);
        assertTrue(error.violations.containsKey("password"));

        // Should combine multiple violations for same field
        String passwordError = error.violations.get("password");
        assertTrue(passwordError.contains("Password is required") ||
                   passwordError.contains("Password must be at least 8 characters"));
    }

    @Test
    void testErrorResponseStructure() {
        GlobalExceptionHandler.ErrorResponse error = new GlobalExceptionHandler.ErrorResponse();
        error.error = "Test Error";
        error.message = "Test Message";

        assertEquals("Test Error", error.error);
        assertEquals("Test Message", error.message);
    }

    @Test
    void testValidationErrorResponseStructure() {
        GlobalExceptionHandler.ValidationErrorResponse error =
            new GlobalExceptionHandler.ValidationErrorResponse();
        error.error = "Validation failed";
        error.violations = Map.of(
            "field1", "Error 1",
            "field2", "Error 2"
        );

        assertEquals("Validation failed", error.error);
        assertNotNull(error.violations);
        assertEquals(2, error.violations.size());
        assertEquals("Error 1", error.violations.get("field1"));
        assertEquals("Error 2", error.violations.get("field2"));
    }

    @Test
    void testHandleExceptionWithNullMessage() {
        Exception exception = new RuntimeException();

        Response response = handler.toResponse(exception);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        GlobalExceptionHandler.ErrorResponse error =
            (GlobalExceptionHandler.ErrorResponse) response.getEntity();

        assertNotNull(error);
        assertEquals("Internal server error", error.error);
        // Message can be null
        assertNull(error.message);
    }

    @Test
    void testConstraintViolationExceptionEmptyViolations() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Response response = handler.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        GlobalExceptionHandler.ValidationErrorResponse error =
            (GlobalExceptionHandler.ValidationErrorResponse) response.getEntity();

        assertNotNull(error);
        assertEquals("Validation failed", error.error);
        assertNotNull(error.violations);
        assertTrue(error.violations.isEmpty());
    }

    // Helper method to create mock constraint violations
    @SuppressWarnings("unchecked")
    private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn(message);

        return violation;
    }
}
