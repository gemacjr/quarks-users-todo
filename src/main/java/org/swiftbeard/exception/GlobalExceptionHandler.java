package org.swiftbeard.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Provides consistent error responses across all endpoints.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ConstraintViolationException) {
            return handleConstraintViolation((ConstraintViolationException) exception);
        }

        // Log the exception (in production, use proper logging)
        System.err.println("Unhandled exception: " + exception.getMessage());
        exception.printStackTrace();

        // Generic error response
        ErrorResponse error = new ErrorResponse();
        error.error = "Internal server error";
        error.message = exception.getMessage();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
    }

    private Response handleConstraintViolation(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        violation -> getPropertyName(violation.getPropertyPath().toString()),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        ValidationErrorResponse error = new ValidationErrorResponse();
        error.error = "Validation failed";
        error.violations = errors;

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .build();
    }

    private String getPropertyName(String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }

    public static class ErrorResponse {
        public String error;
        public String message;
    }

    public static class ValidationErrorResponse {
        public String error;
        public Map<String, String> violations;
    }
}
