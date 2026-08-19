package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.MerComparisonCommand;
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

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BenchmarkHoldingReqValidatorTest {

  private final HoldingsValidator holdingsValidator = new HoldingsValidator(new HoldingsValidationProperties());
  private final BenchmarkHoldingReqValidator validator = new BenchmarkHoldingReqValidator(holdingsValidator);

  @Test
  void shouldSupportEveryBenchmarkMetric() {
    assertThat(validator.supportedMetrics())
        .containsExactlyElementsOf(CalculationMetric.BENCHMARK_METRICS);
  }

  @Test
  void shouldThrow_whenBenchmarkCashHoldingHasNullCurrency() {
    CashHolding cashHolding = cash(null, BigDecimal.TEN);

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
    PortfolioHolding h1 = holding(new SecurityIdentifier("CIG1101", FiIdentifierType.FUNDSERV),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);
    PortfolioHolding h2 = holding(new SecurityIdentifier("VCNS", FiIdentifierType.TICKER),
        FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.TEN);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(List.of(h1, h2));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenBenchmarkIndexHasNoCountry() {
    PortfolioHolding index = holdingWithoutCountry(
        new SecurityIdentifier("F000015366", FiIdentifierType.TICKER), FinancialInstrumentType.BENCHMARK_INDEX,
        BigDecimal.ONE);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(List.of(index));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenBenchmarkFundHoldingIsMissingCountry() {
    PortfolioHolding fund = holdingWithoutCountry(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, BigDecimal.TEN);

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(List.of(fund));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("country");
        });
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
    cmd.setBenchmarkHoldings(List.of(holdingWithoutCountry(
        new SecurityIdentifier("TDB622", FiIdentifierType.FUNDSERV), null, BigDecimal.TEN)));

    assertThatThrownBy(() -> validator.validate(cmd)).isInstanceOf(ValidationException.class);
  }

  @Test
  void shouldNotThrow_whenMerComparisonBenchmarkIsASingleFund() {
    var cmd = new MerComparisonCommand();
    cmd.setBenchmarkHoldings(List.of(holding(
        new SecurityIdentifier("TDB622", FiIdentifierType.FUNDSERV), FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA, BigDecimal.TEN)));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenMerComparisonBenchmarkHasSeveralFunds() {
    var cmd = new MerComparisonCommand();
    cmd.setBenchmarkHoldings(List.of(
        holding(new SecurityIdentifier("TDB622", FiIdentifierType.FUNDSERV),
            FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN),
        holding(new SecurityIdentifier("XBAL", FiIdentifierType.TICKER),
            FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.ONE)));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }
}
