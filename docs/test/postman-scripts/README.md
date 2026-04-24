# Postman Scripts

This directory contains the exact JavaScript to paste into Postman request tabs.

## Request-to-File Mapping

| Request | Pre-request Script | Tests |
|---|---|---|
| `01 Seed Login` | `01_seed_login_pre.js` | `01_seed_login_test.js` |
| `02 Create User` | `02_create_user_pre.js` | `02_create_user_test.js` |
| `03 Login New User` | `03_login_new_user_pre.js` | `03_login_new_user_test.js` |
| `04 Update User` | `04_update_user_pre.js` | `04_update_user_test.js` |
| `05 Login Updated User` | `05_login_updated_pre.js` | `05_login_updated_test.js` |
| `06 Delete User` | none | `06_delete_user_test.js` |
| `07 Verify Deletion` | none | `07_verify_deletion_test.js` |
| `GET /api/v1/users` | none | `v1_get_all_users_test.js` |
| `GET /api/v1/users/{{v1_seed_user_id}}` | none | `v1_get_user_by_id_test.js` |
| `GET /api/v2/users` | none | `v2_get_all_users_test.js` |
| `GET /api/v2/users/{{v2_seed_user_id}}` | none | `v2_get_user_by_id_test.js` |

## Request Bodies

### `01 Seed Login`

```json
{
  "login": "{{seed_login}}",
  "password": "{{seed_password}}"
}
```

### `02 Create User`

```json
{
  "name": "{{new_user_name}}",
  "login": "{{new_user_login}}",
  "password": "{{new_user_password}}"
}
```

### `03 Login New User`

```json
{
  "login": "{{new_user_login}}",
  "password": "{{new_user_password}}"
}
```

### `04 Update User`

```json
{
  "name": "{{updated_user_name}}",
  "login": "{{updated_user_login}}",
  "password": "{{updated_user_password}}"
}
```

### `05 Login Updated User`

```json
{
  "login": "{{updated_user_login}}",
  "password": "{{updated_user_password}}"
}
```

## Authorization Values

- `02 Create User` -> `Bearer {{seed_token}}`
- `04 Update User` -> `Bearer {{new_user_token}}`
- `06 Delete User` -> `Bearer {{updated_token}}`

## Import Instructions

1. Create the request in Postman.
2. Paste the matching pre-request script if one exists.
3. Paste the matching test script.
4. Save the request.
5. Run requests in order.

## Troubleshooting

- If a request cannot resolve `{{new_user_id}}`, previous steps did not run successfully.
- If a GET-by-ID request cannot resolve `{{v1_seed_user_id}}` or `{{v2_seed_user_id}}`, run the corresponding GET-all request first.
