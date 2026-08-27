package ca.tangerine.pce.rest.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * Settings for {@link RequestLoggingFilter}. Payload, header and query-string logging default to off; override per
 * environment to opt in.
 *
 * <p>
 * {@code excludedPaths} is matched as a prefix and as a substring, and covers {@code /actuator} as a whole rather than
 * the individual endpoints. Which management endpoints are reachable is decided by
 * {@code management.endpoints.web.exposure.include}, and all of them are infrastructure traffic: naming them one by one
 * here means every endpoint added later silently starts being buffered and logged as if a client had asked for a
 * calculation.
 */
@Data
@Component
@ConfigurationProperties(prefix = "logging.http")
public class HttpLoggingProperties {

  private boolean enabled = true;

  private boolean includeHeaders = false;
  private boolean includeQueryString = false;
  private boolean includePayload = false;

  private int maxPayloadLength = 10000;

  /** Hard upper bound on the request body that may be buffered for logging (in bytes). */
  private int bodyLoggingMaxBufferBytes = 1_048_576;

  private Set<String> sensitiveHeaders = Set.of(
      "authorization",
      "cookie",
      "set-cookie",
      "x-api-key",
      "x-auth-token",
      "x-csrf-token");

  private Set<String> sensitiveParameters = Set.of(
      "password",
      "token",
      "apikey",
      "secret",
      "access_token",
      "refresh_token");

  private Set<String> sensitiveJsonFields = Set.of(
      "password",
      "token",
      "apiKey",
      "accessToken",
      "refreshToken",
      "clientSecret",
      "ssn",
      "creditCard",
      "cardNumber",
      "cvv",
      "pin",
      "accountNumber");

  private List<String> sensitivePatterns = List.of(
      "\\b\\d{3}-\\d{2}-\\d{4}\\b",
      "\\b\\d{9}\\b",
      "\\b\\d{16}\\b",
      "\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b");

  /** Email regex applied separately, only when {@link #maskEmails} is true. */
  private String emailPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";

  private boolean maskEmails = false;

  private Set<String> excludedPaths = Set.of(
      "/actuator",
      "/swagger-ui",
      "/api-docs");

  private String maskValue = "[MASKED]";
}
