package org.swiftbeard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for creating a new todo.
 */
public class TodoCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    public String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    public String description;

    public Boolean completed = false;

    @NotNull(message = "User ID is required")
    public Long userId;

    public LocalDateTime dueDate;
}
