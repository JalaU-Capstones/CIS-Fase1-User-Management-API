package com.cis.api.migration;

import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMigrationRunnerTest {

    @Test
    void run_WithDryRunAndCleanFlags_ShouldSkipPromptAndExitZero() throws Exception {
        Path callFile = Files.createTempFile("runner-call", ".txt");

        ProcessResult result = runHarness("dry-run", callFile, null, "--dry-run", "--clean");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).doesNotContain("Do you want to continue?");
        assertThat(Files.readString(callFile)).isEqualTo("true,true");
    }

    @Test
    void run_WhenUserDeclinesConfirmation_ShouldExitWithoutMigrating() throws Exception {
        Path callFile = Files.createTempFile("runner-call", ".txt");
        Files.delete(callFile);

        ProcessResult result = runHarness("success", callFile, "no\n");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("Do you want to continue?");
        assertThat(Files.exists(callFile)).isFalse();
    }

    @Test
    void run_WhenConfirmedAndMigrationSucceeds_ShouldExitZero() throws Exception {
        Path callFile = Files.createTempFile("runner-call", ".txt");

        ProcessResult result = runHarness("success", callFile, "yes\n");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("Migration completed successfully!");
        assertThat(Files.readString(callFile)).isEqualTo("false,false");
    }

    @Test
    void run_WithYesFlagAndErrors_ShouldExitOne() throws Exception {
        Path callFile = Files.createTempFile("runner-call", ".txt");

        ProcessResult result = runHarness("errors", callFile, null, "--clean", "--yes");

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.output()).contains("Migration failed!");
        assertThat(Files.readString(callFile)).isEqualTo("false,true");
    }

    @Test
    void run_WithYesFlagAndOnlySkippedUsers_ShouldExitZero() throws Exception {
        Path callFile = Files.createTempFile("runner-call", ".txt");

        ProcessResult result = runHarness("skipped", callFile, null, "--yes");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output())
                .doesNotContain("Do you want to continue?")
                .contains("Migration skipped - users already exist in MongoDB");
        assertThat(Files.readString(callFile)).isEqualTo("false,false");
    }

    @Test
    void run_WithYesFlagAndNoUsersFound_ShouldExitZero() throws Exception {
        Path callFile = Files.createTempFile("runner-call", ".txt");

        ProcessResult result = runHarness("empty", callFile, null, "--yes");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("No users found in MySQL to migrate.");
        assertThat(Files.readString(callFile)).isEqualTo("false,false");
    }

    private ProcessResult runHarness(String scenario, Path callFile, String stdin, String... runnerArgs) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + "/bin/java");
        command.add("-javaagent:" + jacocoAgentPath() + "=destfile=target/jacoco.exec,append=true");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add("-Dharness.call.file=" + callFile.toAbsolutePath());
        command.add(UserMigrationRunnerHarness.class.getName());
        command.add(scenario);
        command.addAll(List.of(runnerArgs));

        Process process = new ProcessBuilder(command)
                .directory(Path.of(System.getProperty("user.dir")).toFile())
                .redirectErrorStream(true)
                .start();

        if (stdin != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(stdin);
            }
        } else {
            process.getOutputStream().close();
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        return new ProcessResult(exitCode, output);
    }

    private String jacocoAgentPath() {
        return Path.of(
                System.getProperty("user.home"),
                ".m2",
                "repository",
                "org",
                "jacoco",
                "org.jacoco.agent",
                "0.8.12",
                "org.jacoco.agent-0.8.12-runtime.jar"
        ).toString();
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
