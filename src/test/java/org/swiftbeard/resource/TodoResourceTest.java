package org.swiftbeard.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.swiftbeard.dto.TodoCreateRequest;
import org.swiftbeard.dto.TodoUpdateRequest;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * Comprehensive unit tests for TodoResource endpoints.
 * Tests all CRUD operations, validation, and error handling.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoResourceTest {

    private static Long createdTodoId;

    @Test
    @Order(1)
    void testGetAllTodos() {
        given()
            .when()
            .get("/api/v1/todos")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThan(0)))
            .header("X-Total-Count", notNullValue());
    }

    @Test
    @Order(2)
    void testGetAllTodosWithPagination() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 5)
            .when()
            .get("/api/v1/todos")
            .then()
            .statusCode(200)
            .header("X-Page", "0")
            .header("X-Page-Size", "5");
    }

    @Test
    @Order(3)
    void testGetAllTodosFilteredByUserId() {
        given()
            .queryParam("userId", 1)
            .when()
            .get("/api/v1/todos")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].userId", equalTo(1));
    }

    @Test
    @Order(4)
    void testGetAllTodosFilteredByCompleted() {
        given()
            .queryParam("completed", true)
            .when()
            .get("/api/v1/todos")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].completed", equalTo(true));
    }

    @Test
    @Order(5)
    void testGetAllTodosFilteredByUserIdAndCompleted() {
        given()
            .queryParam("userId", 1)
            .queryParam("completed", false)
            .when()
            .get("/api/v1/todos")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].userId", equalTo(1))
            .body("[0].completed", equalTo(false));
    }

    @Test
    @Order(6)
    void testGetTodosByUserId() {
        given()
            .when()
            .get("/api/v1/todos/user/1")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].userId", equalTo(1));
    }

    @Test
    @Order(7)
    void testGetTodosByUserIdWithCompletedFilter() {
        given()
            .queryParam("completed", true)
            .when()
            .get("/api/v1/todos/user/1")
            .then()
            .statusCode(200)
            .body("[0].userId", equalTo(1))
            .body("[0].completed", equalTo(true));
    }

    @Test
    @Order(8)
    void testGetTodosByUserIdNotFound() {
        given()
            .when()
            .get("/api/v1/todos/user/999999")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(9)
    void testGetTodoById() {
        given()
            .when()
            .get("/api/v1/todos/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("title", notNullValue())
            .body("completed", notNullValue())
            .body("userId", notNullValue());
    }

    @Test
    @Order(10)
    void testGetTodoByIdNotFound() {
        given()
            .when()
            .get("/api/v1/todos/999999")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(11)
    void testCreateTodo() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Test Todo";
        request.description = "This is a test todo";
        request.completed = false;
        request.userId = 1L;
        request.dueDate = LocalDateTime.now().plusDays(7);

        createdTodoId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/todos")
            .then()
            .statusCode(201)
            .body("title", equalTo("Test Todo"))
            .body("description", equalTo("This is a test todo"))
            .body("completed", equalTo(false))
            .body("userId", equalTo(1))
            .body("id", notNullValue())
            .body("createdAt", notNullValue())
            .extract()
            .path("id");
    }

    @Test
    @Order(12)
    void testCreateTodoWithInvalidUserId() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "Test Todo";
        request.description = "This is a test todo";
        request.userId = 999999L; // Non-existent user

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/todos")
            .then()
            .statusCode(400)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(13)
    void testCreateTodoWithInvalidData() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = ""; // Empty title
        request.userId = 1L;

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/todos")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(14)
    void testCreateTodoWithMissingFields() {
        TodoCreateRequest request = new TodoCreateRequest();
        // Missing required fields

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/todos")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(15)
    void testCreateTodoWithTitleTooLong() {
        TodoCreateRequest request = new TodoCreateRequest();
        request.title = "a".repeat(201); // Exceeds max length
        request.userId = 1L;

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/todos")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(16)
    void testUpdateTodo() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.title = "Updated Test Todo";
        request.description = "Updated description";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/v1/todos/" + createdTodoId)
            .then()
            .statusCode(200)
            .body("title", equalTo("Updated Test Todo"))
            .body("description", equalTo("Updated description"))
            .body("updatedAt", notNullValue());
    }

    @Test
    @Order(17)
    void testUpdateTodoCompleted() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.completed = true;

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/v1/todos/" + createdTodoId)
            .then()
            .statusCode(200)
            .body("completed", equalTo(true));
    }

    @Test
    @Order(18)
    void testUpdateTodoNotFound() {
        TodoUpdateRequest request = new TodoUpdateRequest();
        request.title = "Updated Title";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/v1/todos/999999")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(19)
    void testToggleTodoCompletion() {
        // First, get current completion status
        Boolean currentStatus = given()
            .when()
            .get("/api/v1/todos/" + createdTodoId)
            .then()
            .extract()
            .path("completed");

        // Toggle it
        given()
            .when()
            .patch("/api/v1/todos/" + createdTodoId + "/toggle")
            .then()
            .statusCode(200)
            .body("completed", equalTo(!currentStatus));

        // Toggle it back
        given()
            .when()
            .patch("/api/v1/todos/" + createdTodoId + "/toggle")
            .then()
            .statusCode(200)
            .body("completed", equalTo(currentStatus));
    }

    @Test
    @Order(20)
    void testToggleTodoCompletionNotFound() {
        given()
            .when()
            .patch("/api/v1/todos/999999/toggle")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(21)
    void testDeleteCompletedTodosForUser() {
        // First, get count of completed todos for user 1
        int completedCount = given()
            .queryParam("userId", 1)
            .queryParam("completed", true)
            .when()
            .get("/api/v1/todos")
            .then()
            .extract()
            .path("size()");

        if (completedCount > 0) {
            given()
                .when()
                .delete("/api/v1/todos/user/1/completed")
                .then()
                .statusCode(200)
                .body("deletedCount", greaterThan(0L))
                .body("message", notNullValue());
        }
    }

    @Test
    @Order(22)
    void testDeleteCompletedTodosForUserNotFound() {
        given()
            .when()
            .delete("/api/v1/todos/user/999999/completed")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(23)
    void testDeleteTodo() {
        given()
            .when()
            .delete("/api/v1/todos/" + createdTodoId)
            .then()
            .statusCode(204);

        // Verify todo is deleted
        given()
            .when()
            .get("/api/v1/todos/" + createdTodoId)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(24)
    void testDeleteTodoNotFound() {
        given()
            .when()
            .delete("/api/v1/todos/999999")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }
}
