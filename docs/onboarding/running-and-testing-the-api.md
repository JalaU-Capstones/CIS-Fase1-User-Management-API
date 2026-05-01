# Running and Testing the CIS User Management API

This document provides instructions on how to run and test the CIS User Management API locally, including interaction with the legacy CLI system.

---

## Dual Persistence & Versioning

This API supports dual persistence:

- **v1 (`/api/v1`):** Uses MySQL as the primary data store (default).
- **v2 (`/api/v2`):** Uses MongoDB as the primary data store.

### Emergency Database Fallback (MySQL <-> MongoDB)

When enabled, the API continuously checks connectivity to both MySQL and MongoDB and automatically switches to the
healthy database if one goes down.

Behavior:

- If **both databases are healthy**:
  - `/api/v1/**` uses **MySQL**
  - `/api/v2/**` uses **MongoDB**
- If **MySQL is down** (MongoDB healthy): **both** `/api/v1/**` and `/api/v2/**` use **MongoDB**.
- If **MongoDB is down** (MySQL healthy): **both** `/api/v1/**` and `/api/v2/**` use **MySQL**.
- If **both databases are down**: all requests return HTTP `503` with:

```
Please try again later. Our maintenance team is working to resolve this issue.
```

#### Maintenance Mode for Writes

When the system is operating in fallback mode (only one database is healthy), all **write** operations
(`POST`, `PUT`, `DELETE`, or any non-`GET`) return HTTP `503` with:

```
Our system is currently undergoing planned maintenance. Please try again later.
Recommendation: Until further notice, avoid creating, updating, or deleting any resources. Your data is safe, but modifications may not be persisted. If you cannot find recently created items, please wait for the IT department to contact you.
```

#### Feature Toggle

In `application.yml`:

```yaml
app:
  fallback:
    enabled: true
```

In the `test` profile (unit tests), fallback is disabled by default to avoid requiring live MySQL/MongoDB instances.

### Switching Persistence (Legacy `db.type`)

Historically, the application used the `db.type` property to choose a single persistence implementation.
With emergency fallback enabled, routing is determined dynamically by database health and API version, so `db.type` is
no longer the primary mechanism for selecting persistence.

In `application.yml`:

```yaml
db:
  type: mysql   # options: [mysql, mongo]
```

