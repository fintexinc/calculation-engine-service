package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.exception.ReqValidationException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class AbstractNotIncludePropertyReqValidatorTest {

  abstract RequestValidator createValidator();

  abstract CalculationCommand createCommandWithProperty();

  abstract CalculationCommand createCommandWithoutProperty();

  abstract String expectedErrorCode();

  @Test
  void shouldThrow_whenPropertyIsPresent() {
    CalculationCommand command = createCommandWithProperty();

    assertThatThrownBy(() -> createValidator().validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo(expectedErrorCode());
        });
  }

  @Test
  void shouldNotThrow_whenPropertyIsNull() {
    CalculationCommand command = createCommandWithoutProperty();

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }
}
