# Users with Todos CRUD API Documentation

A comprehensive, low-latency REST API for managing users and their todos, built with Quarkus, Hibernate Panache, and PostgreSQL.

## 🚀 Features

- **Full CRUD operations** for Users and Todos
- **RESTful API design** following best practices
- **Bean Validation** with comprehensive error handling
- **Pagination and filtering** support
- **Optimized database queries** with indexes for low latency
- **Connection pooling** configured for high performance
- **Comprehensive unit tests** with REST Assured
- **Global exception handling** with consistent error responses

## 🏗️ Architecture

### Tech Stack
- **Framework**: Quarkus 3.29.0 (supersonic, subatomic Java)
- **ORM**: Hibernate ORM with Panache (Active Record pattern)
- **Database**: PostgreSQL
- **Testing**: JUnit 5 + REST Assured
- **Java Version**: 21

### Low Latency Optimizations
1. **Database Indexes**: Strategically placed on foreign keys and frequently queried columns
2. **Connection Pooling**: Min 5, Max 20 connections
3. **Query Optimization**: Using Panache for efficient queries
4. **Lazy Loading**: Relationships loaded only when needed
5. **Pagination**: Limits response sizes
6. **Batch Operations**: Statement batch size of 20

## 📋 API Endpoints

### Users API (`/api/v1/users`)

#### Get All Users
```http
GET /api/v1/users?page=0&size=20&search=john
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size
- `search` (optional) - Search by name

**Response:**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "name": "John Doe",
    "createdAt": "2025-11-05T10:00:00",
    "updatedAt": "2025-11-05T10:00:00"
  }
]
```

**Headers:**
- `X-Total-Count`: Total number of users
- `X-Page`: Current page
- `X-Page-Size`: Page size

#### Get User by ID
```http
GET /api/v1/users/{id}
```

**Response:** 200 OK or 404 Not Found

#### Get User by Username
```http
GET /api/v1/users/username/{username}
```

**Response:** 200 OK or 404 Not Found

#### Create User
```http
POST /api/v1/users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "name": "John Doe"
}
```

**Validation:**
- `username`: Required, 3-50 characters, unique
- `email`: Required, valid email format, unique
- `name`: Required, max 100 characters

**Response:** 201 Created or 409 Conflict (duplicate username/email) or 400 Bad Request (validation errors)

#### Update User
```http
PUT /api/v1/users/{id}
Content-Type: application/json

{
  "username": "john_updated",
  "email": "john.new@example.com",
  "name": "John Updated"
}
```

**Note:** All fields are optional for partial updates

**Response:** 200 OK or 404 Not Found or 409 Conflict

#### Delete User
```http
DELETE /api/v1/users/{id}
```

**Response:** 204 No Content or 404 Not Found

#### Get User Statistics
```http
GET /api/v1/users/{id}/stats
```

**Response:**
```json
{
  "userId": 1,
  "username": "john_doe",
  "totalTodos": 10,
  "completedTodos": 4,
  "pendingTodos": 6
}
```

---

### Todos API (`/api/v1/todos`)

#### Get All Todos
```http
GET /api/v1/todos?page=0&size=20&userId=1&completed=false
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size
- `userId` (optional) - Filter by user ID
- `completed` (optional) - Filter by completion status

**Response:**
```json
[
  {
    "id": 1,
    "title": "Complete documentation",
    "description": "Write API documentation",
    "completed": false,
    "userId": 1,
    "userName": "John Doe",
    "dueDate": "2025-11-12T10:00:00",
    "createdAt": "2025-11-05T10:00:00",
    "updatedAt": "2025-11-05T10:00:00"
  }
]
```

#### Get Todos by User ID
```http
GET /api/v1/todos/user/{userId}?completed=false
```

**Query Parameters:**
- `completed` (optional) - Filter by completion status

**Response:** 200 OK or 404 Not Found (user not found)

#### Get Todo by ID
```http
GET /api/v1/todos/{id}
```

**Response:** 200 OK or 404 Not Found

#### Create Todo
```http
POST /api/v1/todos
Content-Type: application/json

{
  "title": "Complete documentation",
  "description": "Write comprehensive API docs",
  "completed": false,
  "userId": 1,
  "dueDate": "2025-11-12T10:00:00"
}
```

**Validation:**
- `title`: Required, max 200 characters
- `description`: Optional, max 1000 characters
- `completed`: Optional, default: false
- `userId`: Required, must reference existing user
- `dueDate`: Optional

**Response:** 201 Created or 400 Bad Request

#### Update Todo
```http
PUT /api/v1/todos/{id}
Content-Type: application/json

