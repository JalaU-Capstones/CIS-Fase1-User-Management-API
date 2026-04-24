pm.test("v1 get user by id responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("v1 get user by id returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

if (pm.response.code === 200) {
    const body = pm.response.json();

    pm.test("v1 get user by id returns valid user schema", function () {
        pm.expect(body).to.be.an("object");
        pm.expect(body.id).to.eql(pm.collectionVariables.get("v1_seed_user_id"));
        pm.expect(body.name).to.be.a("string").and.not.empty;
        pm.expect(body.login).to.be.a("string").and.not.empty;
    });
} else {
    const body = pm.response.json();

    pm.test("v1 get user by id may return 404 for a missing user", function () {
        pm.expect(pm.response.code).to.eql(404);
        pm.expect(body.status).to.eql(404);
        pm.expect(body.error).to.eql("Not Found");
        pm.expect(body.message).to.be.a("string");
    });
}
