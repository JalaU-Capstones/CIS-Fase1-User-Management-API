const apiVersion = pm.collectionVariables.get("api_version") || pm.environment.get("api_version") || "v1";
pm.collectionVariables.set("api_version", apiVersion);

pm.test("Get all users returns HTTP 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Get all users responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Get all users returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const body = pm.response.json();

pm.test("Get all users returns an array", function () {
    pm.expect(body).to.be.an("array");
});

body.forEach((user, index) => {
    pm.test(`user[${index}] matches schema for ${apiVersion}`, function () {
        pm.expect(user).to.be.an("object");
        pm.expect(user.id).to.be.a("string").and.match(/^[0-9a-fA-F-]{36}$/);
        pm.expect(user.name).to.be.a("string").and.not.empty;
        pm.expect(user.login).to.be.a("string").and.not.empty;
    });
});

const seedLogin = pm.variables.get("seed_login");
const existingUser = body.find((user) => user.login === seedLogin) || body[0];

pm.test("Get all users resolves at least one existing user ID", function () {
    pm.expect(existingUser, `No users returned for ${apiVersion}`).to.exist;
});

if (existingUser) {
    pm.collectionVariables.set("existing_user_id", existingUser.id);
}
