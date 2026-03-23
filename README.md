# CIS - Phase 1: User Management API

REST API for employee user management (CRUD) that coexists with the legacy CLI Users System.

## Tech Stack (per ADRs)
- Java 21 (LTS)
- Spring Boot 3.4.x
- Maven
- Spring Data JPA + Hibernate (ORM)
- MySQL 8 (shared `users` table)
- JUnit 5 + Mockito + AssertJ (testing)
- Layered architecture (controllers, services, repositories, DTOs)

## Prerequisites
- Java 21 (OpenJDK or Eclipse Temurin)
- Maven 3.9+
- Docker & Docker Compose
- IntelliJ IDEA (recommended)

## Clone the Repository
```bash
git clone https://gitlab.com/jala-university1/cohort-5/ES.CO.CSSD-232.GA.T1.26.M2/secci-n-c/capstone-sd3/idea-flow/CIS-Fase1-User-Management-API.git
# or SSH
# git clone git@gitlab.com:jala-university1/cohort-5/ES.CO.CSSD-232.GA.T1.26.M2/secci-n-c/capstone-sd3/idea-flow/CIS-Fase1-User-Management-API.git
cd CIS-Fase1-User-Management-API
```

## Database Setup (Docker MySQL)
We use Docker to run MySQL 8.0 with the exact legacy schema (no changes allowed).

1. Start the database:
```bash
docker compose up -d
```

2. Verify it's running:
```bash
docker ps
# You should see container 'cis-mysql-phase1'
```

3. Connect locally (optional, for debugging):
- Host: `localhost`
- Port: `3307`
- Database: `sd3`
- User: `sd3user`
- Password: `sd3pass`

**Note:** The legacy CLI config uses port 3307 → update its XML config if testing locally.

## Run the Application
```bash
mvn clean install
mvn spring-boot:run
```

API available at: http://localhost:8080

### Test Coverage Report (JaCoCo)

To ensure high code quality, we use JaCoCo to generate test coverage reports.

1. **Generate the Report**
   Run the following command in the project root:
   ```bash
   mvn clean test
   ```

2. **Locate the Report**
   The report is generated in: `target/site/jacoco/index.html`

3. **View the Report**
   - Open the file `target/site/jacoco/index.html` in your web browser.
   - Or, double-click the file if you are browsing the directory.

4. **Understanding Coverage**
   The report shows the percentage of code lines covered by unit tests. A higher percentage indicates that more code paths are being tested, reducing the likelihood of bugs.

## Development Workflow
- Create feature branches: `feature/#XX-description-initials`
- Commit small & descriptive
- Open MR to main with at least 2 approvals
- Use `mvn test` for unit/integration tests

See Wiki for full ADRs, C4 diagrams, API contract, and branching model.