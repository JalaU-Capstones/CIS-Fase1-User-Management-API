# Running and Testing the CIS User Management API

This document provides instructions on how to run and test the CIS User Management API locally, including interaction with the legacy CLI system.

---

## Dual Persistence & Versioning

This API supports dual persistence:

- **v1 (`/api/v1`):** Uses MySQL as the primary data store (default).
- **v2 (`/api/v2`):** Uses MongoDB as the primary data store.

### Switching Persistence (v1 default)

The application uses the `db.type` property to choose which repository implementation to use for the default `UserPersistencePort`.

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
- **MongoDB:** `mongodb://localhost:27017/user_management`

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

The integration tests require Docker to run. They are disabled by default in the CI/CD pipeline.

To run them manually:

```bash
# Ensure Docker is running
docker ps

# Run integration tests
mvn test -Dgroups=integration
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
mongosh mongodb://localhost:27017/user_management
db.users.drop()
exit

# Re-run migration with clean flag
mvn spring-boot:run -Dspring-boot.run.profiles=migrate -Dspring-boot.run.arguments="--clean --yes"
```

---

## 6. Stopping the Environment

```bash
# Stop API
Ctrl+C

# Stop containers
docker compose down
```