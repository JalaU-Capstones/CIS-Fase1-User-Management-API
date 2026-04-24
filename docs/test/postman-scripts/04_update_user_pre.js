["new_user_id", "new_user_token"].forEach((name) => {
    const value = pm.collectionVariables.get(name);
    pm.expect(value, `${name} must exist before update`).to.be.a("string").and.not.empty;
});

const timestamp = Date.now().toString();
const randomSuffix = pm.variables.replaceIn("{{$randomInt}}").replace(/[^0-9]/g, "").slice(0, 4) || "2000";
const uniquePart = `${timestamp.slice(-6)}${randomSuffix}`.slice(0, 10);

const updatedName = `Updated User ${uniquePart}`;
const updatedLogin = `upd${uniquePart}`.slice(0, 20);
const updatedPassword = `Np${uniquePart}!7`;

pm.collectionVariables.set("updated_user_name", updatedName);
pm.collectionVariables.set("updated_user_login", updatedLogin);
pm.collectionVariables.set("updated_user_password", updatedPassword);
