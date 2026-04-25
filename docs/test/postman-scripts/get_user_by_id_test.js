const apiVersion = pm.collectionVariables.get("api_version") || pm.environment.get("api_version") || "v1";
pm.collectionVariables.set("api_version", apiVersion);

pm.test("Get user by ID responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Get user by ID returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

if (pm.response.code === 200) {
    const body = pm.response.json();

    pm.test("Get user by ID returns HTTP 200 for an existing ID", function () {
        pm.response.to.have.status(200);
    });

    pm.test("Get user by ID returns valid user schema", function () {
        pm.expect(body).to.be.an("object");
        pm.expect(body.id).to.eql(pm.collectionVariables.get("existing_user_id"));
        pm.expect(body.name).to.be.a("string").and.not.empty;
        pm.expect(body.login).to.be.a("string").and.not.empty;
    });
} else {
    const body = pm.response.json();

    pm.test("Get user by ID may return HTTP 404 for a non-existent ID", function () {
        pm.expect(pm.response.code).to.eql(404);
    });

    pm.test("404 response matches error schema", function () {
        pm.expect(body).to.be.an("object");
        pm.expect(body.status).to.eql(404);
        pm.expect(body.error).to.eql("Not Found");
        pm.expect(body.message).to.be.a("string");
    });
}
