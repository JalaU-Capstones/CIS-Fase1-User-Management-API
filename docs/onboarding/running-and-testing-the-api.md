# Running and Testing the CIS User Management API

This document provides instructions on how to run and test the CIS User Management API locally.

## Prerequisites

Before you begin, ensure you have the following installed:

*   **Java Development Kit (JDK) 17 or higher**: [Download and Install JDK](https://www.oracle.com/java/technologies/downloads/)
*   **Maven 3.6.3 or higher**: [Download and Install Maven](https://maven.apache.org/download.cgi)
*   **Docker Desktop**: [Download and Install Docker Desktop](https://www.docker.com/products/docker-desktop) (for running the MySQL database)

## 1. Clone the Repository

First, clone the project repository to your local machine:

```bash
git clone <repository-url>
cd CIS-Fase1-User-Management-API
```

## 2. Set up the Database (MySQL with Docker)

The API uses a MySQL database. You can easily run a MySQL instance using Docker.

1.  **Start MySQL container**:
    Open your terminal in the project root directory and run the following command:

    ```bash
    docker run --name cis-mysql -e MYSQL_DATABASE=sd3 -e MYSQL_USER=cis_user -e MYSQL_PASSWORD=cis_password -e MYSQL_ROOT_PASSWORD=root_password -p 3306:3306 -d mysql:8.0
    ```

    This command will:
    *   Create a Docker container named `cis-mysql`.
    *   Set the database name to `sd3`.
    *   Set the username to `cis_user`.
    *   Set the password to `cis_password`.
    *   Map the container's port 3306 to your host's port 3306.
    *   Run the container in detached mode (`-d`).

2.  **Verify database connection (Optional)**:
    You can use a tool like `mysql` CLI or DBeaver to connect to `localhost:3306` with the credentials `cis_user`/`cis_password` and database `sd3`.

## 3. Configure the Application

The application configuration is located in `src/main/resources/application.yml`. For local development, the default settings should work with the Dockerized MySQL.

If you need to change database connection details or other properties, modify `application.yml` or create an `application-dev.yml` for development-specific overrides.

## 4. Build the Application

Navigate to the project root directory in your terminal and build the application using Maven:

```bash
mvn clean install
```

This command compiles the code, runs tests, and packages the application into a JAR file.

## 5. Run the Application

After a successful build, you can run the Spring Boot application:

```bash
java -jar target/CIS-Fase1-User-Management-API-0.0.1-SNAPSHOT.jar
```

The API will start on `http://localhost:8080` by default.

## 6. Access API Documentation (Swagger UI)

Once the application is running, you can access the interactive API documentation (Swagger UI) at:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

This interface allows you to explore the available endpoints, send requests, and view responses.

## 7. Testing the API

You can test the API using Swagger UI, Postman, or `curl`.

### Example: Logging In

1.  Open Swagger UI.
2.  Expand the `Authentication` section.
3.  Try the `POST /api/v1/auth/login` endpoint.
4.  Click "Try it out".
5.  In the Request body, provide a user's credentials (e.g., `{"login": "john.doe", "password": "password123"}`).
6.  Click "Execute".
7.  You should receive a `200 OK` response with a JWT token. Copy this token.

### Example: Accessing Protected Endpoint (Get All Users)

1.  In Swagger UI, click the "Authorize" button (usually at the top right).
2.  Paste the JWT token obtained from registration into the `Value` field (e.g., `Bearer <your_jwt_token>`).
3.  Click "Authorize" and then "Close".
4.  Expand the `Users` section.
5.  Try the `GET /api/v1/users` endpoint.
6.  Click "Try it out" and then "Execute".
7.  You should receive a `200 OK` response with a list of users.

### Example: Deleting a User

**WARNING**: Deleting a user will also delete all their associated topics, ideas, and votes. This action is irreversible.

1.  Obtain the `id` (UUID) of the user you wish to delete from the `GET /api/v1/users` endpoint or from the login response if applicable.
2.  Ensure you are authorized with the JWT token of the user you intend to delete.
3.  Expand the `Users` section.
4.  Try the `DELETE /api/v1/users/{id}` endpoint.
5.  Click "Try it out".
6.  Enter the user's `id` in the `id` path parameter field.
7.  Click "Execute".
8.  You should receive a `200 OK` response with the message "User and all related topics, ideas, and votes have been successfully deleted."

## 8. Stopping the Application and Database

To stop the Spring Boot application, press `Ctrl+C` in the terminal where it's running.

To stop and remove the Dockerized MySQL database:

```bash
docker stop cis-mysql
docker rm cis-mysql
```

This will stop the container and remove it, but the data volume will persist unless explicitly removed. If you want to remove the data volume as well (for a clean start), you might need to use `docker volume ls` and `docker volume rm` commands, but be careful not to delete important data.
