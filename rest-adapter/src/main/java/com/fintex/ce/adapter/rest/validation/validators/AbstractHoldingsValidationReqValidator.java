package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractHoldingsValidationReqValidator<T> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, List<PortfolioHolding>> holdingsAccessor;
  private final Consumer<List<PortfolioHolding>> validationAction;
  private final String fieldName;
  private final boolean required;

  protected AbstractHoldingsValidationReqValidator(
      Class<T> carrierType,
      Function<T, List<PortfolioHolding>> holdingsAccessor,
      Consumer<List<PortfolioHolding>> validationAction) {
    this(carrierType, holdingsAccessor, validationAction, null, false);
  }

  protected AbstractHoldingsValidationReqValidator(
      Class<T> carrierType,
      Function<T, List<PortfolioHolding>> holdingsAccessor,
      Consumer<List<PortfolioHolding>> validationAction,
      String fieldName,
      boolean required) {
    this.carrierType = carrierType;
    this.holdingsAccessor = holdingsAccessor;
    this.validationAction = validationAction;
    this.fieldName = fieldName;
    this.required = required;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!carrierType.isInstance(command)) {
      return;
    }
    List<PortfolioHolding> holdings = holdingsAccessor.apply(carrierType.cast(command));
    if (required && CollectionUtils.isEmpty(holdings)) {
      throw ErrorCode.FIELD_NOT_EMPTY.toValidationExceptionForField(fieldName, fieldName);
    }
    validationAction.accept(holdings);
  }
}
