package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpedLastDayOfMonthReqValidatorTest extends AbstractLastDayOfMonthReqValidatorTest {

  private final CpedLastDayOfMonthReqValidator validator = new CpedLastDayOfMonthReqValidator();

  @Override
  RequestValidator createValidator() {
    return validator;
  }

  @Override
  CalculationCommand createCommandWithDate(LocalDate date) {
    PeriodCommand command = new PeriodCommand();
    command.setCustomPed(date);
    command.setCurrency(CurrencyType.CAD);
    return command;
  }

  @Override
  String expectedErrorCode() {
    return "ERR_RRC_CPED_001";
  }

  @Test
  void shouldNotThrow_whenReturnCommandDateIsLastDayOfMonth() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPed(LocalDate.of(2025, 1, 31));
    command.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenReturnCommandDateIsNotLastDayOfMonth() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPed(LocalDate.of(2025, 1, 15));
    command.setCurrency(CurrencyType.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_CPED_001");
        });
  }
}
