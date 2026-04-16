package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpsdLastDayOfMonthReqValidatorTest extends AbstractLastDayOfMonthReqValidatorTest {

  private final CpsdLastDayOfMonthReqValidator validator = new CpsdLastDayOfMonthReqValidator();

  @Override
  RequestValidator createValidator() {
    return validator;
  }

  @Override
  CalculationCommand createCommandWithDate(LocalDate date) {
    RollingCalculationCommand command = new RollingCalculationCommand();
    command.setCustomPsd(date);
    command.setCurrency(Currency.CAD);
    return command;
  }

  @Override
  String expectedErrorCode() {
    return "ERR_RRC_CPSD_001";
  }

  @Test
  void shouldNotThrow_whenReturnCommandDateIsLastDayOfMonth() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPsd(LocalDate.of(2025, 1, 31));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenReturnCommandDateIsNotLastDayOfMonth() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPsd(LocalDate.of(2025, 1, 15));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_CPSD_001");
        });
  }
}
