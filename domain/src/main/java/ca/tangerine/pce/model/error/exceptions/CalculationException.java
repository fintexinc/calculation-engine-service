package ca.tangerine.pce.model.error.exceptions;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;

import java.io.Serial;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Runtime exception thrown by calculation services and related components when a calculation step fails due to missing
 * or inconsistent data, fx-rate gaps, or downstream fetcher errors. For user-input validation failures use
 * {@link ValidationException} instead.
 */
@EqualsAndHashCode(callSuper = true)
public class CalculationException extends BasePceException {

  @Serial
  private static final long serialVersionUID = 1L;

  public CalculationException(ErrorCode errorCode, Object... formatArgs) {
    super(errorCode, formatArgs);
  }

  public CalculationException(ErrorCode errorCode, Map<String, Object> parameters) {
    super(errorCode, parameters);
  }

  @Override
  public CalculationException withId(String id) {
    super.withId(id);
    return this;
  }

  @Override
  public CalculationException withFieldName(String fieldName) {
    super.withFieldName(fieldName);
    return this;
  }

  @Override
  public CalculationException withMetadata(String key, Object value) {
    super.withMetadata(key, value);
    return this;
  }

  @Override
  public CalculationException withHolding(PortfolioHolding holding) {
    super.withHolding(holding);
    return this;
  }
}
