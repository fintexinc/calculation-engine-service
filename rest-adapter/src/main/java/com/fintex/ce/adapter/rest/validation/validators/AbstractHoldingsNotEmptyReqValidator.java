package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.Holding;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Function;

/**
 * Base validator that fails when the list of holdings extracted from the command is null or empty. Subclasses supply
 * the carrier type (e.g. {@link com.fintex.ce.domain.dto.command.HoldingsProvider} or
 * {@link com.fintex.ce.domain.dto.command.BenchmarkHoldingsProvider}), the accessor, and the error message.
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
