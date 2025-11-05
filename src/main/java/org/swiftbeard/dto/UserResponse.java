package org.swiftbeard.dto;

import org.swiftbeard.entity.User;

import java.time.LocalDateTime;

/**
 * DTO for user responses.
 * Excludes sensitive information and controls what data is exposed.
 */
public class UserResponse {

    public Long id;
    public String username;
    public String email;
    public String name;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public UserResponse() {
    }

    public UserResponse(User user) {
        this.id = user.id;
        this.username = user.username;
        this.email = user.email;
        this.name = user.name;
        this.createdAt = user.createdAt;
        this.updatedAt = user.updatedAt;
    }

    public static UserResponse from(User user) {
        return new UserResponse(user);
    }
}
