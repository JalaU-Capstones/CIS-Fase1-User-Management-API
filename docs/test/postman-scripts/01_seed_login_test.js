pm.test("Seed login returns HTTP 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Seed login responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Seed login returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const body = pm.response.json();

pm.test("Seed login response has valid auth schema", function () {
    pm.expect(body).to.be.an("object");
    pm.expect(body.token).to.be.a("string").and.not.empty;
    if (Object.prototype.hasOwnProperty.call(body, "message") && body.message !== null) {
        pm.expect(body.message).to.be.a("string");
    }
});

pm.collectionVariables.set("seed_token", body.token);
