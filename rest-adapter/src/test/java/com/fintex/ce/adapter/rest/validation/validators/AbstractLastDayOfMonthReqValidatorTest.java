package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.exceptions.ValidationException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class AbstractLastDayOfMonthReqValidatorTest {

  abstract RequestValidator createValidator();

  abstract CalculationCommand createCommandWithDate(LocalDate date);

  abstract String expectedErrorCode();

  @Test
  void shouldThrow_whenDateIsNotLastDayOfMonth() {
    CalculationCommand command = createCommandWithDate(LocalDate.of(2025, 1, 15));

    assertThatThrownBy(() -> createValidator().validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo(expectedErrorCode());
        });
  }

  @Test
  void shouldNotThrow_whenDateIsLastDayOfMonth() {
    CalculationCommand command = createCommandWithDate(LocalDate.of(2025, 1, 31));

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenDateIsNull() {
    CalculationCommand command = createCommandWithDate(null);

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenDateIsLastDayOfFeb() {
    CalculationCommand command = createCommandWithDate(LocalDate.of(2024, 2, 29));

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }
}
