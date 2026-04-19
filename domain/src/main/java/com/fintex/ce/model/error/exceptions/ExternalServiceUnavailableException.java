package com.fintex.ce.model.error.exceptions;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;

import java.io.Serial;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Thrown by web-client adapters when a downstream external service (Security Master, Bank of Canada, etc.) is
 * unreachable, times out, or responds with an error status. Maps to {@link ErrorCode#EXTERNAL_SERVICE_UNAVAILABLE}
 * (HTTP 503) at the REST boundary.
 */
@EqualsAndHashCode(callSuper = true)
public class ExternalServiceUnavailableException extends BasePceException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ExternalServiceUnavailableException(String serviceName) {
    super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, serviceName);
  }

  public ExternalServiceUnavailableException(String serviceName, Throwable cause) {
    super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, serviceName);
    initCause(cause);
  }

  public ExternalServiceUnavailableException(String serviceName, Map<String, Object> metadata) {
    super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, serviceName);
    if (metadata != null) {
      metadata.forEach(this::withMetadata);
    }
  }

  @Override
  public ExternalServiceUnavailableException withId(String id) {
    super.withId(id);
    return this;
  }

  @Override
  public ExternalServiceUnavailableException withFieldName(String fieldName) {
    super.withFieldName(fieldName);
    return this;
  }

  @Override
  public ExternalServiceUnavailableException withMetadata(String key, Object value) {
    super.withMetadata(key, value);
    return this;
  }

  @Override
  public ExternalServiceUnavailableException withHolding(PortfolioHolding holding) {
    super.withHolding(holding);
    return this;
  }
}
