package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Function;

/**
 * Base validator that fails when the list of holdings extracted from the command is null or empty. Subclasses supply
 * the carrier type (e.g. {@link com.fintex.ce.model.dto.command.HoldingsProvider} or
 * {@link com.fintex.ce.model.dto.command.BenchmarkHoldingsProvider}), the accessor, and the error message.
 */
public abstract class AbstractHoldingsNotEmptyReqValidator<T> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, List<Holding>> accessor;
  private final String errorMessage;

  protected AbstractHoldingsNotEmptyReqValidator(Class<T> carrierType,
      Function<T, List<Holding>> accessor,
      String errorMessage) {
    this.carrierType = carrierType;
    this.accessor = accessor;
    this.errorMessage = errorMessage;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!carrierType.isInstance(command)) {
      return;
    }
    if (CollectionUtils.isEmpty(accessor.apply(carrierType.cast(command)))) {
      throw new ReqValidationException(errorMessage);
    }
  }
}
