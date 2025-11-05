package org.swiftbeard.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Todo entity representing a task/todo item.
 * Each todo belongs to a user.
 */
@Entity
@Table(name = "todos", indexes = {
    @Index(name = "idx_todo_user_id", columnList = "user_id"),
    @Index(name = "idx_todo_status", columnList = "completed"),
    @Index(name = "idx_todo_created_at", columnList = "created_at")
})
public class Todo extends PanacheEntity {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    public String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    public String description;

    @NotNull(message = "Completed status is required")
    @Column(nullable = false)
    public Boolean completed = false;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(name = "due_date")
    public LocalDateTime dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Custom finder methods for optimized queries
    public static java.util.List<Todo> findByUserId(Long userId) {
        return find("user.id", userId).list();
    }

    public static java.util.List<Todo> findByUserIdAndCompleted(Long userId, Boolean completed) {
        return find("user.id = ?1 and completed = ?2", userId, completed).list();
    }

    public static java.util.List<Todo> findCompletedTodos(Boolean completed) {
        return find("completed", completed).list();
    }

    public static long countByUserId(Long userId) {
        return count("user.id", userId);
    }

    public static long countByUserIdAndCompleted(Long userId, Boolean completed) {
        return count("user.id = ?1 and completed = ?2", userId, completed);
    }
}
