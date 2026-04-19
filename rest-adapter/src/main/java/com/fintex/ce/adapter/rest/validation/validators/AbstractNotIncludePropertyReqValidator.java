package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.ErrorCode;

import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * Base validator that rejects commands whose accessor returns a non-null value. Subclasses supply the carrier type, the
 * property accessor, and the {@link ErrorCode} to throw.
 */
public abstract class AbstractNotIncludePropertyReqValidator<T, V> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, V> accessor;
  private final ErrorCode errorCode;

  protected AbstractNotIncludePropertyReqValidator(Class<T> carrierType,
      Function<T, V> accessor,
      ErrorCode errorCode) {
    this.carrierType = carrierType;
    this.accessor = accessor;
    this.errorCode = errorCode;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!carrierType.isInstance(command)) {
      return;
    }
    if (nonNull(accessor.apply(carrierType.cast(command)))) {
      throw errorCode.toValidationException();
    }
  }
}
