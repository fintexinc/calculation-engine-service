package com.fintex.ce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("dev")
@Component
@ConfigurationProperties(prefix = "mic.runner")
@Setter
public class MarketInvestmentCatalogueServiceRunner implements SmartLifecycle {

  // The sibling repository is still named security-master-service-v2 on disk; this is a path, not a name
  // the product chose, so it does not follow the service rename.
  private String path = "../security-master-service-v2";
  private String envFile = "environment-v2/.env";
  private String overrideEnvFile = "ce-environment/.env";
  private boolean enabled = true;

  private Process process;
  private boolean running;

  @Override
  public void start() {
    if (!enabled) {
      log.info("MIC runner is disabled");
      return;
    }

    File micDir = new File(path).getAbsoluteFile();
    if (!new File(micDir, "pom.xml").exists()) {
      log.warn("Market Investment Catalogue not found at {}, skipping", micDir);
      return;
    }

    Path envPath = micDir.toPath().resolve(envFile);
    if (!Files.exists(envPath)) {
      log.warn("Environment file not found at {}, skipping MIC runner", envPath);
      return;
    }

    try {
      Map<String, String> envVars = loadEnvFile(envPath);

      Path overridePath = new File(".").getAbsoluteFile().toPath().resolve(overrideEnvFile);
      if (Files.exists(overridePath)) {
        Map<String, String> overrides = loadEnvFile(overridePath);
        envVars.putAll(overrides);
        log.info("Applied {} env overrides from {}", overrides.size(), overridePath);
      }

      boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");
      String[] command = isWindows
          ? new String[] {"cmd.exe", "/c", "mvn", "spring-boot:run", "-pl", "bootstrap",
              "-Dspring-boot.run.profiles=dev"}
          : new String[] {"sh", "-c", "mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=dev"};
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(micDir);
      pb.environment().putAll(envVars);
      pb.inheritIO();

      process = pb.start();
      running = true;
      log.info("Started the Market Investment Catalogue (PID: {})", process.pid());
    } catch (IOException e) {
      log.error("Failed to start the Market Investment Catalogue", e);
    }
  }

  @Override
  public void stop() {
    if (process != null && process.isAlive()) {
      log.info("Stopping the Market Investment Catalogue (PID: {})...", process.pid());
      process.descendants().forEach(ProcessHandle::destroyForcibly);
      process.destroyForcibly();
    }
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return Integer.MIN_VALUE;
  }

  private Map<String, String> loadEnvFile(Path path) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(path)) {
      return reader.lines()
          .map(String::trim)
          .filter(line -> !line.startsWith("#") && line.contains("="))
          .collect(Collectors.toMap(
              line -> line.substring(0, line.indexOf('=')),
              line -> line.substring(line.indexOf('=') + 1),
              (existing, replacement) -> replacement,
              HashMap::new));
    }
  }
}
