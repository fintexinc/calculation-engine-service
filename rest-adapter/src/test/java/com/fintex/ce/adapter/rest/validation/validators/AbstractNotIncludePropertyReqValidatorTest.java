package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.exceptions.ValidationException;

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
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo(expectedErrorCode());
        });
  }

  @Test
  void shouldNotThrow_whenPropertyIsNull() {
    CalculationCommand command = createCommandWithoutProperty();

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }
}
