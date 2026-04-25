["new_user_login", "new_user_password"].forEach((name) => {
    const value = pm.collectionVariables.get(name);
    pm.expect(value, `${name} must exist before login`).to.be.a("string").and.not.empty;
});
