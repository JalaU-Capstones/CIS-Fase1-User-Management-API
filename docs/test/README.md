# CIS User Management API Postman Integration Tests

This directory documents a complete Postman-based integration test suite for the CIS User Management API Phase 1. The suite covers both persistence versions:

- `v1` -> `POST /api/v1/auth/login`, `GET|POST|PUT|DELETE /api/v1/users/**` backed by MySQL
- `v2` -> `POST /api/v2/auth/login`, `GET|POST|PUT|DELETE /api/v2/users/**` backed by MongoDB

The automated flow is designed so Postman manages tokens, IDs, and credentials through collection variables. No manual copy/paste is required once the collection and environment are configured.

## What Is Included

- A reusable 7-step end-to-end flow driven by `{{api_version}}`
- Public GET validation scripts for:
  - `GET /api/v1/users`
  - `GET /api/v1/users/{{v1_seed_user_id}}`
  - `GET /api/v2/users`
  - `GET /api/v2/users/{{v2_seed_user_id}}`
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
3. Bootstrap the same seed user in MongoDB once. The simplest approach is:
   - log in through `/api/v1/auth/login` with the CLI-created user
   - call `POST /api/v2/users` with that token
   - create the same `login` and `password` you want to use as the shared seed for v2

The same `seed_login` and `seed_password` values can then be reused for both versions.

## Postman Environment Setup

Create one environment named `CIS API Local` with these initial/current values:

| Variable | Example value | Required | Notes |
|---|---|---:|---|
| `base_url` | `http://localhost:8080` | Yes | API root |
| `seed_login` | `jroca` | Yes | Must exist in MySQL and MongoDB |
| `seed_password` | `pass123` | Yes | Must match the shared seed user |
| `api_version` | `v1` | Yes | Run the e2e flow once with `v1` and once with `v2` |
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
| `v1_seed_user_id` | empty | No | Populated by `v1_get_all_users_test.js` |
| `v2_seed_user_id` | empty | No | Populated by `v2_get_all_users_test.js` |

## Collection Structure

Use one collection named `CIS User Management API - Integration Tests` with these folders.

### Folder `E2E Flow`

Run this folder twice: first with `api_version=v1`, then with `api_version=v2`.

1. `01 Seed Login`
   - `POST {{base_url}}/api/{{api_version}}/auth/login`
   - Body:
     ```json
     {
       "login": "{{seed_login}}",
       "password": "{{seed_password}}"
     }
     ```
2. `02 Create User`
   - `POST {{base_url}}/api/{{api_version}}/users`
   - Authorization: `Bearer Token` -> `{{seed_token}}`
   - Body:
     ```json
     {
       "name": "{{new_user_name}}",
       "login": "{{new_user_login}}",
       "password": "{{new_user_password}}"
     }
     ```
3. `03 Login New User`
   - `POST {{base_url}}/api/{{api_version}}/auth/login`
   - Body:
     ```json
     {
       "login": "{{new_user_login}}",
       "password": "{{new_user_password}}"
     }
     ```
4. `04 Update User`
   - `PUT {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`
   - Authorization: `Bearer Token` -> `{{new_user_token}}`
   - Body:
     ```json
     {
       "name": "{{updated_user_name}}",
       "login": "{{updated_user_login}}",
       "password": "{{updated_user_password}}"
     }
     ```
5. `05 Login Updated User`
   - `POST {{base_url}}/api/{{api_version}}/auth/login`
   - Body:
     ```json
     {
       "login": "{{updated_user_login}}",
       "password": "{{updated_user_password}}"
     }
     ```
6. `06 Delete User`
   - `DELETE {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`
   - Authorization: `Bearer Token` -> `{{updated_token}}`
7. `07 Verify Deletion`
   - `GET {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`

### Folder `Public GET - v1`

1. `GET {{base_url}}/api/v1/users`
2. `GET {{base_url}}/api/v1/users/{{v1_seed_user_id}}`

Run the collection item 1 before 2 so the seed ID is auto-detected from `seed_login`.

### Folder `Public GET - v2`

1. `GET {{base_url}}/api/v2/users`
2. `GET {{base_url}}/api/v2/users/{{v2_seed_user_id}}`

Run the collection item 1 before 2 so the seed ID is auto-detected from `seed_login`.

## Importing the Scripts

For each request:

1. Open the request in Postman.
2. Paste the matching file from [postman-scripts/README.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/postman-scripts/README.md) into the `Pre-request Script` tab when a `_pre.js` file exists.
3. Paste the matching file into the `Tests` tab.
4. Save the request.

## Running the Full Collection

### Postman Collection Runner

1. Select the collection.
2. Select environment `CIS API Local`.
3. Run the `E2E Flow` folder with `api_version=v1`.
4. Change `api_version` to `v2`.
5. Run the `E2E Flow` folder again.
6. Run the public GET folders.

Expected outcome:

- Steps 1, 3, and 5 return `200`
- Step 2 returns `201`
- Steps 4 and 6 return `200`
- Step 7 returns `404`
- Public GET requests return `200`, unless the requested ID does not exist, in which case they must return `404`
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
  - run the corresponding `get all` request first so the seed ID variable is populated

## Reference Files

- [QUICK_REFERENCE.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/QUICK_REFERENCE.md)
- [DELIVERY_SUMMARY.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/DELIVERY_SUMMARY.md)
- [connectivity.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/connectivity.md)
- [structure.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/structure.md)
- [integrity.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/integrity.md)
- [functionality.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/functionality.md)
- [performance.md](/home/zeus/Jala/Desarrollo%20de%20Software%203/Capstone/Phase1/CIS-Fase1-User-Management-API/docs/test/performance.md)
