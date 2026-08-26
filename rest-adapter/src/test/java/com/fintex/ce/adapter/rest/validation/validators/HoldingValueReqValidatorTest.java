package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingValueReqValidatorTest {

  private final HoldingValueReqValidator validator = new HoldingValueReqValidator(new HoldingsValidator(
      new HoldingsValidationProperties()));

  @Test
  void shouldThrow_whenHoldingValueIsNull() {
    PortfolioHolding holding = holding(null, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        (BigDecimal) null);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL);
        });
  }

  @Test
  void shouldThrow_whenHoldingValueIsNegative() {
    PortfolioHolding holding = holding(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.valueOf(-1));

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL);
        });
  }

  @Test
  void shouldThrow_whenHoldingValueIsNull_andHasSecurityId() {
    PortfolioHolding holding = holding(new SecurityIdentifier("FUND1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, (BigDecimal) null);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL);
          assertThat(rve.getId()).isEqualTo("MUTUAL_FUND-FUND1");
        });
  }

  @Test
  void shouldNotThrow_whenHoldingValuesArePositive() {
    PortfolioHolding holding = holding(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(holding));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenAllHoldingValuesAreZero() {
    PortfolioHolding zeroHolding = holding(new SecurityIdentifier("HEWJ", FiIdentifierType.TICKER),
        FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.ZERO);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(zeroHolding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_VALUES_SUM_NOT_POSITIVE);
        });
  }

  @Test
  void shouldNotThrow_whenAnyHoldingValueIsZero_butSumIsPositive() {
    PortfolioHolding zero = holding(new SecurityIdentifier("HEWJ", FiIdentifierType.TICKER),
        FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.ZERO);
    PortfolioHolding positive = holding(new SecurityIdentifier("FUND1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setHoldings(List.of(zero, positive));

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
