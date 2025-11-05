package org.swiftbeard.resource;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.swiftbeard.dto.TodoCreateRequest;
import org.swiftbeard.dto.TodoResponse;
import org.swiftbeard.dto.TodoUpdateRequest;
import org.swiftbeard.entity.Todo;
import org.swiftbeard.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Resource for Todo CRUD operations.
 * Implements best practices for RESTful API design.
 */
@Path("/api/v1/todos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {

    /**
     * Get all todos with optional pagination and filtering.
     * GET /api/v1/todos?page=0&size=20&userId=1&completed=true
     */
    @GET
    public Response getAllTodos(
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("20") @Min(1) int size,
            @QueryParam("userId") Long userId,
            @QueryParam("completed") Boolean completed) {

        List<Todo> todos;

        if (userId != null && completed != null) {
            todos = Todo.findByUserIdAndCompleted(userId, completed);
        } else if (userId != null) {
            todos = Todo.findByUserId(userId);
        } else if (completed != null) {
            todos = Todo.findCompletedTodos(completed);
        } else {
            todos = Todo.findAll()
                    .page(page, size)
                    .list();
        }

        List<TodoResponse> response = todos.stream()
                .map(TodoResponse::from)
                .collect(Collectors.toList());

        long totalCount = Todo.count();

        return Response.ok(response)
                .header("X-Total-Count", totalCount)
                .header("X-Page", page)
                .header("X-Page-Size", size)
                .build();
    }

    /**
     * Get all todos for a specific user.
     * GET /api/v1/todos/user/{userId}
     */
    @GET
    @Path("/user/{userId}")
    public Response getTodosByUserId(
            @PathParam("userId") Long userId,
            @QueryParam("completed") Boolean completed) {

        User user = User.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("User not found with id: " + userId))
                    .build();
        }

        List<Todo> todos;
        if (completed != null) {
            todos = Todo.findByUserIdAndCompleted(userId, completed);
        } else {
            todos = Todo.findByUserId(userId);
        }

        List<TodoResponse> response = todos.stream()
                .map(TodoResponse::from)
                .collect(Collectors.toList());

        return Response.ok(response).build();
    }

    /**
     * Get a todo by ID.
     * GET /api/v1/todos/{id}
     */
    @GET
    @Path("/{id}")
    public Response getTodoById(@PathParam("id") Long id) {
        Todo todo = Todo.findById(id);

        if (todo == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Todo not found with id: " + id))
                    .build();
        }

        return Response.ok(TodoResponse.from(todo)).build();
    }

    /**
     * Create a new todo.
     * POST /api/v1/todos
     */
    @POST
    @Transactional
    public Response createTodo(@Valid TodoCreateRequest request) {
        // Verify user exists
        User user = User.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("User not found with id: " + request.userId))
                    .build();
        }

        Todo todo = new Todo();
        todo.title = request.title;
        todo.description = request.description;
        todo.completed = request.completed != null ? request.completed : false;
        todo.user = user;
        todo.dueDate = request.dueDate;

        todo.persist();

        return Response.status(Response.Status.CREATED)
                .entity(TodoResponse.from(todo))
                .build();
    }

    /**
     * Update an existing todo.
     * PUT /api/v1/todos/{id}
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateTodo(@PathParam("id") Long id, @Valid TodoUpdateRequest request) {
        Todo todo = Todo.findById(id);

        if (todo == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Todo not found with id: " + id))
                    .build();
        }

        // Update fields if provided
        if (request.title != null) {
            todo.title = request.title;
        }
        if (request.description != null) {
            todo.description = request.description;
        }
        if (request.completed != null) {
            todo.completed = request.completed;
        }
        if (request.dueDate != null) {
            todo.dueDate = request.dueDate;
        }

        todo.persist();

        return Response.ok(TodoResponse.from(todo)).build();
    }

    /**
     * Toggle todo completion status.
     * PATCH /api/v1/todos/{id}/toggle
     */
    @PATCH
    @Path("/{id}/toggle")
    @Transactional
    public Response toggleTodoCompletion(@PathParam("id") Long id) {
        Todo todo = Todo.findById(id);

        if (todo == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Todo not found with id: " + id))
                    .build();
        }

        todo.completed = !todo.completed;
        todo.persist();

        return Response.ok(TodoResponse.from(todo)).build();
    }

    /**
     * Delete a todo by ID.
     * DELETE /api/v1/todos/{id}
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteTodo(@PathParam("id") Long id) {
        Todo todo = Todo.findById(id);

        if (todo == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Todo not found with id: " + id))
                    .build();
        }

        todo.delete();

        return Response.noContent().build();
    }

    /**
     * Delete all completed todos for a user.
     * DELETE /api/v1/todos/user/{userId}/completed
     */
    @DELETE
    @Path("/user/{userId}/completed")
    @Transactional
    public Response deleteCompletedTodos(@PathParam("userId") Long userId) {
        User user = User.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("User not found with id: " + userId))
                    .build();
        }

        List<Todo> completedTodos = Todo.findByUserIdAndCompleted(userId, true);
        long deletedCount = completedTodos.size();

        completedTodos.forEach(Todo::delete);

        DeleteResponse response = new DeleteResponse();
        response.message = "Deleted " + deletedCount + " completed todos";
        response.deletedCount = deletedCount;

        return Response.ok(response).build();
    }

    // Inner classes for responses
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    public static class DeleteResponse {
        public String message;
        public long deletedCount;
    }
}
