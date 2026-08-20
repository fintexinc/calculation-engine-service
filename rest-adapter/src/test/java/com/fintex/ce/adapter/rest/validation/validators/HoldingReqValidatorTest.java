package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingReqValidatorTest {

  private final HoldingsValidator holdingsValidator = new HoldingsValidator(new HoldingsValidationProperties());
  private final HoldingReqValidator validator = new HoldingReqValidator(holdingsValidator);

  @Test
  void shouldThrow_whenHoldingTypeIsNull() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        null,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("holdingType");
          assertThat(rve.getMessage()).isEqualTo("Holding Type must not be null");
        });
  }

  @Test
  void shouldThrow_whenOneHoldingIsMissingHoldingType() {
    PortfolioHolding validHolding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    PortfolioHolding invalidHolding = new PortfolioHolding(
        BigDecimal.TEN,
        null,
        new SecurityIdentifier("ID2", FiIdentifierType.TICKER));

    PeriodCommand command = new PeriodCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(validHolding, invalidHolding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("holdingType");
          assertThat(rve.getMessage()).isEqualTo("Holding Type must not be null");
        });
  }

  @Test
  void shouldThrow_whenFundHoldingIsMissingCountry() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("country");
          assertThat(rve.getMessage()).isEqualTo("Country must not be null");
        });
  }

  @Test
  void shouldThrow_whenStockHoldingIsMissingCountry() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.STOCK,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("country");
        });
  }

  @Test
  void shouldThrow_whenFundHoldingHasUnsupportedCountry() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND,
        Country.GERMANY,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.COUNTRY_NOT_SUPPORTED);
          assertThat(rve.getFieldName()).isEqualTo("country");
        });
  }

  @Test
  void shouldNotThrow_whenGicHoldingHasNoCountry() {
    com.fintex.ce.model.domain.holding.GicHolding gicHolding = com.fintex.ce.model.domain.holding.GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .name("GIC-1")
        .currency(Currency.CAD)
        .term(BigDecimal.ONE)
        .build();

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(gicHolding));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

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
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_MISSING_CURRENCY);
          assertThat(rve.getMessage()).isEqualTo("The holding CASH-10 is missing Currency");
        });
  }

  @Test
  void shouldNotThrow_whenHoldingsAreValid() {
    PortfolioHolding holding1 = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    PortfolioHolding holding2 = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.ETF,
        Country.CANADA,
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
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        new SecurityIdentifier("", FiIdentifierType.FUNDSERV));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK);
          assertThat(rve.getFieldName()).isEqualTo("securityIdentifier.id");
          assertThat(rve.getMessage()).isEqualTo("Security Identifier ID must not be blank");
        });
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdIsBlank() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.STOCK,
        Country.USA,
        new SecurityIdentifier("   ", FiIdentifierType.TICKER));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK);
          assertThat(rve.getMessage()).isEqualTo("Security Identifier ID must not be blank");
        });
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdTypeIsNull() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        new SecurityIdentifier("ID1", null));

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("securityIdentifier.idType");
          assertThat(rve.getMessage()).isEqualTo("Security Identifier ID Type must not be null");
        });
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIsNull_forSecurityHolding() {
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        null);

    ReturnCommand command = new ReturnCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("securityIdentifier");
          assertThat(rve.getMessage()).isEqualTo("Security Identifier must not be null");
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
