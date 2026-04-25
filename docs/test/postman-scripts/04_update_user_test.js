pm.test("Update user returns HTTP 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Update user responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Update user returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const body = pm.response.json();
const expectedId = pm.collectionVariables.get("new_user_id");
const expectedName = pm.collectionVariables.get("updated_user_name");
const expectedLogin = pm.collectionVariables.get("updated_user_login");
const expectedPassword = pm.collectionVariables.get("updated_user_password");

pm.test("Update user response has valid user schema", function () {
    pm.expect(body).to.be.an("object");
    pm.expect(body.id).to.be.a("string").and.match(/^[0-9a-fA-F-]{36}$/);
    pm.expect(body.name).to.be.a("string").and.not.empty;
    pm.expect(body.login).to.be.a("string").and.not.empty;
});

pm.test("Updated user matches generated update values", function () {
    pm.expect(body.id).to.eql(expectedId);
    pm.expect(body.name).to.eql(expectedName);
    pm.expect(body.login).to.eql(expectedLogin);
});

pm.collectionVariables.set("new_user_name", expectedName);
pm.collectionVariables.set("new_user_login", expectedLogin);
pm.collectionVariables.set("new_user_password", expectedPassword);
pm.collectionVariables.set("updated_user_response", JSON.stringify(body));
