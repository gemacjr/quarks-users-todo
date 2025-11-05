package org.swiftbeard.resource;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.swiftbeard.dto.UserCreateRequest;
import org.swiftbeard.dto.UserResponse;
import org.swiftbeard.dto.UserUpdateRequest;
import org.swiftbeard.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Resource for User CRUD operations.
 * Implements best practices for RESTful API design.
 */
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    /**
     * Get all users with optional pagination.
     * GET /api/v1/users?page=0&size=20
     */
    @GET
    public Response getAllUsers(
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("20") @Min(1) int size,
            @QueryParam("search") String search) {

        List<User> users;

        if (search != null && !search.isBlank()) {
            users = User.findByNameContaining(search);
        } else {
            users = User.findAll()
                    .page(page, size)
                    .list();
        }

        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        long totalCount = User.count();

        return Response.ok(response)
                .header("X-Total-Count", totalCount)
                .header("X-Page", page)
                .header("X-Page-Size", size)
                .build();
    }

    /**
     * Get a user by ID.
     * GET /api/v1/users/{id}
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        User user = User.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("User not found with id: " + id))
                    .build();
        }

        return Response.ok(UserResponse.from(user)).build();
    }

    /**
     * Get a user by username.
     * GET /api/v1/users/username/{username}
     */
    @GET
    @Path("/username/{username}")
    public Response getUserByUsername(@PathParam("username") String username) {
        User user = User.findByUsername(username);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("User not found with username: " + username))
                    .build();
        }

        return Response.ok(UserResponse.from(user)).build();
    }

    /**
     * Create a new user.
     * POST /api/v1/users
     */
    @POST
    @Transactional
    public Response createUser(@Valid UserCreateRequest request) {
        // Check if username already exists
        if (User.findByUsername(request.username) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Username already exists: " + request.username))
                    .build();
        }

        // Check if email already exists
        if (User.findByEmail(request.email) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Email already exists: " + request.email))
                    .build();
        }

        User user = new User();
        user.username = request.username;
        user.email = request.email;
        user.name = request.name;

        user.persist();

        return Response.status(Response.Status.CREATED)
                .entity(UserResponse.from(user))
                .build();
    }

    /**
     * Update an existing user.
     * PUT /api/v1/users/{id}
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, @Valid UserUpdateRequest request) {
        User user = User.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("User not found with id: " + id))
                    .build();
        }

        // Check if username is being changed and if it already exists
        if (request.username != null && !request.username.equals(user.username)) {
            User existingUser = User.findByUsername(request.username);
            if (existingUser != null && !existingUser.id.equals(id)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("Username already exists: " + request.username))
                        .build();
            }
            user.username = request.username;
        }

        // Check if email is being changed and if it already exists
        if (request.email != null && !request.email.equals(user.email)) {
            User existingUser = User.findByEmail(request.email);
            if (existingUser != null && !existingUser.id.equals(id)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("Email already exists: " + request.email))
                        .build();
            }
            user.email = request.email;
        }

        // Update name if provided
        if (request.name != null) {
            user.name = request.name;
        }

        user.persist();

        return Response.ok(UserResponse.from(user)).build();
    }

    /**
     * Delete a user by ID.
     * DELETE /api/v1/users/{id}
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User user = User.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("User not found with id: " + id))
                    .build();
        }

        user.delete();

        return Response.noContent().build();
    }

    /**
     * Get user statistics.
     * GET /api/v1/users/{id}/stats
     */
    @GET
    @Path("/{id}/stats")
    public Response getUserStats(@PathParam("id") Long id) {
        User user = User.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("User not found with id: " + id))
                    .build();
        }

        long totalTodos = org.swiftbeard.entity.Todo.countByUserId(id);
        long completedTodos = org.swiftbeard.entity.Todo.countByUserIdAndCompleted(id, true);
        long pendingTodos = org.swiftbeard.entity.Todo.countByUserIdAndCompleted(id, false);

        UserStats stats = new UserStats();
        stats.userId = id;
        stats.username = user.username;
        stats.totalTodos = totalTodos;
        stats.completedTodos = completedTodos;
        stats.pendingTodos = pendingTodos;

        return Response.ok(stats).build();
    }

    // Inner classes for responses
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    public static class UserStats {
        public Long userId;
        public String username;
        public long totalTodos;
        public long completedTodos;
        public long pendingTodos;
    }
}
