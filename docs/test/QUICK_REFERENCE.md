# Quick Reference

## Purpose

This file is the fast setup guide for running the documented Postman integration tests with the least amount of context switching.

## Covered Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v2/auth/login`
- `GET|POST|PUT|DELETE /api/v1/users/**`
- `GET|POST|PUT|DELETE /api/v2/users/**`

## Required Postman Variables

| Variable | Value |
|---|---|
| `base_url` | `http://localhost:8080` |
| `seed_login` | shared seed login present in v1 and v2 |
| `seed_password` | shared seed password present in v1 and v2 |
| `api_version` | `v1` or `v2` |

## Request URLs

### Automated E2E Flow

1. `POST {{base_url}}/api/{{api_version}}/auth/login`
2. `POST {{base_url}}/api/{{api_version}}/users`
3. `POST {{base_url}}/api/{{api_version}}/auth/login`
4. `PUT {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`
5. `POST {{base_url}}/api/{{api_version}}/auth/login`
6. `DELETE {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`
7. `GET {{base_url}}/api/{{api_version}}/users/{{new_user_id}}`

### Public GET Requests

- `GET {{base_url}}/api/v1/users`
- `GET {{base_url}}/api/v1/users/{{v1_seed_user_id}}`
- `GET {{base_url}}/api/v2/users`
- `GET {{base_url}}/api/v2/users/{{v2_seed_user_id}}`

## Bodies

### 01 Seed Login

```json
{
  "login": "{{seed_login}}",
  "password": "{{seed_password}}"
}
```

### 02 Create User

```json
{
  "name": "{{new_user_name}}",
  "login": "{{new_user_login}}",
  "password": "{{new_user_password}}"
}
```

### 03 Login New User

```json
{
  "login": "{{new_user_login}}",
  "password": "{{new_user_password}}"
}
```

### 04 Update User

```json
{
  "name": "{{updated_user_name}}",
  "login": "{{updated_user_login}}",
  "password": "{{updated_user_password}}"
}
```

### 05 Login Updated User

```json
{
  "login": "{{updated_user_login}}",
  "password": "{{updated_user_password}}"
}
```

## Authorization

- Step 2: `Bearer {{seed_token}}`
- Step 4: `Bearer {{new_user_token}}`
- Step 6: `Bearer {{updated_token}}`
- Public GET requests: no authorization

## Expected Results

| Request | Expected status |
|---|---:|
| 01 Seed Login | `200` |
| 02 Create User | `201` |
| 03 Login New User | `200` |
| 04 Update User | `200` |
| 05 Login Updated User | `200` |
| 06 Delete User | `200` |
| 07 Verify Deletion | `404` |
| Public GET all | `200` |
| Public GET by valid ID | `200` |
| Public GET by missing ID | `404` |

## Troubleshooting

- If `new_user_login` exceeds 20 characters, shorten the prefix in the pre-request script.
- If step 5 fails, step 4 did not persist the updated credentials.
- If `v1_seed_user_id` or `v2_seed_user_id` is empty, run the matching `get all users` request first.
