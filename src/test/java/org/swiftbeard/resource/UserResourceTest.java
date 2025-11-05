package org.swiftbeard.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.swiftbeard.dto.UserCreateRequest;
import org.swiftbeard.dto.UserUpdateRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * Comprehensive unit tests for UserResource endpoints.
 * Tests all CRUD operations, validation, and error handling.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    private static Long createdUserId;

    @Test
    @Order(1)
    void testGetAllUsers() {
        given()
            .when()
            .get("/api/v1/users")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThan(0)))
            .header("X-Total-Count", notNullValue());
    }

    @Test
    @Order(2)
    void testGetAllUsersWithPagination() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 2)
            .when()
            .get("/api/v1/users")
            .then()
            .statusCode(200)
            .header("X-Page", "0")
            .header("X-Page-Size", "2");
    }

    @Test
    @Order(3)
    void testGetAllUsersWithSearch() {
        given()
            .queryParam("search", "john")
            .when()
            .get("/api/v1/users")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)));
    }

    @Test
    @Order(4)
    void testGetUserById() {
        given()
            .when()
            .get("/api/v1/users/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("username", notNullValue())
            .body("email", notNullValue())
            .body("name", notNullValue());
    }

    @Test
    @Order(5)
    void testGetUserByIdNotFound() {
        given()
            .when()
            .get("/api/v1/users/999999")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(6)
    void testGetUserByUsername() {
        given()
            .when()
            .get("/api/v1/users/username/john_doe")
            .then()
            .statusCode(200)
            .body("username", equalTo("john_doe"));
    }

    @Test
    @Order(7)
    void testGetUserByUsernameNotFound() {
        given()
            .when()
            .get("/api/v1/users/username/nonexistent_user")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(8)
    void testCreateUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "testuser";
        request.email = "testuser@example.com";
        request.name = "Test User";

        createdUserId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/users")
            .then()
            .statusCode(201)
            .body("username", equalTo("testuser"))
            .body("email", equalTo("testuser@example.com"))
            .body("name", equalTo("Test User"))
            .body("id", notNullValue())
            .body("createdAt", notNullValue())
            .extract()
            .path("id");
    }

    @Test
    @Order(9)
    void testCreateUserWithDuplicateUsername() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "john_doe"; // Already exists
        request.email = "newemail@example.com";
        request.name = "New User";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/users")
            .then()
            .statusCode(409)
            .body("error", containsString("already exists"));
    }

    @Test
    @Order(10)
    void testCreateUserWithDuplicateEmail() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "newusername";
        request.email = "john.doe@example.com"; // Already exists
        request.name = "New User";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/users")
            .then()
            .statusCode(409)
            .body("error", containsString("already exists"));
    }

    @Test
    @Order(11)
    void testCreateUserWithInvalidData() {
        UserCreateRequest request = new UserCreateRequest();
        request.username = "ab"; // Too short
        request.email = "invalid-email"; // Invalid format
        request.name = ""; // Empty

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/users")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(12)
    void testCreateUserWithMissingFields() {
        UserCreateRequest request = new UserCreateRequest();
        // All fields null

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/users")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(13)
    void testUpdateUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.name = "Updated Test User";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/v1/users/" + createdUserId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Test User"))
            .body("updatedAt", notNullValue());
    }

    @Test
    @Order(14)
    void testUpdateUserWithNewUsername() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.username = "updated_testuser";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/v1/users/" + createdUserId)
            .then()
            .statusCode(200)
            .body("username", equalTo("updated_testuser"));
    }

    @Test
    @Order(15)
    void testUpdateUserWithDuplicateUsername() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.username = "john_doe"; // Already exists

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/v1/users/" + createdUserId)
            .then()
            .statusCode(409)
            .body("error", containsString("already exists"));
    }

    @Test
    @Order(16)
    void testUpdateUserNotFound() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.name = "Updated Name";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/v1/users/999999")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(17)
    void testGetUserStats() {
        given()
            .when()
            .get("/api/v1/users/1/stats")
            .then()
            .statusCode(200)
            .body("userId", equalTo(1))
            .body("username", notNullValue())
            .body("totalTodos", notNullValue())
            .body("completedTodos", notNullValue())
            .body("pendingTodos", notNullValue());
    }

    @Test
    @Order(18)
    void testGetUserStatsNotFound() {
        given()
            .when()
            .get("/api/v1/users/999999/stats")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    @Order(19)
    void testDeleteUser() {
        given()
            .when()
            .delete("/api/v1/users/" + createdUserId)
            .then()
            .statusCode(204);

        // Verify user is deleted
        given()
            .when()
            .get("/api/v1/users/" + createdUserId)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(20)
    void testDeleteUserNotFound() {
        given()
            .when()
            .delete("/api/v1/users/999999")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }
}
