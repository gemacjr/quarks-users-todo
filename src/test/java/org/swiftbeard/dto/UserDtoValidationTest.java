package org.swiftbeard.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User DTO validation.
 * Tests validation annotations on UserCreateRequest and UserUpdateRequest.
 */
class UserDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // UserCreateRequest Tests

    @Test
    void testUserCreateRequestValidData() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "valid@example.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid data should not have violations");
    }

    @Test
    void testUserCreateRequestBlankUsername() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "";
        request.email = "valid@example.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void testUserCreateRequestNullUsername() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = null;
        request.email = "valid@example.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void testUserCreateRequestUsernameTooShort() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "ab"; // Less than 3 characters
        request.email = "valid@example.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username") &&
                          v.getMessage().contains("between 3 and 50")));
    }

    @Test
    void testUserCreateRequestUsernameTooLong() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "a".repeat(51); // More than 50 characters
        request.email = "valid@example.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username") &&
                          v.getMessage().contains("between 3 and 50")));
    }

    @Test
    void testUserCreateRequestUsernameMinLength() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "abc"; // Exactly 3 characters
        request.email = "valid@example.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testUserCreateRequestUsernameMaxLength() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "a".repeat(50); // Exactly 50 characters
        request.email = "valid@example.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testUserCreateRequestBlankEmail() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testUserCreateRequestNullEmail() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = null;
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testUserCreateRequestInvalidEmailFormat() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "invalid-email";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email") &&
                          v.getMessage().contains("valid")));
    }

    @Test
    void testUserCreateRequestInvalidEmailNoAt() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "invalidemail.com";
        request.name = "Valid User";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testUserCreateRequestBlankName() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "valid@example.com";
        request.name = "";

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testUserCreateRequestNullName() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "valid@example.com";
        request.name = null;

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testUserCreateRequestNameTooLong() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "valid@example.com";
        request.name = "a".repeat(101); // More than 100 characters

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                          v.getMessage().contains("100")));
    }

    @Test
    void testUserCreateRequestNameMaxLength() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "validuser";
        request.email = "valid@example.com";
        request.name = "a".repeat(100); // Exactly 100 characters

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testUserCreateRequestAllFieldsInvalid() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "ab"; // Too short
        request.email = "invalid"; // Invalid format
        request.name = ""; // Blank

        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Should have violations for all three fields
        assertTrue(violations.size() >= 3);
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    // UserUpdateRequest Tests

    @Test
    void testUserUpdateRequestValidDataAllFields() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.username = "updateduser";
        request.email = "updated@example.com";
        request.name = "Updated User";

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testUserUpdateRequestValidDataPartialFields() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.name = "Updated Name Only";
        // username and email are null (optional in update)

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testUserUpdateRequestInvalidUsername() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.username = "ab"; // Too short

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void testUserUpdateRequestInvalidEmail() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.email = "invalid-email";

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testUserUpdateRequestEmptyFieldsShouldFail() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.username = ""; // Empty should fail if provided
        request.email = "";

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Empty strings should fail validation even in update
        assertFalse(violations.isEmpty());
    }
}
