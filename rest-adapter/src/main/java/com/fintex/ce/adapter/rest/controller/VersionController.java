package com.fintex.ce.adapter.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import lombok.extern.slf4j.Slf4j;

/**
 * REST endpoint for service version information. Provides metadata about the service including the artifact ID,
 * version, and JVM uptime.
 */
@Slf4j
@RestController
@RequestMapping("/version")
@Tag(name = "Service Information", description = "Endpoints for retrieving service metadata and health information")
public class VersionController {

  private static final String ARTIFACT_ID = "ce";
  private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();

  @Autowired(required = false)
  private BuildProperties buildProperties;

  @Operation(summary = "Get service version information", description = """
      Returns version information about the service including the artifact ID, version number,
      and the number of whole seconds the JVM has been running.
      """)
  @ApiResponse(responseCode = "200", description = "Service version information", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VersionResponse.class)))
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public VersionResponse getVersion() {
    String version = resolveVersion();
    long uptimeSeconds = RUNTIME_MX_BEAN.getUptime() / 1000;

    return VersionResponse.builder()
        .name(ARTIFACT_ID)
        .version(version)
        .uptimeSeconds(uptimeSeconds)
        .build();
  }

  /**
   * Resolves the service version from Spring Boot's BuildProperties, which reads from META-INF/build-info.properties.
   * Falls back to the package manifest's Implementation-Version, and finally to "unknown" if the version cannot be
   * determined.
   */
  private String resolveVersion() {
    try {
      if (buildProperties != null) {
        return buildProperties.getVersion();
      }
    } catch (Exception e) {
      log.debug("Could not resolve version from BuildProperties", e);
    }

    Package pkg = Package.getPackage("com.fintex.ce.adapter.rest.controller");
    if (pkg != null && pkg.getImplementationVersion() != null) {
      return pkg.getImplementationVersion();
    }
    return "unknown";
  }
}
