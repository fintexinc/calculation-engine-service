package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
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

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingReqValidatorTest {

  private final HoldingsValidator holdingsValidator = new HoldingsValidator(new HoldingsValidationProperties());
  private final HoldingReqValidator validator = new HoldingReqValidator(holdingsValidator);

  @Test
  void shouldThrow_whenHoldingTypeIsNull() {
    PortfolioHolding holding = holdingWithoutCountry(new SecurityIdentifier("ID1", FiIdentifierType.TICKER), null,
        BigDecimal.TEN);

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
    PortfolioHolding validHolding = holding(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);
    PortfolioHolding invalidHolding = holdingWithoutCountry(new SecurityIdentifier("ID2", FiIdentifierType.TICKER),
        null, BigDecimal.TEN);

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
    PortfolioHolding holding = holdingWithoutCountry(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, BigDecimal.TEN);

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
    PortfolioHolding holding = holdingWithoutCountry(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, BigDecimal.TEN);

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
    PortfolioHolding holding = holding(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.GERMANY, BigDecimal.TEN);

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
    GicHolding gicHolding = GicHolding.builder()
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
    CashHolding cashHolding = cash(null, BigDecimal.TEN);

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
    PortfolioHolding holding1 = holding(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);
    PortfolioHolding holding2 = holding(new SecurityIdentifier("ID2", FiIdentifierType.TICKER),
        FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.TEN);
    CashHolding cashHolding = cash(Currency.CAD, BigDecimal.TEN);

    PeriodCommand command = new PeriodCommand();
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(holding1, holding2, cashHolding));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdIsEmpty() {
    PortfolioHolding holding = holding(new SecurityIdentifier("", FiIdentifierType.FUNDSERV),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);

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
    PortfolioHolding holding = holding(new SecurityIdentifier("   ", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, BigDecimal.TEN);

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
    PortfolioHolding holding = holding(new SecurityIdentifier("ID1", null),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);

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
    PortfolioHolding holding = holding(null, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        BigDecimal.TEN);

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
    CashHolding cashHolding = cash(Currency.CAD, BigDecimal.TEN);

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
