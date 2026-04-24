# Functionality Tests

## Purpose

Functionality tests validate the user lifecycle and the business rules enforced by the API, especially authentication and ownership-sensitive operations.

## Covered Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v2/auth/login`
- `POST /api/{{api_version}}/users`
- `PUT /api/{{api_version}}/users/{id}`
- `DELETE /api/{{api_version}}/users/{id}`
- `GET /api/{{api_version}}/users`
- `GET /api/{{api_version}}/users/{id}`

## Business Rules Verified

- create requires authentication
- update requires authentication and ownership
- delete requires authentication and ownership
- public GET requests do not require authentication
- updated credentials become the credentials of record for the user
- delete messages differ by API version and are validated accordingly

## Setup Instructions

1. Complete the environment setup from `README.md`.
2. Ensure the seed credentials exist in both MySQL and MongoDB.
3. Import the scripts from `postman-scripts/`.
4. Set `api_version=v1` or `api_version=v2` before running.
5. Run the e2e folder.
6. Run the unified public GET requests.

## Expected Results

### v1

- seed login succeeds
- create returns `201`
- update and delete succeed only with the user’s own token
- delete response text equals:
  - `User and all related topics, ideas, and votes have been successfully deleted.`

### v2

- seed login succeeds
- create returns `201`
- update and delete succeed only with the user’s own token
- delete response text equals:
  - `User has been successfully deleted from MongoDB.`

## Troubleshooting

- If step 2 fails for `v2` but works for `v1`, the v2 seed user is probably missing.
- If update or delete returns `403`, the token does not belong to the user identified by `{{new_user_id}}`.
