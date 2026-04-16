package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.HoldingsProvider;
import com.fintex.ce.model.error.ErrorCode;

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
    List<Holding> holdings = hp.getHoldings();
    if (holdings == null || holdings.isEmpty()) {
      return;
    }
    for (Holding holding : holdings) {
      if (holding instanceof GicHolding gic && isNull(fieldAccessor.apply(gic))) {
        throw errorCode.reqValidationError();
      }
    }
  }
}
