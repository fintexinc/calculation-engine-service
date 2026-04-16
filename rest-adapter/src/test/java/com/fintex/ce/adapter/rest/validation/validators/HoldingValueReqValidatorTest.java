package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingValueReqValidatorTest {

  private final HoldingValueReqValidator validator = new HoldingValueReqValidator();

  @Test
  void shouldThrow_whenHoldingValueIsNull() {
    Holding holding = new Holding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA, null);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_ALL_GTZ_001");
        });
  }

  @Test
  void shouldThrow_whenHoldingValueIsNegative() {
    Holding holding = new Holding(
        BigDecimal.valueOf(-1), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_ALL_GTZ_001");
        });
  }

  @Test
  void shouldThrow_whenHoldingValueIsNull_andHasSecurityId() {
    Holding holding = new Holding(
        null, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("FUND1", FiIdentifierType.TICKER));

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_ALL_GTZ_001");
          assertThat(rve.getId()).isEqualTo("FUND1");
        });
  }

  @Test
  void shouldNotThrow_whenHoldingValuesArePositive() {
    Holding holding = new Holding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenHoldingsAreEmpty() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(Collections.emptyList());

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotHoldingsProvider() {
    com.fintex.ce.model.dto.command.CalculationCommand command = new com.fintex.ce.model.dto.command.CalculationCommand() {};

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