---

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 21:** [Download and Install JDK](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9 or higher:** [Download and Install Maven](https://maven.apache.org/download.cgi)
- **Docker Desktop:** [Download and Install Docker Desktop](https://www.docker.com/products/docker-desktop/)

---

## 1. Set Up the Database (MySQL & MongoDB with Docker)

The API coexists with a legacy system and uses a shared MySQL 8 database. It also supports MongoDB for v2 endpoints.

**Start containers:**

From the project root, run:

```bash
docker compose up -d
```

This will start:

- **MySQL:** `localhost:3307` — Database: `sd3`, User: `sd3user`, Password: `sd3pass`
- **MongoDB:** `mongodb://localhost:27017/sd3`

**Verify it's running:**

```bash
docker ps
# Look for containers 'cis-mysql-phase1' and 'cis-mongo-phase1'
```

---

## 2. Create a User via Legacy CLI

Since the User Management API Phase 1 is designed to work with existing users, you should first create a user using the legacy CLI tool.

**Clone the legacy project:**

```bash
git clone https://github.com/JalaU-Capstones/userscli.git
cd userscli
```

**Build the CLI:**

```bash
mvn clean install
```

**Create a user:**

Ensure the `sd3.xml` config file in the CLI project points to `localhost:3307`. Then run:

```bash
java -jar target/UsersCLI-1.0-SNAPSHOT.jar -config=sd3.xml -create -n javier -l jroca -p pass123
```

---

## 3. Run the User Management API

Navigate back to the `CIS-Fase1-User-Management-API` project root.

**Build and run:**

```bash
mvn clean install
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

---

## 4. Testing the API

### Access API Documentation (Swagger UI)

Open `http://localhost:8080/swagger-ui.html` to explore endpoints.

### Example Flow: Login and Delete (v1 — MySQL)

1. **Log in** using the credentials created via the CLI:
    - Endpoint: `POST /api/v1/auth/login`
    - Body: `{"login": "jroca", "password": "pass123"}`
    - Execute and copy the returned token.

2. **Authorize:**
    - Click the **Authorize** button in Swagger UI.
    - Enter `Bearer <your_token>` and click Authorize.

3. **Get User ID:**
    - Endpoint: `GET /api/v1/users`
    - Execute and find the `id` (UUID) for login `jroca`.

4. **Delete Account (Cascade):**
    - Endpoint: `DELETE /api/v1/users/{id}`
    - Provide the UUID and execute.

> ⚠️ **Warning:** This will also delete all associated topics, ideas, and votes created by this user in the shared database.

**Success message:** `"User and all related topics, ideas, and votes have been successfully deleted."`

### Example Flow (v2 — MongoDB)

Endpoints under `/api/v2/users` will use MongoDB regardless of the `db.type` configuration.

---

## 5. Phase 3: Data Migration (MySQL to MongoDB)

This section explains how to migrate existing users from MySQL to MongoDB.

### Prerequisites for Migration

- MySQL container running with existing user data
- MongoDB container running
- Application built successfully

### Run the Migration

**Preview migration (dry run) — no data changes:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=migrate -Dspring-boot.run.arguments="--dry-run"
```

**Execute full migration (with confirmation prompt):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=migrate
```

**Clean and migrate (removes existing MongoDB data first):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=migrate -Dspring-boot.run.arguments="--clean --yes"
```

**Run using JAR file:**

```bash
java -jar target/user-management-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=migrate --spring-boot.run.arguments="--dry-run"
```

### Migration Options

| Option | Description |
|--------|------------|
| `--dry-run` | Preview what would be migrated without saving any data |
| `--clean` | Remove all existing users from MongoDB before migration |
| `--yes` | Skip confirmation prompt (for automation) |

### Running Integration Tests

The integration tests require Docker because they start MySQL and MongoDB with Testcontainers.

Run the default test suite without Docker-backed integration tests:

```bash
mvn clean test
```

Run the full suite, including `@Tag("integration")` tests, when Docker is available:

```bash
# Ensure Docker is running
docker ps

# Run unit and integration tests
mvn clean test -Pintegration-tests
```

This is also the profile to use in CI environments that provide Docker:

```bash
mvn verify -Pintegration-tests
```

### Verify Migration

After migration, verify that users were migrated correctly:

```bash
# Check all users in MongoDB
curl http://localhost:8080/api/v2/users

# Check user count
curl -s http://localhost:8080/api/v2/users | jq length

# Check specific user by ID (replace with actual UUID from the list)
curl http://localhost:8080/api/v2/users/{user-id}
```

### Expected Output

**Dry run:**

Dry run completed - no data was modified

**Successful migration:**

Migration completed successfully!
Statistics:

Total users found in MySQL: 5
Successfully migrated: 5
Final users in MongoDB: 5


### Troubleshooting Migration

**MongoDB Connection Refused:**

```bash
docker compose up -d mongodb
docker logs cis-mongo-phase1
```

**MySQL Connection Error:**

```bash
docker compose up -d mysql
docker logs cis-mysql-phase1
```

**Duplicate Key Error** (check for duplicate logins):

```bash
docker exec -it cis-mysql-phase1 mysql -u sd3user -psd3pass sd3 -e "
SELECT login, COUNT(*) 
FROM users 
GROUP BY login 
HAVING COUNT(*) > 1
"
```

### Rollback Procedure

The migration does **not** delete MySQL data. To rollback:

```bash
# Drop MongoDB collection
mongosh mongodb://localhost:27017/sd3
db.users.drop()
exit

# Re-run migration with clean flag
mvn spring-boot:run -Dspring-boot.run.profiles=migrate -Dspring-boot.run.arguments="--clean --yes"
```

---

## 6. Controlling Maintenance and Sunset Flags

The API provides mechanisms to control migration maintenance and v1 sunset flags both at startup and at runtime.

### Initializing Flags at Startup

You can set the initial state of the flags when starting the API using system properties:

```bash
# Start with migration maintenance mode enabled
mvn spring-boot:run -Dmigration.maintenance=true

# Start with v1 sunset mode enabled
mvn spring-boot:run -Dsunset.v1=true

# Combine both
mvn spring-boot:run -Dmigration.maintenance=true -Dsunset.v1=true
```

### Toggling Flags at Runtime via Console

When running the API from a terminal, you can interact with it using console commands to dynamically change the flag states without restarting the application. This console input is only available when running directly from the terminal (e.g., `mvn spring-boot:run`) and not when deployed as a service. It can be explicitly disabled by passing `-Dconsole.input.disabled=true` at startup.

**Available Commands:**

-   `maintenance on`  → Enables migration maintenance mode. Write operations on both v1 and v2 will return 503.
-   `maintenance off` → Disables migration maintenance mode. Write operations are restored for v2 (and v1 if not sunset).
-   `sunset on`       → Enables v1 sunset mode. v1 writes will return 410, v1 reads include a warning header.
-   `sunset off`      → Disables v1 sunset mode. v1 writes and reads behave normally (unless maintenance mode is on).
-   `status`          → Prints the current state of `migrationRunning` and `v1Sunset` flags.
-   `help`            → Displays a list of available commands.
-   `exit` or `quit`  → Initiates a graceful shutdown of the console listener.

**Example Console Interaction:**

```
> status
Migration running: false, V1 sunset: false
> maintenance on
Maintenance mode enabled. Write operations on both v1 and v2 will return 503.
> status
Migration running: true, V1 sunset: false
> sunset on
V1 sunset mode enabled. v1 writes will return 410, v1 reads include warning header.
> status
Migration running: true, V1 sunset: true
> maintenance off
Maintenance mode disabled. Write operations restored for v2 (and v1 if not sunset). (v1 writes remain blocked because sunset is true)
> status
Migration running: false, V1 sunset: true
> exit
Shutting down...
```

### Expected HTTP Behavior with Flags

The behavior of the API endpoints changes based on the combination of these flags. Refer to the existing "Emergency Database Fallback" and "Maintenance Mode for Writes" sections for details on HTTP status codes and messages. These flags can now be changed dynamically at runtime.

---

## 7. Stopping the Environment

```bash
# Stop API
Ctrl+C

# Stop containers
docker compose down
```
