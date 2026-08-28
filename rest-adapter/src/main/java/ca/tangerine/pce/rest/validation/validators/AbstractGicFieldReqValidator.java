package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.contract.HoldingsProvider;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.rest.validation.RequestValidator;

import java.util.List;
import java.util.function.Function;

import static java.util.Objects.isNull;

/**
 * Base validator that ensures a required field on every {@link GicHolding} in the command's holdings list is non-null.
 * Subclasses supply the accessor and the {@link ErrorCode} raised when a value is missing.
 */
public abstract class AbstractGicFieldReqValidator<V> implements RequestValidator {

  private final Function<GicHolding, V> fieldAccessor;
  private final ErrorCode errorCode;

  protected AbstractGicFieldReqValidator(Function<GicHolding, V> fieldAccessor, ErrorCode errorCode) {
    this.fieldAccessor = fieldAccessor;
    this.errorCode = errorCode;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!(command instanceof HoldingsProvider hp)) {
      return;
    }
    List<PortfolioHolding> holdings = hp.getHoldings();
    if (holdings == null || holdings.isEmpty()) {
      return;
    }
    for (PortfolioHolding holding : holdings) {
      if (holding instanceof GicHolding gic && isNull(fieldAccessor.apply(gic))) {
        throw errorCode.toValidationExceptionForHolding(gic);
      }
    }
  }
}
