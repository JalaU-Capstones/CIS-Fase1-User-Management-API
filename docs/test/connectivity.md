# Connectivity Tests

## Purpose

Connectivity tests verify that each endpoint is reachable, uses the correct HTTP method, and returns the expected status code for the happy path and key negative path.

## Covered Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v2/auth/login`
- `GET /api/{{api_version}}/users`
- `GET /api/{{api_version}}/users/{id}`
- `POST /api/{{api_version}}/users`
- `PUT /api/{{api_version}}/users/{id}`
- `DELETE /api/{{api_version}}/users/{id}`

## Postman Setup

Use the environment variables from [README.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/README.md). The minimum required variables are:

- `base_url`
- `seed_login`
- `seed_password`
- `api_version`

## How To Run

1. Configure the request URLs exactly as documented in `README.md`.
2. Paste the matching scripts into each request.
3. Set `api_version` to the target version.
4. Run the 7-step e2e folder in order.
5. Run the unified public GET requests.
6. Repeat with the other `api_version`.

## Expected Results

| Endpoint pattern | Success status | Negative status covered |
|---|---:|---:|
| `POST /api/{version}/auth/login` | `200` | `401` |
| `POST /api/{version}/users` | `201` | `400`, `401` |
| `PUT /api/{version}/users/{id}` | `200` | `400`, `401`, `403`, `404` |
| `DELETE /api/{version}/users/{id}` | `200` | `401`, `403`, `404` |
| `GET /api/{version}/users` | `200` | none expected for public access |
| `GET /api/{version}/users/{id}` | `200` | `404` |

## Troubleshooting

- `404` on every endpoint:
  - verify `base_url`
  - verify the API is running on port `8080`
- `405 Method Not Allowed`:
  - confirm the request method matches the documentation
- `401` on create, update, or delete:
  - check the bearer token source for that step
- `404` on step 7:
  - that is the expected result after a successful delete
