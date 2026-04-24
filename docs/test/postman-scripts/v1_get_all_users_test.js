pm.test("v1 get all users returns HTTP 200", function () {
    pm.response.to.have.status(200);
});

pm.test("v1 get all users responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("v1 get all users returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const body = pm.response.json();
const seedLogin = pm.variables.get("seed_login");

pm.test("v1 get all users returns an array", function () {
    pm.expect(body).to.be.an("array");
});

body.forEach((user, index) => {
    pm.test(`v1 user[${index}] matches schema`, function () {
        pm.expect(user).to.be.an("object");
        pm.expect(user.id).to.be.a("string").and.match(/^[0-9a-fA-F-]{36}$/);
        pm.expect(user.name).to.be.a("string").and.not.empty;
        pm.expect(user.login).to.be.a("string").and.not.empty;
    });
});

const seedUser = body.find((user) => user.login === seedLogin);

pm.test("v1 seed user is present in the list", function () {
    pm.expect(seedUser, `No v1 user found with login ${seedLogin}`).to.exist;
});

if (seedUser) {
    pm.collectionVariables.set("v1_seed_user_id", seedUser.id);
}
