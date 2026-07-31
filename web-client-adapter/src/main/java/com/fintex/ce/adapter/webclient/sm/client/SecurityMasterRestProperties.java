package com.fintex.ce.adapter.webclient.sm.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "external-services.security-master.rest")
public class SecurityMasterRestProperties {

  private static final int BYTES_PER_MEGABYTE = 1024 * 1024;

  /** Default ceiling, in bytes, for a buffered Security Master response body. See {@link #maxInMemorySize}. */
  private static final int DEFAULT_MAX_IN_MEMORY_SIZE = 32 * BYTES_PER_MEGABYTE;

  private String baseUrl;
  private int timeout = 90000;
  private boolean logBody = false;
  private boolean logRequests = false;
  private String healthCheckPath;

  /**
   * Ceiling, in bytes, on a buffered Security Master response body — WebFlux's {@code maxInMemorySize} codec limit,
   * whose 256 KB default is far too small once holdings are looked through to leaf instruments and a whole portfolio is
   * fetched at once. Exceeding it surfaces as a {@code DataBufferLimitException} reported as the service being
   * unavailable. <b>Coupled to Security Master's {@code max-holdings-percentage-per-security}</b> — raise both
   * together.
   */
  private int maxInMemorySize = DEFAULT_MAX_IN_MEMORY_SIZE;
}
