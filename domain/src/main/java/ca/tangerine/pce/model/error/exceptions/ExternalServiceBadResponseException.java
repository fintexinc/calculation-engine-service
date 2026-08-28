package ca.tangerine.pce.model.error.exceptions;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;

import java.io.Serial;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Thrown by web-client adapters when a downstream external service (Market Investment Catalogue, Bank of Canada, etc.)
 * responds with a 4xx client-error status, indicating that the outbound request was malformed, unauthorized or
 * unacceptable. Maps to {@link ErrorCode#EXTERNAL_SERVICE_BAD_RESPONSE} (HTTP 502 Bad Gateway) at the REST boundary.
 */
@EqualsAndHashCode(callSuper = true)
public class ExternalServiceBadResponseException extends BasePceException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ExternalServiceBadResponseException(String serviceName) {
    super(ErrorCode.EXTERNAL_SERVICE_BAD_RESPONSE, serviceName);
  }

  public ExternalServiceBadResponseException(String serviceName, Throwable cause) {
    super(ErrorCode.EXTERNAL_SERVICE_BAD_RESPONSE, serviceName);
    initCause(cause);
  }

  public ExternalServiceBadResponseException(String serviceName, Map<String, Object> metadata) {
    super(ErrorCode.EXTERNAL_SERVICE_BAD_RESPONSE, serviceName);
    if (metadata != null) {
      metadata.forEach(this::withMetadata);
    }
  }

  @Override
  public ExternalServiceBadResponseException withId(String id) {
    super.withId(id);
    return this;
  }

  @Override
  public ExternalServiceBadResponseException withFieldName(String fieldName) {
    super.withFieldName(fieldName);
    return this;
  }

  @Override
  public ExternalServiceBadResponseException withMetadata(String key, Object value) {
    super.withMetadata(key, value);
    return this;
  }

  @Override
  public ExternalServiceBadResponseException withHolding(PortfolioHolding holding) {
    super.withHolding(holding);
    return this;
  }
}
