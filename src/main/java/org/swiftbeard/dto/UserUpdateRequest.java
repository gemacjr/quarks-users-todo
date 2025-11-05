package org.swiftbeard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing user.
 * All fields are optional for partial updates.
 */
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    public String username;

    @Email(message = "Email must be valid")
    public String email;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    public String name;
}
