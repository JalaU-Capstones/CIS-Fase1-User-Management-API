# Performance Tests

## Purpose

Performance tests in this documentation are lightweight integration checks intended to catch obvious latency regressions during local verification and collection runs.

## Covered Endpoints

- all scripted auth endpoints
- all scripted CRUD endpoints
- the unified public GET endpoints

## Performance Threshold

Each provided Postman test script asserts:

- response time `< 500 ms`

This threshold is intentionally strict for a local developer environment and should be treated as an early warning, not as a production SLA.

## Setup Instructions

1. Start Docker containers and the Spring Boot API locally.
2. Avoid running heavy background tasks during the measurement.
3. Run the collection from Postman Runner or Newman.
4. Repeat with `api_version=v1` and `api_version=v2`.

## Expected Results

- all requests remain under `500 ms`
- repeated runs stay stable for both `v1` and `v2`
- the same unified public GET scripts stay under threshold when `api_version` changes

## Troubleshooting

- If many requests exceed the threshold:
  - confirm Docker containers are healthy
  - confirm the app is running without startup errors
  - retry after the JVM warms up
- If only login is slow on the first request:
  - rerun once; startup and class loading can affect the first hit
- If Mongo-only requests are slow:
  - verify the Mongo container is healthy and reachable
