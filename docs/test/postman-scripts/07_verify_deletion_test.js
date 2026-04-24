pm.test("Verify deletion returns HTTP 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Verify deletion responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Verify deletion returns JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const body = pm.response.json();
const deletedId = pm.collectionVariables.get("last_deleted_user_id") || pm.collectionVariables.get("new_user_id");

pm.test("Verify deletion returns the expected error schema", function () {
    pm.expect(body).to.be.an("object");
    pm.expect(body.status).to.eql(404);
    pm.expect(body.error).to.eql("Not Found");
    pm.expect(body.message).to.be.a("string");
});

pm.test("Verify deletion error references the deleted user ID", function () {
    pm.expect(body.message).to.include(deletedId);
});
