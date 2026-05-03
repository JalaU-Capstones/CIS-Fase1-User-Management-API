package com.cis.api.console;

import com.cis.api.config.SystemStateConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ConsoleCommandListener implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConsoleCommandListener.class);
    private static final String PROMPT = "> ";

    private final SystemStateConfig systemStateConfig;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final InputStream inputStream;
    private final PrintStream outputStream;

    @Autowired
    public ConsoleCommandListener(SystemStateConfig systemStateConfig) {
        this(systemStateConfig, System.in, System.out);
    }

    public ConsoleCommandListener(SystemStateConfig systemStateConfig, InputStream inputStream, PrintStream outputStream) {
        this.systemStateConfig = systemStateConfig;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void start() {
        String disabled = System.getProperty("console.input.disabled");
        if ("true".equalsIgnoreCase(disabled)) {
            log.info("Console command listener is disabled via system property.");
            return;
        }

        Thread consoleThread = new Thread(this, "console-command-listener");
        consoleThread.setDaemon(true);
        consoleThread.start();
        log.info("Console command listener started. Type 'help' for commands.");
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(inputStream)) {
            while (running.get()) {
                outputStream.print(PROMPT);
                if (!scanner.hasNextLine()) {
                    log.info("Console input stream closed. Stopping console listener.");
                    break;
                }
                String line = scanner.nextLine();
                processCommand(line.trim());
            }
        } catch (Exception e) {
            log.error("Error reading from console input: {}", e.getMessage());
        } finally {
            running.set(false);
            log.info("Console command listener stopped.");
        }
    }

    public void stop() {
        running.set(false);
    }

    private void processCommand(String commandLine) {
        if (commandLine.isEmpty()) {
            return;
        }

        String[] parts = commandLine.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].toLowerCase() : null;

        switch (command) {
            case "maintenance":
                handleMaintenanceCommand(arg);
                break;
            case "sunset":
                handleSunsetCommand(arg);
                break;
            case "status":
                printStatus();
                break;
            case "help":
                printHelp();
                break;
            case "exit":
            case "quit":
                outputStream.println("Shutting down console listener...");
                stop();
                break;
            default:
                outputStream.println("Unknown command: " + command + ". Type 'help' for commands.");
                break;
        }
    }

    private void handleMaintenanceCommand(String arg) {
        if ("on".equals(arg)) {
            systemStateConfig.setMigrationRunning(true);
            outputStream.println("Maintenance mode enabled. Write operations on both v1 and v2 will return 503.");
        } else if ("off".equals(arg)) {
            systemStateConfig.setMigrationRunning(false);
            outputStream.println("Maintenance mode disabled. Write operations restored for v2 (and v1 if not sunset).");
        } else {
            outputStream.println("Usage: maintenance [on|off]");
        }
    }

    private void handleSunsetCommand(String arg) {
        if ("on".equals(arg)) {
            systemStateConfig.setV1Sunset(true);
            outputStream.println("V1 sunset mode enabled. v1 writes will return 410, v1 reads include warning header.");
        } else if ("off".equals(arg)) {
            systemStateConfig.setV1Sunset(false);
            outputStream.println("V1 sunset mode disabled. v1 writes and reads behave normally (unless maintenance mode is on).");
        } else {
            outputStream.println("Usage: sunset [on|off]");
        }
    }

    private void printStatus() {
        outputStream.printf("Migration running: %b, V1 sunset: %b%n",
                systemStateConfig.isMigrationRunning(),
                systemStateConfig.isV1Sunset());
    }

    private void printHelp() {
        outputStream.println("Available commands:");
        outputStream.println("  maintenance [on|off] - Enable or disable migration maintenance mode.");
        outputStream.println("  sunset [on|off]      - Enable or disable V1 sunset mode.");
        outputStream.println("  status               - Show current migration and sunset flag status.");
        outputStream.println("  help                 - Display this help message.");
        outputStream.println("  exit | quit          - Shut down the console listener.");
    }
}
