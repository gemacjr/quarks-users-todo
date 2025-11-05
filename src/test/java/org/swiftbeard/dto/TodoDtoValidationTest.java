package org.swiftbeard.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Todo DTO validation.
 * Tests validation annotations on TodoCreateRequest and TodoUpdateRequest.
 */
class TodoDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // TodoCreateRequest Tests

    @Test
    void testTodoCreateRequestValidData() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Todo Title";
        request.description = "Valid description";
        request.completed = false;
        request.userId = 1L;
        request.dueDate = LocalDateTime.now().plusDays(7);

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid data should not have violations");
    }

    @Test
    void testTodoCreateRequestValidDataMinimalFields() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.userId = 1L;
        // description, completed, and dueDate are optional

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestBlankTitle() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "";
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTodoCreateRequestNullTitle() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = null;
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTodoCreateRequestTitleTooLong() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "a".repeat(201); // More than 200 characters
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title") &&
                          v.getMessage().contains("200")));
    }

    @Test
    void testTodoCreateRequestTitleMaxLength() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "a".repeat(200); // Exactly 200 characters
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestDescriptionTooLong() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.description = "a".repeat(1001); // More than 1000 characters
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("description") &&
                          v.getMessage().contains("1000")));
    }

    @Test
    void testTodoCreateRequestDescriptionMaxLength() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.description = "a".repeat(1000); // Exactly 1000 characters
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestNullDescription() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.description = null;
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        // Description is optional, null should be valid
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestNullUserId() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.userId = null;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }

    @Test
    void testTodoCreateRequestDefaultCompletedValue() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.userId = 1L;
        // completed should default to false

        assertEquals(false, request.completed);
    }

    @Test
    void testTodoCreateRequestCompletedTrue() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.completed = true;
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestCompletedFalse() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.completed = false;
        request.userId = 1L;

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestWithDueDate() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.userId = 1L;
        request.dueDate = LocalDateTime.now().plusDays(7);

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestWithPastDueDate() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Valid Title";
        request.userId = 1L;
        request.dueDate = LocalDateTime.now().minusDays(7);

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        // No constraint on past dates, should be valid
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoCreateRequestAllFieldsInvalid() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = ""; // Blank
        request.description = "a".repeat(1001); // Too long
        request.userId = null; // Null

        Set<ConstraintViolation<TodoCreateRequest>> violations = validator.validate(request);

        // Should have violations for title, description, and userId
        assertTrue(violations.size() >= 3);
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }

    // TodoUpdateRequest Tests

    @Test
    void testTodoUpdateRequestValidDataAllFields() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.title = "Updated Title";
        request.description = "Updated description";
        request.completed = true;
        request.dueDate = LocalDateTime.now().plusDays(3);

        Set<ConstraintViolation<TodoUpdateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoUpdateRequestValidDataPartialFields() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.title = "Only Title Updated";
        // Other fields are null (optional in update)

        Set<ConstraintViolation<TodoUpdateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoUpdateRequestOnlyCompletedField() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.completed = true;

        Set<ConstraintViolation<TodoUpdateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodoUpdateRequestInvalidTitle() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.title = "a".repeat(201); // Too long

        Set<ConstraintViolation<TodoUpdateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTodoUpdateRequestInvalidDescription() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.description = "a".repeat(1001); // Too long

        Set<ConstraintViolation<TodoUpdateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void testTodoUpdateRequestEmptyTitleShouldFail() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.title = ""; // Empty should fail if provided

        Set<ConstraintViolation<TodoUpdateRequest>> violations = validator.validate(request);

        // Empty strings should fail validation even in update
        assertFalse(violations.isEmpty());
    }

    @Test
    void testTodoUpdateRequestNullFieldsAreValid() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        // All fields null - valid for partial update

        Set<ConstraintViolation<TodoUpdateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
