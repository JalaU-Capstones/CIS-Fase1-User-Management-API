const requiredVars = ["base_url", "seed_login", "seed_password"];

requiredVars.forEach((name) => {
    const value = pm.variables.get(name);
    pm.expect(value, `Variable ${name} must be defined`).to.be.a("string").and.not.empty;
});

const apiVersion = pm.collectionVariables.get("api_version") || pm.environment.get("api_version") || "v1";
pm.expect(["v1", "v2"]).to.include(apiVersion);
pm.collectionVariables.set("api_version", apiVersion);

[
    "seed_token",
    "new_user_id",
    "new_user_name",
    "new_user_login",
    "new_user_password",
    "new_user_token",
    "updated_user_name",
    "updated_user_login",
    "updated_user_password",
    "updated_token",
    "last_deleted_user_id"
].forEach((name) => pm.collectionVariables.unset(name));
