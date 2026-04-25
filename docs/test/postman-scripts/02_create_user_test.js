pm.test("Create user returns HTTP 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Create user responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Create user returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const body = pm.response.json();
const expectedName = pm.collectionVariables.get("new_user_name");
const expectedLogin = pm.collectionVariables.get("new_user_login");

pm.test("Create user response has valid user schema", function () {
    pm.expect(body).to.be.an("object");
    pm.expect(body.id).to.be.a("string").and.match(/^[0-9a-fA-F-]{36}$/);
    pm.expect(body.name).to.be.a("string").and.not.empty;
    pm.expect(body.login).to.be.a("string").and.not.empty;
});

pm.test("Created user matches generated request values", function () {
    pm.expect(body.name).to.eql(expectedName);
    pm.expect(body.login).to.eql(expectedLogin);
});

pm.collectionVariables.set("new_user_id", body.id);
pm.collectionVariables.set("created_user_response", JSON.stringify(body));
