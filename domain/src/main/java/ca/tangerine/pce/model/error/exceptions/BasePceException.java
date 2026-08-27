package ca.tangerine.pce.model.error.exceptions;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.ErrorParams;

/**
 * Shared runtime exception used by the calculation engine. Carries an {@link ErrorCode}, the formatted message derived
 * from it, and optional contextual fields (holding id, request field name, metadata). Concrete subclasses:
 * {@link CalculationException} — thrown by calculation services and related components; {@link ValidationException} —
 * thrown by request validators.
 *
 * <p>
 * Each positional {@code formatArg} passed to the varargs constructor is also recorded in {@link #getMetadata()} under
 * a generated key {@code param-1}, {@code param-2}, ... so the values stay accessible downstream (e.g. in the
 * {@code Notification} body) without forcing every call site to duplicate them via
 * {@link #withMetadata(String, Object)}. Callers that already have named parameters can use the
 * {@link #BasePceException(ErrorCode, Map)} constructor instead.
 * </p>
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public abstract class BasePceException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final ErrorCode errorCode;
  private String id;
  private String fieldName;
  private final SequencedMap<String, Object> metadata = new LinkedHashMap<>();

  protected BasePceException(ErrorCode errorCode, Object... formatArgs) {
    super(errorCode.getFormattedMessage(formatArgs));
    this.errorCode = errorCode;
    ErrorParams.putParams(metadata, formatArgs);
  }

  protected BasePceException(ErrorCode errorCode, Map<String, Object> parameters) {
    super(errorCode.getFormattedMessage(parametersToArray(parameters)));
    this.errorCode = errorCode;
    if (parameters != null) {
      metadata.putAll(parameters);
    }
  }

  public BasePceException withId(String id) {
    this.id = id;
    return this;
  }

  public BasePceException withFieldName(String fieldName) {
    this.fieldName = fieldName;
    return this;
  }

  public BasePceException withMetadata(String key, Object value) {
    metadata.put(key, value);
    return this;
  }

  public BasePceException withHolding(PortfolioHolding holding) {
    this.id = ErrorParams.holdingId(holding);
    if (this.id != null) {
      metadata.put(ErrorParams.HOLDING_ID, this.id);
    }
    return this;
  }

  private static Object[] parametersToArray(Map<String, Object> parameters) {
    return parameters == null ? new Object[0] : parameters.values().toArray();
  }
}
