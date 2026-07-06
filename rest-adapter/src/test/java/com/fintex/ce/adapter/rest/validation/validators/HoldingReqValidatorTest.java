package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
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

class HoldingReqValidatorTest {

  private final HoldingReqValidator validator = new HoldingReqValidator();

  @Test
  void shouldThrow_whenCashHoldingHasNullCurrency() {
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(null)
        .build();

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(cashHolding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("HOLDING_MISSING_CURRENCY");
        });
  }

  @Test
  void shouldNotThrow_whenHoldingsAreValid() {
    PortfolioHolding holding1 = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    PortfolioHolding holding2 = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier("ID2", FiIdentifierType.TICKER));
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(Currency.CAD)
        .build();

    PeriodCommand command = new PeriodCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding1, holding2, cashHolding));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdIsEmpty() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("", FiIdentifierType.FUNDSERV));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("FIELD_NOT_BLANK");
          assertThat(rve.getFieldName()).isEqualTo("securityIdentifier.id");
        });
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdIsBlank() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.STOCK_US,
        new SecurityIdentifier("   ", FiIdentifierType.TICKER));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> assertThat(((ValidationException) ex).getErrorCode().name()).isEqualTo("FIELD_NOT_BLANK"));
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdTypeIsNull() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", null));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("FIELD_NOT_NULL");
          assertThat(rve.getFieldName()).isEqualTo("securityIdentifier.idType");
        });
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIsNull_forSecurityHolding() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        null);

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("FIELD_NOT_NULL");
          assertThat(rve.getFieldName()).isEqualTo("securityIdentifier");
        });
  }

  @Test
  void shouldNotThrow_whenCashHoldingHasNoSecurityIdentifier() {
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(Currency.CAD)
        .build();

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(cashHolding));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenHoldingsAreEmpty() {
    PeriodCommand command = new PeriodCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(Collections.emptyList());

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotHoldingsProvider() {
    com.fintex.ce.model.dto.command.CalculationCommand command = new com.fintex.ce.model.dto.command.CalculationCommand() {};

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
