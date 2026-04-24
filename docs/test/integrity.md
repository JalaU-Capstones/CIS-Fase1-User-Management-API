# Integrity Tests

## Purpose

Integrity tests verify that data returned by the API matches the inputs that created or changed it, and that later requests observe the same state transitions.

## Covered Endpoints

- `POST /api/{version}/users`
- `PUT /api/{version}/users/{id}`
- `POST /api/{version}/auth/login`
- `DELETE /api/{version}/users/{id}`
- `GET /api/{version}/users/{id}`
- `GET /api/v1/users`
- `GET /api/v2/users`

## Postman Setup

Use:

- `base_url`
- `seed_login`
- `seed_password`
- `api_version`

The scripts will populate:

- `new_user_id`
- `new_user_login`
- `new_user_password`
- `updated_user_login`
- `updated_user_password`
- `v1_seed_user_id`
- `v2_seed_user_id`

## Integrity Checks Performed

### Create

- response `id` is present and non-empty
- response `name` equals `{{new_user_name}}`
- response `login` equals `{{new_user_login}}`

### Update

- response `id` remains equal to `{{new_user_id}}`
- response `name` equals `{{updated_user_name}}`
- response `login` equals `{{updated_user_login}}`
- collection variables are updated so the next login request uses the changed credentials

### Login After Update

- login succeeds with the updated credentials
- a fresh `updated_token` is stored

### Delete and Verification

- delete succeeds with the updated token
- a later `GET /users/{{new_user_id}}` returns `404`

### Public GET by ID

- `GET /api/v1/users` stores the ID belonging to `seed_login` in `v1_seed_user_id`
- `GET /api/v2/users` stores the ID belonging to `seed_login` in `v2_seed_user_id`

## How To Run

1. Run the 7-step e2e folder.
2. Repeat it with the other `api_version`.
3. Run public GET for `v1`.
4. Run public GET for `v2`.

## Expected Results

- no manual value transfer between requests
- no mismatch between request-generated values and response fields
- no stale credentials after update
- deleted users are not retrievable afterwards

## Troubleshooting

- If step 3 fails after a successful create:
  - confirm step 2 stored `new_user_login` and `new_user_password`
- If step 5 fails after a successful update:
  - confirm step 4 stored `updated_user_login` and `updated_user_password`
- If public GET by ID fails with an empty URL placeholder:
  - run the matching `get all users` request first
