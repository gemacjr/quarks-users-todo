package org.swiftbeard.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing todo.
 * All fields are optional for partial updates.
 */
public class TodoUpdateRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    public String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    public String description;

    public Boolean completed;

    public LocalDateTime dueDate;
}
