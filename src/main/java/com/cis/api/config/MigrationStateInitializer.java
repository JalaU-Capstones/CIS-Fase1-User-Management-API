package com.cis.api.config;

import com.cis.api.console.ConsoleCommandListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class MigrationStateInitializer implements CommandLineRunner {

    private final SystemStateConfig systemStateConfig;
    private final ConsoleCommandListener consoleCommandListener;

    public MigrationStateInitializer(SystemStateConfig systemStateConfig, ConsoleCommandListener consoleCommandListener) {
        this.systemStateConfig = systemStateConfig;
        this.consoleCommandListener = consoleCommandListener;
    }

    @Override
    public void run(String... args) {
        String maintenance = System.getProperty("migration.maintenance");
        if (maintenance != null) {
            systemStateConfig.setMigrationRunning(Boolean.parseBoolean(maintenance));
        }

        String sunset = System.getProperty("sunset.v1");
        if (sunset != null) {
            systemStateConfig.setV1Sunset(Boolean.parseBoolean(sunset));
        }

        // Start console listener thread
        consoleCommandListener.start();
    }
}
