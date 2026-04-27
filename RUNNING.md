# Running and Testing

For the full local setup guide, see `docs/onboarding/running-and-testing-the-api.md`.

## Test Commands

Run the default CI-style test suite without Docker-backed integration tests:

```bash
mvn clean test
```

Run the full suite, including integration tests, when Docker is available:

```bash
docker ps
mvn clean test -Pintegration-tests
```

The integration tests use Testcontainers for MySQL and MongoDB, so they require Docker access.
