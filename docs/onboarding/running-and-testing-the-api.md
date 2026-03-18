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
java -jar target/user-management-api-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`.

## 5. Testing the API

The API uses JWT Bearer Token Authentication. Read operations (GET) and creating the first user are public, but other write operations (PUT, DELETE) require a valid token.

### 5.1. POST /api/v1/users (Public)

Create the first user. This endpoint is public.

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/users \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Test User",
           "login": "testuser",
           "password": "password123"
         }'
```

**Expected Response (201 Created):**
```json
{
  "id": "generated-uuid",
  "name": "Test User",
  "login": "testuser"
}
```

### 5.2. POST /api/v1/auth/login (Public)

Authenticate to receive a JWT token.

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{
           "login": "testuser",
           "password": "password123"
         }'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTcx..."
}
```

**Note:** Copy this token for subsequent requests.

### 5.3. GET /api/v1/users (Public)

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

### 5.4. GET /api/v1/users/{id} (Public)
Retrieve a specific user by ID.

**Request:**
```bash
curl http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000
```

### 5.5. PUT /api/v1/users/{id} (Protected)

Update an existing user. Requires Bearer Token.

**Request:**
```bash
TOKEN="your_jwt_token_here"
USER_ID="550e8400-e29b-41d4-a716-446655440000"

curl -X PUT http://localhost:8080/api/v1/users/$USER_ID \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Updated Name",
           "login": "updatedlogin",
           "password": "newpassword"
         }'
```

**Expected Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Updated Name",
  "login": "updatedlogin"
}
```

### 5.6. DELETE /api/v1/users/{id} (Protected)

Delete a user. Requires Bearer Token.

**Request:**
```bash
TOKEN="your_jwt_token_here"
USER_ID="550e8400-e29b-41d4-a716-446655440000"

curl -X DELETE http://localhost:8080/api/v1/users/$USER_ID \
     -H "Authorization: Bearer $TOKEN"
```

**Expected Response (204 No Content)**

## 6. Profiles

- **default**: Uses local MySQL (localhost:3306). Ideal for local development with Docker.
- **test**: Uses H2 in-memory database. Used automatically during `mvn test`.

## 7. Common Issues & Warnings

- **403 Forbidden**: If you get this on PUT/DELETE, check your Authorization header format (`Bearer <token>`) and ensure the token is not expired.
- **Port Conflict**: If port 8080 is in use, modify `server.port` in `application.properties`.
- **Database Connection**: Ensure the Docker container is running before starting the app.
- **Deprecation Warnings**: We use `@MockitoBean` in tests to align with Spring Boot 3.4+.

## 8. Running Tests

To execute all unit and integration tests:

```bash
./mvnw test
```
