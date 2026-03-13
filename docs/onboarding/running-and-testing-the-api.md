# Running and Testing the CIS User Management API

This guide provides step-by-step instructions for new developers to set up, run, and test the User Management API (Phase 1).

## 1. Prerequisites

Ensure you have the following installed:
- **Java 21** (JDK 21)
- **Maven** (Apache Maven 3.9+)
- **Docker** & **Docker Compose**
- **Git**

## 2. Cloning the Repository

Clone the project repository to your local machine:

```bash
git clone https://gitlab.com/your-team/cis-user-management-api.git
cd cis-user-management-api
```

## 3. Setting Up the Database

The application requires a MySQL database. Use the provided Docker Compose file to start a local instance.

```bash
docker-compose up -d
```

This will start a MySQL container with:
- **Database**: `sd3`
- **User**: `user`
- **Password**: `password`
- **Port**: `3306`

To verify the container is running:
```bash
docker ps
```

## 4. Running the Application

You can run the application using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

Alternatively, build the JAR and run it:

```bash
./mvnw clean package -DskipTests
java -jar target/cis-user-management-api-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`.

## 5. Testing the API

The current implementation supports retrieving a list of users.

### GET /api/v1/users

Retrieve all users (password field is excluded).

**Request:**
```bash
curl -v http://localhost:8080/api/v1/users
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Test User",
    "login": "testuser"
  }
]
```

## 6. Profiles

- **default**: Uses local MySQL (localhost:3306). Ideal for local development with Docker.
- **test**: Uses H2 in-memory database. Used automatically during `mvn test`.

## 7. Common Issues & Warnings

- **Port Conflict**: If port 8080 is in use, modify `server.port` in `application.properties` or stop the conflicting service.
- **Database Connection**: Ensure the Docker container is running before starting the app.
- **Deprecation Warnings**: We have replaced `@MockBean` with `@MockitoBean` (Spring Boot 3.4+) in tests. Ensure you use the updated annotations when writing new tests.

## 8. Running Tests

To execute all unit and integration tests:

```bash
./mvnw test
```
