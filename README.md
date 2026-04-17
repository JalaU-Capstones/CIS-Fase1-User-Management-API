# CIS - Phase 1: User Management API

REST API for employee user management (CRUD) that coexists with the legacy CLI Users System.

## Tech Stack (per ADRs)
- Java 21 (LTS)
- Spring Boot 3.4.x
- Maven
- Spring Data JPA + Hibernate (ORM)
- Spring Data MongoDB
- MySQL 8 (shared `users` table)
- MongoDB (v2 storage)
- JUnit 5 + Mockito + AssertJ (testing)
- Layered architecture (controllers, services, repositories, DTOs)

## Dual Persistence & Versioning
This API supports dual persistence:
- **v1 (/api/v1):** Uses MySQL as the primary data store (default).
- **v2 (/api/v2):** Uses MongoDB as the primary data store.

### How to switch between MySQL and MongoDB
The application uses a property `db.type` to decide which repository implementation to use for the default `UserPersistencePort`.

In `application.yml` or as an environment variable:
```yaml
db:
  type: mysql   # or mongo
```

Regardless of this setting, the `/api/v2` endpoints will always attempt to use the MongoDB adapter via `MongoUserService`.

## Prerequisites
- Java 21 (OpenJDK or Eclipse Temurin)
- Maven 3.9+
- Docker & Docker Compose
- IntelliJ IDEA (recommended)

## Clone the Repository
```bash
git clone https://gitlab.com/jala-university1/cohort-5/ES.CO.CSSD-232.GA.T1.26.M2/secci-n-c/capstone-sd3/idea-flow/CIS-Fase1-User-Management-API.git
cd CIS-Fase1-User-Management-API
```

## Database Setup (Docker MySQL & MongoDB)
We use Docker to run MySQL 8.0 and MongoDB.

1. Start the databases:
```bash
docker compose up -d
```

2. Verify they are running:
```bash
docker ps
# You should see containers 'cis-mysql-phase1' and 'cis-mongo-phase1'
```

3. MySQL Connection:
- Host: `localhost`, Port: `3307`, Database: `sd3`, User: `sd3user`, Password: `sd3pass`

4. MongoDB Connection:
- URI: `mongodb://localhost:27017/user_management`

## Run the Application
```bash
mvn clean install
mvn spring-boot:run
```

API available at: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html

### Test Coverage Report (JaCoCo)
```bash
mvn clean test
```
Report: `target/site/jacoco/index.html`

## Development Workflow
- Create feature branches: `feature/#XX-description-initials`
- Commit small & descriptive
- Open MR to main with at least 2 approvals
- Use `mvn test` for unit/integration tests

See Wiki for full ADRs, C4 diagrams, API contract, and branching model.
