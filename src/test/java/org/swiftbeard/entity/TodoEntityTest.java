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
 * Unit tests for Todo entity.
 * Tests custom finder methods and lifecycle callbacks.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoEntityTest {

    @Inject
    EntityManager entityManager;

    private static Long testUserId;

    @BeforeAll
    @Transactional
    static void setupTestUser() {
        // Create a test user for all tests
        User testUser = new User();
        testUser.username = "todoentitytest" + System.currentTimeMillis();
        testUser.email = "todoentity" + System.currentTimeMillis() + "@example.com";
        testUser.name = "Todo Entity Test User";
        testUser.persist();
        testUserId = testUser.id;
    }

    @Test
    @Order(1)
    void testTodoCreation() {
        Todo todo = new Todo();
        todo.title = "Test Todo";
        todo.description = "Test Description";
        todo.completed = false;

        assertNull(todo.createdAt);
        assertNull(todo.updatedAt);
    }

    @Test
    @Order(2)
    @Transactional
    void testTodoPrePersistCallback() {
        User user = User.findById(testUserId);
        assertNotNull(user);

        Todo todo = new Todo();
        todo.title = "New Todo";
        todo.description = "Description";
        todo.completed = false;
        todo.user = user;

        LocalDateTime before = LocalDateTime.now();
        todo.persist();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(todo.createdAt);
        assertNotNull(todo.updatedAt);
        assertTrue(todo.createdAt.isAfter(before.minusSeconds(1)));
        assertTrue(todo.createdAt.isBefore(after.plusSeconds(1)));
        assertEquals(todo.createdAt, todo.updatedAt);
    }

    @Test
    @Order(3)
    @Transactional
    void testTodoPreUpdateCallback() throws InterruptedException {
        User user = User.findById(testUserId);
        assertNotNull(user);

        // Create a todo
        Todo todo = new Todo();
        todo.title = "Update Test Todo";
        todo.completed = false;
        todo.user = user;
        todo.persist();

        LocalDateTime originalCreatedAt = todo.createdAt;
        LocalDateTime originalUpdatedAt = todo.updatedAt;

        // Wait to ensure time difference
        Thread.sleep(100);

        // Update the todo
        todo.title = "Updated Title";
        entityManager.merge(todo);
        entityManager.flush();

        // Verify timestamps
        assertEquals(originalCreatedAt, todo.createdAt, "createdAt should not change");
        assertNotEquals(originalUpdatedAt, todo.updatedAt, "updatedAt should change");
        assertTrue(todo.updatedAt.isAfter(originalUpdatedAt));
    }

    @Test
    @Order(4)
    void testFindByUserId() {
        // Assuming user 1 exists from import.sql with todos
        List<Todo> todos = Todo.findByUserId(1L);

        assertNotNull(todos);
        assertFalse(todos.isEmpty());
        assertTrue(todos.stream().allMatch(t -> t.user.id.equals(1L)));
    }

    @Test
    @Order(5)
    void testFindByUserIdNotFound() {
        List<Todo> todos = Todo.findByUserId(999999L);

        assertNotNull(todos);
        assertTrue(todos.isEmpty());
    }

    @Test
    @Order(6)
    void testFindByUserIdAndCompleted() {
        // Find completed todos for user 1
        List<Todo> completedTodos = Todo.findByUserIdAndCompleted(1L, true);

        assertNotNull(completedTodos);
        if (!completedTodos.isEmpty()) {
            assertTrue(completedTodos.stream().allMatch(t ->
                t.user.id.equals(1L) && t.completed == true));
        }
    }

    @Test
    @Order(7)
    void testFindByUserIdAndCompletedPending() {
        // Find pending todos for user 1
        List<Todo> pendingTodos = Todo.findByUserIdAndCompleted(1L, false);

        assertNotNull(pendingTodos);
        if (!pendingTodos.isEmpty()) {
            assertTrue(pendingTodos.stream().allMatch(t ->
                t.user.id.equals(1L) && t.completed == false));
        }
    }

    @Test
    @Order(8)
    void testFindCompletedTodos() {
        List<Todo> completedTodos = Todo.findCompletedTodos(true);

        assertNotNull(completedTodos);
        if (!completedTodos.isEmpty()) {
            assertTrue(completedTodos.stream().allMatch(t -> t.completed == true));
        }
    }

    @Test
    @Order(9)
    void testFindPendingTodos() {
        List<Todo> pendingTodos = Todo.findCompletedTodos(false);

        assertNotNull(pendingTodos);
        if (!pendingTodos.isEmpty()) {
            assertTrue(pendingTodos.stream().allMatch(t -> t.completed == false));
        }
    }

    @Test
    @Order(10)
    void testCountByUserId() {
        long count = Todo.countByUserId(1L);

        assertTrue(count >= 0);

        // If there are todos, verify the count matches the list
        List<Todo> todos = Todo.findByUserId(1L);
        assertEquals(todos.size(), count);
    }

    @Test
    @Order(11)
    void testCountByUserIdNotFound() {
        long count = Todo.countByUserId(999999L);
        assertEquals(0, count);
    }

    @Test
    @Order(12)
    void testCountByUserIdAndCompleted() {
        long completedCount = Todo.countByUserIdAndCompleted(1L, true);
        long pendingCount = Todo.countByUserIdAndCompleted(1L, false);

        assertTrue(completedCount >= 0);
        assertTrue(pendingCount >= 0);

        // Verify counts match the lists
        List<Todo> completedTodos = Todo.findByUserIdAndCompleted(1L, true);
        List<Todo> pendingTodos = Todo.findByUserIdAndCompleted(1L, false);

        assertEquals(completedTodos.size(), completedCount);
        assertEquals(pendingTodos.size(), pendingCount);
    }

    @Test
    @Order(13)
    @Transactional
    void testTodoDefaultCompletedValue() {
        User user = User.findById(testUserId);
        assertNotNull(user);

        Todo todo = new Todo();
        todo.title = "Default Completed Test";
        todo.user = user;
        todo.persist();

        // Default value should be false
        assertNotNull(todo.completed);
        assertFalse(todo.completed);
    }

    @Test
    @Order(14)
    @Transactional
    void testTodoWithDueDate() {
        User user = User.findById(testUserId);
        assertNotNull(user);

        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

        Todo todo = new Todo();
        todo.title = "Todo with Due Date";
        todo.completed = false;
        todo.user = user;
        todo.dueDate = futureDate;
        todo.persist();

        assertNotNull(todo.dueDate);
        assertEquals(futureDate.toLocalDate(), todo.dueDate.toLocalDate());
    }

    @Test
    @Order(15)
    @Transactional
    void testTodoWithoutDueDate() {
        User user = User.findById(testUserId);
        assertNotNull(user);

        Todo todo = new Todo();
        todo.title = "Todo without Due Date";
        todo.completed = false;
        todo.user = user;
        todo.persist();

        assertNull(todo.dueDate);
    }

    @Test
    @Order(16)
    @Transactional
    void testTodoToggleCompletion() {
        User user = User.findById(testUserId);
        assertNotNull(user);

        Todo todo = new Todo();
        todo.title = "Toggle Test Todo";
        todo.completed = false;
        todo.user = user;
        todo.persist();

        Long todoId = todo.id;

        // Toggle to completed
        todo.completed = true;
        entityManager.merge(todo);
        entityManager.flush();
        entityManager.clear();

        Todo retrieved = Todo.findById(todoId);
        assertTrue(retrieved.completed);

        // Toggle back to pending
        retrieved.completed = false;
        entityManager.merge(retrieved);
        entityManager.flush();
        entityManager.clear();

        Todo retrievedAgain = Todo.findById(todoId);
        assertFalse(retrievedAgain.completed);
    }

    @Test
    @Order(17)
    @Transactional
    void testTodoUserRelationship() {
        User user = User.findById(testUserId);
        assertNotNull(user);

        Todo todo = new Todo();
        todo.title = "Relationship Test Todo";
        todo.completed = false;
        todo.user = user;
        todo.persist();

        assertNotNull(todo.user);
        assertEquals(user.id, todo.user.id);
        assertEquals(user.username, todo.user.username);
    }

    @Test
    @Order(18)
    @Transactional
    void testMultipleTodosForSameUser() {
        User user = User.findById(testUserId);
        assertNotNull(user);

        // Create multiple todos
        Todo todo1 = new Todo();
        todo1.title = "Multiple Todos Test 1";
        todo1.completed = false;
        todo1.user = user;
        todo1.persist();

        Todo todo2 = new Todo();
        todo2.title = "Multiple Todos Test 2";
        todo2.completed = true;
        todo2.user = user;
        todo2.persist();

        Todo todo3 = new Todo();
        todo3.title = "Multiple Todos Test 3";
        todo3.completed = false;
        todo3.user = user;
        todo3.persist();

        entityManager.flush();

        // Verify todos exist
        List<Todo> userTodos = Todo.findByUserId(testUserId);
        assertTrue(userTodos.size() >= 3);

        long completedCount = Todo.countByUserIdAndCompleted(testUserId, true);
        long pendingCount = Todo.countByUserIdAndCompleted(testUserId, false);

        assertTrue(completedCount >= 1);
        assertTrue(pendingCount >= 2);
    }
}
