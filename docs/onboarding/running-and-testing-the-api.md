# Running and Testing the CIS User Management API

This document provides instructions on how to run and test the CIS User Management API locally, including interaction with the legacy CLI system.

## Dual Persistence & Versioning
This API supports dual persistence:
- **v1 (/api/v1):** Uses MySQL as the primary data store (default).
- **v2 (/api/v2):** Uses MongoDB as the primary data store.

### Switching Persistence (v1 default)
The application uses the `db.type` property to choose which repository implementation to use for the default `UserPersistencePort`.

In `application.yml`:
```yaml
db:
  type: mysql   # options: [mysql, mongo]
```

## Prerequisites

Before you begin, ensure you have the following installed:

*   **Java Development Kit (JDK) 21**: [Download and Install JDK](https://www.oracle.com/java/technologies/downloads/)
*   **Maven 3.9 or higher**: [Download and Install Maven](https://maven.apache.org/download.cgi)
*   **Docker Desktop**: [Download and Install Docker Desktop](https://www.docker.com/products/docker-desktop)

## 1. Set up the Database (MySQL & MongoDB with Docker)

The API coexists with a legacy system and uses a shared MySQL 8 database. It also supports MongoDB for v2 endpoints.

1.  **Start containers**:
    From the project root, run:
    ```bash
    docker compose up -d
    ```
    This will start:
    *   **MySQL**: `localhost:3307`, Database: `sd3`, User: `sd3user`, Password: `sd3pass`
    *   **MongoDB**: `mongodb://localhost:27017/user_management`

2.  **Verify it's running**:
    ```bash
    docker ps
    # Look for containers 'cis-mysql-phase1' and 'cis-mongo-phase1'
    ```

## 2. Create a User via Legacy CLI

Since the User Management API Phase 1 is designed to work with existing users, you should first create a user using the legacy CLI tool.

1.  **Clone the legacy project**:
    ```bash
    git clone https://github.com/JalaU-Capstones/userscli.git
    cd userscli
    ```

2.  **Build the CLI**:
    ```bash
    mvn clean install
    ```

3.  **Create a user**:
    Ensure the `sd3.xml` config file in the CLI project points to `localhost:3307`. Then run:
    ```bash
    java -jar target/UsersCLI-1.0-SNAPSHOT.jar -config=sd3.xml -create -n javier -l jroca -p pass123
    ```

## 3. Run the User Management API

1.  Navigate back to the `CIS-Fase1-User-Management-API` project root.
2.  Build and run:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    The API will start on `http://localhost:8080`.

## 4. Testing the API

### Access API Documentation (Swagger UI)
Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to explore endpoints.

### Example Flow: Login and Delete (v1 - MySQL)

1.  **Log in**:
    Use the credentials created via the CLI:
    *   **Endpoint**: `POST /api/v1/auth/login`
    *   **Body**: `{"login": "jroca", "password": "pass123"}`
    *   **Action**: Execute and copy the returned `token`.

2.  **Authorize**:
    *   Click the "Authorize" button in Swagger UI.
    *   Enter `Bearer <your_token>` and click Authorize.

3.  **Get User ID**:
    *   **Endpoint**: `GET /api/v1/users`
    *   **Action**: Execute and find the `id` (UUID) for login `jroca`.

4.  **Delete Account (Cascade)**:
    *   **Endpoint**: `DELETE /api/v1/users/{id}`
    *   **Action**: Provide the UUID and execute.
    *   **Warning**: This will also delete all associated topics, ideas, and votes created by this user in the shared database.
    *   **Success Message**: "User and all related topics, ideas, and votes have been successfully deleted."

### Example Flow (v2 - MongoDB)
Endpoints under `/api/v2/users` will use MongoDB regardless of the `db.type` configuration.

## 5. Stopping the Environment

```bash
# Stop API
Ctrl+C

# Stop Database
docker compose down
```
