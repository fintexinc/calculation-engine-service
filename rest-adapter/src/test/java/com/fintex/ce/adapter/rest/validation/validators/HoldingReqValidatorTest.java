package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.ReturnCommand;
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

class HoldingReqValidatorTest {

  private final HoldingReqValidator validator = new HoldingReqValidator();

  @Test
  void shouldThrow_whenDuplicateNonGicHoldingsExist() {
    Holding holding1 = new Holding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    Holding holding2 = new Holding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    PeriodCommand command = new PeriodCommand();
    command.setCurrency(CurrencyType.CAD);
    command.setHoldings(List.of(holding1, holding2));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_DH_001");
        });
  }

  @Test
  void shouldThrow_whenCashHoldingHasNullCurrency() {
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(null)
        .build();

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(CurrencyType.CAD);
    command.setHoldings(List.of(cashHolding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_MC_002");
        });
  }

  @Test
  void shouldNotThrow_whenHoldingsAreValid() {
    Holding holding1 = new Holding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    Holding holding2 = new Holding(
        BigDecimal.TEN,
        FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier("ID2", FiIdentifierType.TICKER));
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(CurrencyType.CAD)
        .build();

    PeriodCommand command = new PeriodCommand();
    command.setCurrency(CurrencyType.CAD);
    command.setHoldings(List.of(holding1, holding2, cashHolding));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenHoldingsAreEmpty() {
    PeriodCommand command = new PeriodCommand();
    command.setCurrency(CurrencyType.CAD);
    command.setHoldings(Collections.emptyList());

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotHoldingsProvider() {
    com.fintex.ce.domain.dto.command.CalculationCommand command = new com.fintex.ce.domain.dto.command.CalculationCommand() {};

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
