package com.fintex.ce.adapter.rest.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * Response containing version information about the service.
 */
@Data
@Builder
public class VersionResponse {

  /**
   * The service artifact ID.
   */
  @JsonProperty("name")
  private String name;

  /**
   * The version from the Maven build.
   */
  @JsonProperty("version")
  private String version;

  /**
   * Whole seconds since the JVM started.
   */
  @JsonProperty("uptimeSeconds")
  private long uptimeSeconds;
}
