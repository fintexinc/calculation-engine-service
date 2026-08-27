package ca.tangerine.pce.model.error.exceptions;

import java.io.Serial;
import java.util.Map;
import lombok.EqualsAndHashCode;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;

/**
 * Runtime exception thrown exclusively by request validators. Indicates that the incoming request body failed a
 * validation rule (invalid field, missing data, bad time interval, etc.). For runtime calculation failures, use
 * {@link CalculationException} instead.
 */
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends BasePceException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ValidationException(ErrorCode errorCode, Object... formatArgs) {
    super(errorCode, formatArgs);
  }

  public ValidationException(ErrorCode errorCode, Map<String, Object> parameters) {
    super(errorCode, parameters);
  }

  @Override
  public ValidationException withId(String id) {
    super.withId(id);
    return this;
  }

  @Override
  public ValidationException withFieldName(String fieldName) {
    super.withFieldName(fieldName);
    return this;
  }

  @Override
  public ValidationException withMetadata(String key, Object value) {
    super.withMetadata(key, value);
    return this;
  }

  @Override
  public ValidationException withHolding(PortfolioHolding holding) {
    super.withHolding(holding);
    return this;
  }
}