{
  "title": "Updated title",
  "description": "Updated description",
  "completed": true,
  "dueDate": "2025-11-15T10:00:00"
}
```

**Note:** All fields are optional for partial updates

**Response:** 200 OK or 404 Not Found

#### Toggle Todo Completion
```http
PATCH /api/v1/todos/{id}/toggle
```

**Response:** 200 OK (returns updated todo) or 404 Not Found

#### Delete Todo
```http
DELETE /api/v1/todos/{id}
```

**Response:** 204 No Content or 404 Not Found

#### Delete Completed Todos for User
```http
DELETE /api/v1/todos/user/{userId}/completed
```

**Response:**
```json
{
  "message": "Deleted 5 completed todos",
  "deletedCount": 5
}
```

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);
```

### Todos Table
```sql
CREATE TABLE todos (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  description VARCHAR(1000),
  completed BOOLEAN NOT NULL DEFAULT false,
  user_id BIGINT NOT NULL REFERENCES users(id),
  due_date TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE INDEX idx_todo_user_id ON todos(user_id);
CREATE INDEX idx_todo_status ON todos(completed);
CREATE INDEX idx_todo_created_at ON todos(created_at);
```

---

## 🚦 Error Handling

### Validation Errors (400 Bad Request)
```json
{
  "error": "Validation failed",
  "violations": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Email must be valid"
  }
}
```

### Not Found (404)
```json
{
  "error": "User not found with id: 123"
}
```

### Conflict (409)
```json
{
  "error": "Username already exists: john_doe"
}
```

### Internal Server Error (500)
```json
{
  "error": "Internal server error",
  "message": "Error details..."
}
```

---

## 🧪 Testing

The project includes comprehensive unit tests for all endpoints:

- **UserResourceTest**: 20 test cases covering all user operations
- **TodoResourceTest**: 24 test cases covering all todo operations

### Run Tests
```bash
./mvnw test
```

### Run with Coverage
```bash
./mvnw verify
```

---

## 🚀 Running the Application

### Prerequisites
- Java 21+
- PostgreSQL 12+
- Maven 3.8+

### Database Setup
```sql
CREATE DATABASE todos_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE todos_db TO postgres;
```

### Configuration
Update `src/main/resources/application.properties`:
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/todos_db
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
```

### Start Application
```bash
# Development mode with live reload
./mvnw quarkus:dev

# Production build
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

The API will be available at `http://localhost:8080`

### Dev UI
Quarkus provides a dev UI at `http://localhost:8080/q/dev/` when running in dev mode.

---

## 📊 Performance Characteristics

### Low Latency Features:
- **Indexed Queries**: All foreign keys and frequently queried columns are indexed
- **Connection Pooling**: Pre-warmed connections (5-20 pool size)
- **Lazy Loading**: Relationships loaded on-demand
- **Optimized Queries**: Using Panache for efficient JPA queries
- **Batch Operations**: Configured for batch size of 20
- **No N+1 Problems**: Proper JOIN FETCH where needed

### Expected Response Times:
- Simple CRUD operations: < 10ms
- Filtered queries: < 20ms
- Paginated queries: < 30ms
- Complex aggregations: < 50ms

*(Times may vary based on hardware and database load)*

---

## 🔒 Best Practices Implemented

1. **API Versioning**: `/api/v1/` prefix for future compatibility
2. **DTO Pattern**: Separate request/response DTOs from entities
3. **Bean Validation**: Declarative validation with Jakarta Bean Validation
4. **Global Exception Handling**: Consistent error responses
5. **RESTful Design**: Proper HTTP methods and status codes
6. **Pagination Support**: Prevents large response payloads
7. **Soft Relationships**: Cascade operations properly configured
8. **Timestamps**: Automatic created_at/updated_at tracking
9. **Comprehensive Testing**: Full test coverage with REST Assured
10. **Documentation**: Well-documented code and API

---

## 📝 Sample Data

The application includes sample data in `import.sql`:
- 3 sample users
- 10 sample todos with various states

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure all tests pass
5. Submit a pull request

---

## 📄 License

This project is part of the Swiftbeard organization.

---

## 📞 Support

For issues or questions, please open an issue in the repository.
