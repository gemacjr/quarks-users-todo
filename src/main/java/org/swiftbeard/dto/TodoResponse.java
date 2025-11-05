package org.swiftbeard.dto;

import org.swiftbeard.entity.Todo;

import java.time.LocalDateTime;

/**
 * DTO for todo responses.
 */
public class TodoResponse {

    public Long id;
    public String title;
    public String description;
    public Boolean completed;
    public Long userId;
    public String userName;
    public LocalDateTime dueDate;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public TodoResponse() {
    }

    public TodoResponse(Todo todo) {
        this.id = todo.id;
        this.title = todo.title;
        this.description = todo.description;
        this.completed = todo.completed;
        this.userId = todo.user != null ? todo.user.id : null;
        this.userName = todo.user != null ? todo.user.name : null;
        this.dueDate = todo.dueDate;
        this.createdAt = todo.createdAt;
        this.updatedAt = todo.updatedAt;
    }

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(todo);
    }
}
