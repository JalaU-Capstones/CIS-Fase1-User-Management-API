# Test Structure

## Purpose

This document explains the folder layout under `docs/test/` and the intended request structure inside Postman.

## Documentation Tree

```text
docs/
├── onboarding
│   └── running-and-testing-the-api.md
└── test/
    ├── README.md
    ├── QUICK_REFERENCE.md
    ├── DELIVERY_SUMMARY.md
    ├── connectivity.md
    ├── structure.md
    ├── integrity.md
    ├── functionality.md
    ├── performance.md
    └── postman-scripts/
        ├── README.md
        ├── 01_seed_login_pre.js
        ├── 01_seed_login_test.js
        ├── 02_create_user_pre.js
        ├── 02_create_user_test.js
        ├── 03_login_new_user_pre.js
        ├── 03_login_new_user_test.js
        ├── 04_update_user_pre.js
        ├── 04_update_user_test.js
        ├── 05_login_updated_pre.js
        ├── 05_login_updated_test.js
        ├── 06_delete_user_test.js
        ├── 07_verify_deletion_test.js
        ├── v1_get_all_users_test.js
        ├── v1_get_user_by_id_test.js
        ├── v2_get_all_users_test.js
        └── v2_get_user_by_id_test.js
```

## Postman Collection Layout

## Folder `E2E Flow`

Run with `api_version=v1`, then rerun with `api_version=v2`.

1. `01 Seed Login`
2. `02 Create User`
3. `03 Login New User`
4. `04 Update User`
5. `05 Login Updated User`
6. `06 Delete User`
7. `07 Verify Deletion`

## Folder `Public GET - v1`

1. `Get All Users`
2. `Get User By ID`

## Folder `Public GET - v2`

1. `Get All Users`
2. `Get User By ID`

## Variable Flow

```text
seed_login + seed_password
  -> 01 seed login
  -> seed_token
  -> 02 create user
  -> new_user_id + new_user_login + new_user_password + new_user_name
  -> 03 login new user
  -> new_user_token
  -> 04 update user
  -> updated_user_login + updated_user_password + updated_user_name
  -> 05 login updated user
  -> updated_token
  -> 06 delete user
  -> 07 verify deletion
```

## Setup Instructions

1. Create the environment variables defined in `README.md`.
2. Create the request folders above.
3. For each request, paste the script file with the matching name.
4. Use collection variables, not environment variables, for transient data generated during the run.

## Expected Results

- The e2e folder is self-contained.
- The public GET folders are independent except that each by-ID request expects the matching `get all` request to populate the seed ID.

## Troubleshooting

- If variables appear empty between requests, confirm the scripts use `pm.collectionVariables.set(...)`.
- If you accidentally used environment variables for the transient data, clear them and rerun the folder.
