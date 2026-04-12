package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.model.holding.Holding;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractHoldingsValidationReqValidator<T> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, List<Holding>> holdingsAccessor;
  private final Consumer<List<Holding>> validationAction;

  protected AbstractHoldingsValidationReqValidator(
      Class<T> carrierType,
      Function<T, List<Holding>> holdingsAccessor,
      Consumer<List<Holding>> validationAction) {
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
