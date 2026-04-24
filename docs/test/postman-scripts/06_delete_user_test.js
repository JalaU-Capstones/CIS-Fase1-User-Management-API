pm.test("Delete user returns HTTP 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Delete user responds in under 500 ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

const version = pm.collectionVariables.get("active_api_version") || pm.variables.get("api_version");
const responseText = pm.response.text();
const expectedMessage = version === "v2"
    ? "User has been successfully deleted from MongoDB."
    : "User and all related topics, ideas, and votes have been successfully deleted.";

pm.test("Delete user returns the expected version-specific message", function () {
    pm.expect(responseText).to.eql(expectedMessage);
});

pm.collectionVariables.set("last_deleted_user_id", pm.collectionVariables.get("new_user_id"));
