package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.exceptions.ValidationException;
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

class BenchmarkHoldingReqValidatorTest {

  private final BenchmarkHoldingReqValidator validator = new BenchmarkHoldingReqValidator();

  @Test
  void shouldThrow_whenBenchmarkCashHoldingHasNullCurrency() {
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(null)
        .build();

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(List.of(cashHolding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("HOLDING_MISSING_CURRENCY");
        });
  }

  @Test
  void shouldNotThrow_whenBenchmarkHoldingsAreValid() {
    PortfolioHolding h1 = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    PortfolioHolding h2 = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier("ID2", FiIdentifierType.TICKER));

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(List.of(h1, h2));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenBenchmarkHoldingsAreEmpty() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(Collections.emptyList());

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("FIELD_NOT_EMPTY");
          assertThat(rve.getFieldName()).isEqualTo("benchmarkHoldings");
        });
  }

  @Test
  void shouldThrow_whenBenchmarkHoldingsAreNull() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(null);

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("FIELD_NOT_EMPTY");
          assertThat(rve.getFieldName()).isEqualTo("benchmarkHoldings");
        });
  }
}
