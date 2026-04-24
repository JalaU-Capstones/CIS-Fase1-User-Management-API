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
| `GET /api/{{api_version}}/users` | none | `get_all_users_test.js` |
| `GET /api/{{api_version}}/users/{{existing_user_id}}` | request-level snippet in this README | `get_user_by_id_test.js` |

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

## Unified GET By ID Pre-request Script

Paste this into the `Pre-request Script` tab of `GET {{base_url}}/api/{{api_version}}/users/{{existing_user_id}}`:

```javascript
const apiVersion = pm.collectionVariables.get("api_version") || pm.environment.get("api_version") || "v1";
pm.collectionVariables.set("api_version", apiVersion);

const existingUserId = pm.collectionVariables.get("existing_user_id");
const lastCreatedUserId = pm.collectionVariables.get("new_user_id");

if (lastCreatedUserId) {
    pm.collectionVariables.set("existing_user_id", lastCreatedUserId);
} else if (!existingUserId) {
    pm.sendRequest(`${pm.variables.get("base_url")}/api/${apiVersion}/users`, (error, response) => {
        pm.expect(error).to.equal(null);
        pm.expect(response).to.have.property("code", 200);

        const users = response.json();
        pm.expect(users).to.be.an("array").and.not.empty;

        const seedLogin = pm.variables.get("seed_login");
        const resolvedUser = users.find((user) => user.login === seedLogin) || users[0];
        pm.collectionVariables.set("existing_user_id", resolvedUser.id);
    });
}
```

This keeps the folder limited to the exact files requested while still providing the required pre-request automation for the by-ID GET request.

## Import Instructions

1. Create the request in Postman.
2. Paste the matching pre-request script if one exists.
3. Paste the matching test script.
4. For `GET /api/{{api_version}}/users/{{existing_user_id}}`, also paste the pre-request snippet above.
5. Save the request.
6. Run requests in order.

## Troubleshooting

- If a request cannot resolve `{{new_user_id}}`, previous steps did not run successfully.
- If a GET-by-ID request cannot resolve `{{existing_user_id}}`, run the unified GET-all request first or use the pre-request snippet above.
