package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;

import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public abstract class AbstractPeriodsLessThan12ReqValidator<T> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, Set<String>> periodsAccessor;
  private final ErrorCode errorCode;

  protected AbstractPeriodsLessThan12ReqValidator(
      Class<T> carrierType,
      Function<T, Set<String>> periodsAccessor,
      ErrorCode errorCode) {
    this.carrierType = carrierType;
    this.periodsAccessor = periodsAccessor;
    this.errorCode = errorCode;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!carrierType.isInstance(command)) {
      return;
    }
    Set<String> periods = periodsAccessor.apply(carrierType.cast(command));
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    for (String period : periods) {
      if (isNumeric(period) && Long.parseLong(period) < 12) {
        throw errorCode.reqValidationError();
      }
    }
  }
}
