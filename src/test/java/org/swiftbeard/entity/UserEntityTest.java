package org.swiftbeard.entity;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity.
 * Tests custom finder methods and lifecycle callbacks.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserEntityTest {

    @Inject
    EntityManager entityManager;

    @Test
    @Order(1)
    void testUserCreation() {
        User user = new User();
        user.username = "testuser";
        user.email = "test@example.com";
        user.name = "Test User";

        assertNull(user.createdAt);
        assertNull(user.updatedAt);
    }

    @Test
    @Order(2)
    @Transactional
    void testUserPrePersistCallback() {
        User user = new User();
        user.username = "newuser" + System.currentTimeMillis();
        user.email = "newuser" + System.currentTimeMillis() + "@example.com";
        user.name = "New User";

        LocalDateTime before = LocalDateTime.now();
        user.persist();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(user.createdAt);
        assertNotNull(user.updatedAt);
        assertTrue(user.createdAt.isAfter(before.minusSeconds(1)));
        assertTrue(user.createdAt.isBefore(after.plusSeconds(1)));
        assertEquals(user.createdAt, user.updatedAt);
    }

    @Test
    @Order(3)
    @Transactional
    void testUserPreUpdateCallback() throws InterruptedException {
        // Create a user
        User user = new User();
        user.username = "updatetest" + System.currentTimeMillis();
        user.email = "updatetest" + System.currentTimeMillis() + "@example.com";
        user.name = "Update Test";
        user.persist();

        LocalDateTime originalCreatedAt = user.createdAt;
        LocalDateTime originalUpdatedAt = user.updatedAt;

        // Wait a bit to ensure time difference
        Thread.sleep(100);

        // Update the user
        user.name = "Updated Name";
        entityManager.merge(user);
        entityManager.flush();

        // Verify timestamps
        assertEquals(originalCreatedAt, user.createdAt, "createdAt should not change");
        assertNotEquals(originalUpdatedAt, user.updatedAt, "updatedAt should change");
        assertTrue(user.updatedAt.isAfter(originalUpdatedAt));
    }

    @Test
    @Order(4)
    void testFindByUsername() {
        // Assuming "john_doe" exists from import.sql
        User user = User.findByUsername("john_doe");

        assertNotNull(user);
        assertEquals("john_doe", user.username);
        assertNotNull(user.email);
        assertNotNull(user.name);
    }

    @Test
    @Order(5)
    void testFindByUsernameNotFound() {
        User user = User.findByUsername("nonexistent_user_xyz");
        assertNull(user);
    }

    @Test
    @Order(6)
    void testFindByEmail() {
        // Assuming this email exists from import.sql
        User user = User.findByEmail("john.doe@example.com");

        assertNotNull(user);
        assertEquals("john.doe@example.com", user.email);
        assertNotNull(user.username);
        assertNotNull(user.name);
    }

    @Test
    @Order(7)
    void testFindByEmailNotFound() {
        User user = User.findByEmail("nonexistent@example.com");
        assertNull(user);
    }

    @Test
    @Order(8)
    void testFindByNameContaining() {
        // Search for "john" - should find users with "john" in their name (case-insensitive)
        List<User> users = User.findByNameContaining("john");

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.stream()
            .anyMatch(u -> u.name.toLowerCase().contains("john")));
    }

    @Test
    @Order(9)
    void testFindByNameContainingCaseInsensitive() {
        // Test case-insensitive search
        List<User> usersLower = User.findByNameContaining("john");
        List<User> usersUpper = User.findByNameContaining("JOHN");
        List<User> usersMixed = User.findByNameContaining("JoHn");

        assertEquals(usersLower.size(), usersUpper.size());
        assertEquals(usersLower.size(), usersMixed.size());
    }

    @Test
    @Order(10)
    void testFindByNameContainingNotFound() {
        List<User> users = User.findByNameContaining("xyznonexistent");

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @Order(11)
    void testFindByNameContainingPartialMatch() {
        // Should find users with partial name match
        List<User> users = User.findByNameContaining("o");

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.stream()
            .allMatch(u -> u.name.toLowerCase().contains("o")));
    }

    @Test
    @Order(12)
    @Transactional
    void testUserTodoRelationship() {
        // Create a user
        User user = new User();
        user.username = "todoreltest" + System.currentTimeMillis();
        user.email = "todorel" + System.currentTimeMillis() + "@example.com";
        user.name = "Todo Relationship Test";
        user.persist();

        // Create todos
        Todo todo1 = new Todo();
        todo1.title = "Test Todo 1";
        todo1.completed = false;
        todo1.user = user;

        Todo todo2 = new Todo();
        todo2.title = "Test Todo 2";
        todo2.completed = false;
        todo2.user = user;

        user.todos.add(todo1);
        user.todos.add(todo2);

        todo1.persist();
        todo2.persist();

        entityManager.flush();
        entityManager.clear();

        // Retrieve user and verify todos
        User retrievedUser = User.findById(user.id);
        assertNotNull(retrievedUser);
        assertEquals(2, retrievedUser.todos.size());
    }

    @Test
    @Order(13)
    void testUserValidation() {
        User user = new User();
        // Test that user object can be created with proper fields
        user.username = "validuser";
        user.email = "valid@example.com";
        user.name = "Valid User";

        assertEquals("validuser", user.username);
        assertEquals("valid@example.com", user.email);
        assertEquals("Valid User", user.name);
    }

    @Test
    @Order(14)
    @Transactional
    void testCascadeDeleteTodos() {
        // Create a user with todos
        User user = new User();
        user.username = "cascadetest" + System.currentTimeMillis();
        user.email = "cascade" + System.currentTimeMillis() + "@example.com";
        user.name = "Cascade Test";
        user.persist();

        Todo todo = new Todo();
        todo.title = "Test Todo for Cascade";
        todo.completed = false;
        todo.user = user;
        todo.persist();

        user.todos.add(todo);

        Long userId = user.id;
        Long todoId = todo.id;

        entityManager.flush();
        entityManager.clear();

        // Delete user and verify todo is also deleted (cascade)
        User userToDelete = User.findById(userId);
        assertNotNull(userToDelete);

        userToDelete.delete();
        entityManager.flush();

        // Verify both user and todo are deleted
        assertNull(User.findById(userId));
        assertNull(Todo.findById(todoId));
    }
}
