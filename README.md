# Users with Todos - CRUD API

A comprehensive, low-latency REST API for managing users and their todos, built with Quarkus, Hibernate Panache, and PostgreSQL.

## 🚀 Features

- Full CRUD operations for Users and Todos
- RESTful API design with best practices
- Bean Validation with comprehensive error handling
- Pagination and filtering support
- Optimized database queries with indexes for low latency
- Connection pooling configured for high performance
- Comprehensive unit tests with REST Assured
- Global exception handling

## 📚 Documentation

For complete API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## 🏗️ Tech Stack

This project uses Quarkus, the Supersonic Subatomic Java Framework.

- **Framework**: Quarkus 3.29.0
- **ORM**: Hibernate ORM with Panache
- **Database**: PostgreSQL
- **Testing**: JUnit 5 + REST Assured
- **Java**: 21

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## 📋 Quick Start API Endpoints

### Users
- `GET /api/v1/users` - Get all users (with pagination)
- `GET /api/v1/users/{id}` - Get user by ID
- `POST /api/v1/users` - Create new user
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user
- `GET /api/v1/users/{id}/stats` - Get user statistics

### Todos
- `GET /api/v1/todos` - Get all todos (with filtering)
- `GET /api/v1/todos/{id}` - Get todo by ID
- `GET /api/v1/todos/user/{userId}` - Get user's todos
- `POST /api/v1/todos` - Create new todo
- `PUT /api/v1/todos/{id}` - Update todo
- `PATCH /api/v1/todos/{id}/toggle` - Toggle completion
- `DELETE /api/v1/todos/{id}` - Delete todo

## 🗄️ Database Setup

Before running the application, set up PostgreSQL:

```sql
CREATE DATABASE todos_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE todos_db TO postgres;
```

Update `src/main/resources/application.properties` with your database credentials if needed.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarks-users-todo-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
