package com.cis.api.console;

import com.cis.api.config.SystemStateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ConsoleCommandListenerTest {

    private SystemStateConfig mockSystemStateConfig;
    private ByteArrayOutputStream testOut;
    private PrintStream printStream;

    @BeforeEach
    void setUp() {
        mockSystemStateConfig = mock(SystemStateConfig.class);
        testOut = new ByteArrayOutputStream();
        printStream = new PrintStream(testOut);
    }

    private ConsoleCommandListener createListener(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        return new ConsoleCommandListener(mockSystemStateConfig, inputStream, printStream);
    }

    @Test
    void testMaintenanceOnCommand() {
        ConsoleCommandListener listener = createListener("maintenance on\nexit\n");
        listener.run();

        verify(mockSystemStateConfig).setMigrationRunning(true);
        assertTrue(testOut.toString().contains("Maintenance mode enabled."));
    }

    @Test
    void testMaintenanceOffCommand() {
        ConsoleCommandListener listener = createListener("maintenance off\nexit\n");
        listener.run();

        verify(mockSystemStateConfig).setMigrationRunning(false);
        assertTrue(testOut.toString().contains("Maintenance mode disabled."));
    }

    @Test
    void testSunsetOnCommand() {
        ConsoleCommandListener listener = createListener("sunset on\nexit\n");
        listener.run();

        verify(mockSystemStateConfig).setV1Sunset(true);
        assertTrue(testOut.toString().contains("V1 sunset mode enabled."));
    }

    @Test
    void testSunsetOffCommand() {
        ConsoleCommandListener listener = createListener("sunset off\nexit\n");
        listener.run();

        verify(mockSystemStateConfig).setV1Sunset(false);
        assertTrue(testOut.toString().contains("V1 sunset mode disabled."));
    }

    @Test
    void testStatusCommand() {
        when(mockSystemStateConfig.isMigrationRunning()).thenReturn(true);
        when(mockSystemStateConfig.isV1Sunset()).thenReturn(false);

        ConsoleCommandListener listener = createListener("status\nexit\n");
        listener.run();

        assertTrue(testOut.toString().contains("Migration running: true, V1 sunset: false"));
    }

    @Test
    void testHelpCommand() {
        ConsoleCommandListener listener = createListener("help\nexit\n");
        listener.run();

        String output = testOut.toString();
        assertTrue(output.contains("Available commands:"));
        assertTrue(output.contains("maintenance [on|off]"));
        assertTrue(output.contains("sunset [on|off]"));
    }

    @Test
    void testUnknownCommand() {
        ConsoleCommandListener listener = createListener("unknown\nexit\n");
        listener.run();

        assertTrue(testOut.toString().contains("Unknown command: unknown"));
    }

    @Test
    void testExitCommand() {
        ConsoleCommandListener listener = createListener("exit\n");
        listener.run();

        assertTrue(testOut.toString().contains("Shutting down console listener..."));
    }

    @Test
    void testQuitCommand() {
        ConsoleCommandListener listener = createListener("quit\n");
        listener.run();

        assertTrue(testOut.toString().contains("Shutting down console listener..."));
    }

    @Test
    void testEmptyCommand() {
        ConsoleCommandListener listener = createListener("\nexit\n");
        listener.run();
        // Should not crash, just show prompt again
    }

    @Test
    void testMaintenanceNoArg() {
        ConsoleCommandListener listener = createListener("maintenance\nexit\n");
        listener.run();
        assertTrue(testOut.toString().contains("Usage: maintenance [on|off]"));
    }

    @Test
    void testSunsetNoArg() {
        ConsoleCommandListener listener = createListener("sunset\nexit\n");
        listener.run();
        assertTrue(testOut.toString().contains("Usage: sunset [on|off]"));
    }

    @Test
    void testStartWhenEnabled() {
        System.setProperty("console.input.disabled", "false");
        try {
            ConsoleCommandListener listener = createListener("exit\n");
            listener.start();
            // Just verifying it starts without exception
            listener.stop();
        } finally {
            System.clearProperty("console.input.disabled");
        }
    }

    @Test
    void testStopMethod() {
        ConsoleCommandListener listener = createListener("status\nstatus\nstatus\n");
        listener.stop();
        listener.run();
        // Should stop immediately and not process status
        assertTrue(!testOut.toString().contains("Migration running"));
    }
}
