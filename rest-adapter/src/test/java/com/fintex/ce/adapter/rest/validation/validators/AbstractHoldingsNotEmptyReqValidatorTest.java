package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class AbstractHoldingsNotEmptyReqValidatorTest {

  abstract RequestValidator createValidator();

  abstract CalculationCommand createCommandWithEmptyList();

  abstract CalculationCommand createCommandWithNullList();

  abstract CalculationCommand createCommandWithNonEmptyList();

  abstract String expectedMessage();

  @Test
  void shouldThrow_whenListIsEmpty() {
    CalculationCommand command = createCommandWithEmptyList();

    assertThatThrownBy(() -> createValidator().validate(command))
        .isInstanceOf(ReqValidationException.class)
        .hasMessage(expectedMessage());
  }

  @Test
  void shouldThrow_whenListIsNull() {
    CalculationCommand command = createCommandWithNullList();

    assertThatThrownBy(() -> createValidator().validate(command))
        .isInstanceOf(ReqValidationException.class)
        .hasMessage(expectedMessage());
  }

  @Test
  void shouldNotThrow_whenListIsNotEmpty() {
    CalculationCommand command = createCommandWithNonEmptyList();

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }
}
