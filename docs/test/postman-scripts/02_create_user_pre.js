pm.expect(pm.collectionVariables.get("seed_token"), "seed_token must exist before creating a user").to.be.a("string").and.not.empty;

const timestamp = Date.now().toString();
const randomSuffix = pm.variables.replaceIn("{{$randomInt}}").replace(/[^0-9]/g, "").slice(0, 4) || "1000";
const uniquePart = `${timestamp.slice(-6)}${randomSuffix}`.slice(0, 10);

const generatedName = `API User ${uniquePart}`;
const generatedLogin = `u${uniquePart}`.slice(0, 20);
const generatedPassword = `Pw${uniquePart}!9`;

pm.collectionVariables.set("new_user_name", generatedName);
pm.collectionVariables.set("new_user_login", generatedLogin);
pm.collectionVariables.set("new_user_password", generatedPassword);
