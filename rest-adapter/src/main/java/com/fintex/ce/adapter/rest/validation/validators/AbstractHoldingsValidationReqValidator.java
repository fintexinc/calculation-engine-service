package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.CalculationCommand;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractHoldingsValidationReqValidator<T> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, List<PortfolioHolding>> holdingsAccessor;
  private final Consumer<List<PortfolioHolding>> validationAction;

  protected AbstractHoldingsValidationReqValidator(
      Class<T> carrierType,
      Function<T, List<PortfolioHolding>> holdingsAccessor,
      Consumer<List<PortfolioHolding>> validationAction) {
    this.carrierType = carrierType;
    this.holdingsAccessor = holdingsAccessor;
    this.validationAction = validationAction;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!carrierType.isInstance(command)) {
      return;
    }
    validationAction.accept(holdingsAccessor.apply(carrierType.cast(command)));
  }
}
