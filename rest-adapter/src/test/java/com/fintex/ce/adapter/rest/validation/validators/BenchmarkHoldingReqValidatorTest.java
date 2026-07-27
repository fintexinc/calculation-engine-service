package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.MerComparisonCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
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
  void shouldSupportEveryBenchmarkMetric() {
    assertThat(validator.supportedMetrics())
        .containsExactlyElementsOf(CalculationMetric.BENCHMARK_METRICS);
  }

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
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_MISSING_CURRENCY);
        });
  }

  @Test
  void shouldNotThrow_whenBenchmarkHoldingsAreValid() {
    PortfolioHolding h1 = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("CIG1101", FiIdentifierType.FUNDSERV));
    PortfolioHolding h2 = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier("VCNS", FiIdentifierType.TICKER));

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
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_EMPTY);
          assertThat(rve.getFieldName()).isEqualTo(BenchmarkHoldingReqValidator.BENCHMARK_HOLDINGS_FIELD);
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
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_EMPTY);
          assertThat(rve.getFieldName()).isEqualTo(BenchmarkHoldingReqValidator.BENCHMARK_HOLDINGS_FIELD);
        });
  }

  @Test
  void shouldThrow_whenMerComparisonBenchmarkHoldingsAreNull() {
    var cmd = new MerComparisonCommand();

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_EMPTY);
          assertThat(rve.getFieldName()).isEqualTo(BenchmarkHoldingReqValidator.BENCHMARK_HOLDINGS_FIELD);
        });
  }

  @Test
  void shouldThrow_whenMerComparisonBenchmarkHoldingHasNullHoldingType() {
    var cmd = new MerComparisonCommand();
    cmd.setBenchmarkHoldings(List.of(new PortfolioHolding(BigDecimal.TEN, null,
        new SecurityIdentifier("TDB622", FiIdentifierType.FUNDSERV))));

    assertThatThrownBy(() -> validator.validate(cmd)).isInstanceOf(ValidationException.class);
  }

  @Test
  void shouldNotThrow_whenMerComparisonBenchmarkIsASingleFund() {
    var cmd = new MerComparisonCommand();
    cmd.setBenchmarkHoldings(List.of(new PortfolioHolding(BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("TDB622", FiIdentifierType.FUNDSERV))));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenMerComparisonBenchmarkHasSeveralFunds() {
    var cmd = new MerComparisonCommand();
    cmd.setBenchmarkHoldings(List.of(
        new PortfolioHolding(BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
            new SecurityIdentifier("TDB622", FiIdentifierType.FUNDSERV)),
        new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.ETF_CANADA,
            new SecurityIdentifier("XBAL", FiIdentifierType.TICKER))));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }
}
