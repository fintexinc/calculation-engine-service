package com.fintex.ce.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@ConfigurationProperties(prefix = "sms.runner")
@Setter
public class SecurityMasterServiceRunner implements SmartLifecycle {

  private static final Logger log = LoggerFactory.getLogger(SecurityMasterServiceRunner.class);

  private String path = "../security-master-service-v2";
  private String envFile = "environment-v2/.env";
  private String overrideEnvFile = "ce-environment/.env";
  private boolean enabled = true;

  private Process process;
  private boolean running;

  @Override
  public void start() {
    if (!enabled) {
      log.info("SMS runner is disabled");
      return;
    }

    File smsDir = new File(path).getAbsoluteFile();
    if (!new File(smsDir, "pom.xml").exists()) {
      log.warn("security-master-service-v2 not found at {}, skipping", smsDir);
      return;
    }

    Path envPath = smsDir.toPath().resolve(envFile);
    if (!Files.exists(envPath)) {
      log.warn("Environment file not found at {}, skipping SMS runner", envPath);
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
          ? new String[] {"cmd.exe", "/c", "mvn", "spring-boot:run", "-pl", "bootstrap", "-Dspring-boot.run.profiles=dev"}
          : new String[] {"sh", "-c", "mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=dev"};
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(smsDir);
      pb.environment().putAll(envVars);
      pb.inheritIO();

      process = pb.start();
      running = true;
      log.info("Started security-master-service-v2 (PID: {})", process.pid());
    } catch (IOException e) {
      log.error("Failed to start security-master-service-v2", e);
    }
  }

  @Override
  public void stop() {
    if (process != null && process.isAlive()) {
      log.info("Stopping security-master-service-v2 (PID: {})...", process.pid());
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
          .filter(line -> !line.isEmpty() && !line.startsWith("#") && line.contains("="))
          .collect(Collectors.toMap(
              line -> line.substring(0, line.indexOf('=')),
              line -> line.substring(line.indexOf('=') + 1),
              (existing, replacement) -> replacement,
              HashMap::new));
    }
  }
}
