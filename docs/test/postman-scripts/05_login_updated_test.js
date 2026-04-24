pm.test("Login with updated credentials returns HTTP 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Login with updated credentials responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Login with updated credentials returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const body = pm.response.json();

pm.test("Updated login response has valid auth schema", function () {
    pm.expect(body).to.be.an("object");
    pm.expect(body.token).to.be.a("string").and.not.empty;
    if (Object.prototype.hasOwnProperty.call(body, "message") && body.message !== null) {
        pm.expect(body.message).to.be.a("string");
    }
});

pm.collectionVariables.set("updated_token", body.token);
