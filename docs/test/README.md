# CIS User Management API Postman Integration Tests

This directory documents a complete Postman-based integration test suite for the CIS User Management API Phase 1. The suite covers both persistence versions through the same requests and scripts by using the collection variable `{{api_version}}`.

- `v1` -> MySQL-backed endpoints under `/api/v1`
- `v2` -> MongoDB-backed endpoints under `/api/v2`

The automated flow is designed so Postman manages tokens, IDs, and credentials through collection variables. No manual token or ID copy/paste is required once the collection and environment are configured.

## What Is Included

- A reusable 7-step end-to-end flow driven by `{{api_version}}`
- Unified public GET validation scripts for:
  - `GET /api/{{api_version}}/users`
  - `GET /api/{{api_version}}/users/{{existing_user_id}}`
- Test guidance organized by:
  - connectivity
  - structure
  - integrity
  - functionality
  - performance

## Covered Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v2/auth/login`
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `POST /api/v1/users`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`
- `GET /api/v2/users`
- `GET /api/v2/users/{id}`
- `POST /api/v2/users`
- `PUT /api/v2/users/{id}`
- `DELETE /api/v2/users/{id}`

## One-Time Prerequisites

1. Start the databases and API as documented in [running-and-testing-the-api.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/onboarding/running-and-testing-the-api.md).
2. Create the seed user in MySQL with the legacy CLI.
3. Bootstrap the same seed credentials in MongoDB once so `api_version=v2` can authenticate from step 1.

The same `seed_login` and `seed_password` values can then be reused for both versions.

## Postman Environment Setup

Create one environment named `CIS API Local` with these initial/current values:

| Variable | Example value | Required | Notes |
|---|---|---:|---|
| `base_url` | `http://localhost:8080` | Yes | API root |
| `seed_login` | `jroca` | Yes | Must exist in MySQL and MongoDB |
| `seed_password` | `pass123` | Yes | Must match the shared seed user |
| `api_version` | `v1` | Yes | Set to `v1` or `v2` before running |
| `seed_token` | empty | No | Populated by step 1 |
| `new_user_id` | empty | No | Populated by step 2 |
| `new_user_name` | empty | No | Populated by step 2 |
| `new_user_login` | empty | No | Populated by step 2 |
| `new_user_password` | empty | No | Populated by step 2 |
| `new_user_token` | empty | No | Populated by step 3 |
| `updated_user_name` | empty | No | Populated by step 4 |
| `updated_user_login` | empty | No | Populated by step 4 |
| `updated_user_password` | empty | No | Populated by step 4 |
| `updated_token` | empty | No | Populated by step 5 |
| `existing_user_id` | empty | No | Used by the unified GET-by-ID request |

## Collection Structure

Use one collection named `CIS User Management API - Integration Tests` with these folders.

### Folder `E2E Flow`

Set `api_version=v1` or `api_version=v2`, then run the same folder.

1. `01 Seed Login`
   - `POST {{base_url}}/api/{{api_version}}/auth/login`
2. `02 Create User`
   - `POST {{base_url}}/api/{{api_version}}/users`
   - Authorization: `Bearer {{seed_token}}`
3. `03 Login New User`
   - `POST {{base_url}}/api/{{api_version}}/auth/login`
4. `04 Update User`
   - `PUT {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`
   - Authorization: `Bearer {{new_user_token}}`
5. `05 Login Updated User`
   - `POST {{base_url}}/api/{{api_version}}/auth/login`
6. `06 Delete User`
   - `DELETE {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`
   - Authorization: `Bearer {{updated_token}}`
7. `07 Verify Deletion`
   - `GET {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`

### Folder `Public GET`

Set `api_version` to the target version, then run:

1. `GET {{base_url}}/api/{{api_version}}/users`
2. `GET {{base_url}}/api/{{api_version}}/users/{{existing_user_id}}`

## Importing The Scripts

For each request:

1. Open the request in Postman.
2. Paste the matching file from [postman-scripts/README.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/postman-scripts/README.md).
3. Save the request.

For `GET {{base_url}}/api/{{api_version}}/users/{{existing_user_id}}`, also add the documented request-level pre-request script from `postman-scripts/README.md` so Postman resolves a valid ID automatically.

## Running The Full Collection

### Postman Collection Runner

1. Select the collection.
2. Select environment `CIS API Local`.
3. Set `api_version=v1`.
4. Run the `E2E Flow` folder and the `Public GET` folder.
5. Set `api_version=v2`.
6. Run the same folders again.

Expected outcome:

- Steps 1, 3, and 5 return `200`
- Step 2 returns `201`
- Steps 4 and 6 return `200`
- Step 7 returns `404`
- `GET /api/{{api_version}}/users` returns `200`
- `GET /api/{{api_version}}/users/{{existing_user_id}}` returns `200` for an existing user, or `404` when you intentionally point it to a missing ID
- Every scripted request validates response time `< 500 ms`

### Newman

Export the collection and environment from Postman, then run:

```bash
newman run CIS_User_Management_API_Integration.postman_collection.json \
  -e CIS_API_Local.postman_environment.json \
  --env-var base_url=http://localhost:8080 \
  --env-var seed_login=jroca \
  --env-var seed_password=pass123 \
  --env-var api_version=v1
```

Run the same command again with `--env-var api_version=v2`.

## Running Tests For Both API Versions

### Option 1: Change `api_version` and rerun

1. Set `api_version=v1` in the environment.
2. Run the collection.
3. Set `api_version=v2`.
4. Run the same collection again.

### Option 2: Use Collection Runner with iteration data

Create a JSON data file such as:

```json
[
  { "api_version": "v1" },
  { "api_version": "v2" }
]
```

Run the same collection in Postman Collection Runner with that file. The unified requests and scripts will use the iteration value of `api_version` for each pass.

## Troubleshooting

- `401 Unauthorized` on step 1:
  - verify `seed_login` and `seed_password`
  - confirm the seed user exists in the target store
- `401 Unauthorized` on step 2 or step 4:
  - verify the request is using the correct bearer token variable
- `403 Forbidden` on step 4 or step 6:
  - confirm the token belongs to the same user being updated or deleted
- `400 Bad Request` on step 2 or step 4:
  - logins must stay at or below 20 characters
  - passwords must be between 6 and 100 characters
- `404 Not Found` on public GET by ID:
  - verify the request-level pre-request script populated `existing_user_id`
  - run `GET /api/{{api_version}}/users` first if needed

## Reference Files

- [QUICK_REFERENCE.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/QUICK_REFERENCE.md)
- [DELIVERY_SUMMARY.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/DELIVERY_SUMMARY.md)
- [connectivity.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/connectivity.md)
- [structure.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/structure.md)
- [integrity.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/integrity.md)
- [functionality.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/functionality.md)
- [performance.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/performance.md)
