package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

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
  void shouldThrow_whenBenchmarkHasDuplicateNonGicHoldings() {
    Holding h1 = new Holding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    Holding h2 = new Holding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);
    cmd.setBenchmarkHoldings(List.of(h1, h2));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_DH_001");
        });
  }

  @Test
  void shouldThrow_whenBenchmarkCashHoldingHasNullCurrency() {
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(null)
        .build();

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);
    cmd.setBenchmarkHoldings(List.of(cashHolding));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_MC_002");
        });
  }

  @Test
  void shouldNotThrow_whenBenchmarkHoldingsAreValid() {
    Holding h1 = new Holding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    Holding h2 = new Holding(
        BigDecimal.TEN, FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier("ID2", FiIdentifierType.TICKER));

    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);
    cmd.setBenchmarkHoldings(List.of(h1, h2));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenBenchmarkHoldingsAreEmpty() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);
    cmd.setBenchmarkHoldings(Collections.emptyList());

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }
}
