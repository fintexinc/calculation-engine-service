package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.ErrorParams;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UniqueHoldingsReqValidatorTest {

  private final UniqueHoldingsReqValidator validator = new UniqueHoldingsReqValidator();

  static Stream<Arguments> duplicateHoldings() {
    return Stream.of(
        Arguments.of(
            List.of(cashHolding(Currency.CAD), cashHolding(Currency.USD), cashHolding(Currency.CAD)),
            ErrorCode.DUPLICATE_CASH_HOLDING),
        Arguments.of(
            List.of(
                gicHolding(Currency.CAD, BigDecimal.valueOf(365), BigDecimal.valueOf(2.5), "GIC A"),
                gicHolding(Currency.CAD, new BigDecimal("365.0"), new BigDecimal("2.50"), "GIC B")),
            ErrorCode.DUPLICATE_GIC_HOLDING),
        Arguments.of(
            List.of(fundHolding("ID1", BigDecimal.TEN), fundHolding("ID1", BigDecimal.ONE)),
            ErrorCode.DUPLICATE_HOLDING));
  }

  @ParameterizedTest
  @MethodSource("duplicateHoldings")
  void shouldThrow_whenHoldingsContainDuplicates(List<PortfolioHolding> holdings, ErrorCode expectedErrorCode) {
    PeriodCommand command = new PeriodCommand();
    command.setHoldings(holdings);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(expectedErrorCode);
        });
  }

  @Test
  void shouldThrow_whenBenchmarkHoldingsContainDuplicates() {
    PeriodCommand command = new PeriodCommand();
    command.setHoldings(List.of(fundHolding("ID1", BigDecimal.TEN)));
    command.setBenchmarkHoldings(List.of(cashHolding(Currency.CAD), cashHolding(Currency.CAD)));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CASH_HOLDING);
        });
  }

  @Test
  void shouldThrow_whenMultiplePortfoliosCommandContainsPortfolioWithDuplicates() {
    MultiplePortfoliosCommand command = new MultiplePortfoliosCommand();
    command.setPortfolios(Set.of(new MultiplePortfoliosCommand.Portfolio(
        List.of(fundHolding("ID1", BigDecimal.TEN), fundHolding("ID1", BigDecimal.ONE)))));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_HOLDING);
          assertThat(rve.getMessage()).isEqualTo("Duplicate holding with id MUTUAL_FUND-ID1 found in request");
          assertThat(rve.getId()).isEqualTo("MUTUAL_FUND-ID1");
          assertThat(rve.getMetadata()).containsEntry(ErrorParams.HOLDING_ID, "MUTUAL_FUND-ID1");
        });
  }

  @Test
  void shouldNotThrow_whenAllHoldingsAreUnique() {
    List<PortfolioHolding> holdings = List.of(
        cashHolding(Currency.CAD),
        cashHolding(Currency.USD),
        gicHolding(Currency.CAD, BigDecimal.valueOf(365), BigDecimal.valueOf(2.5), "GIC A"),
        gicHolding(Currency.CAD, BigDecimal.valueOf(730), BigDecimal.valueOf(2.5), "GIC B"),
        gicHolding(Currency.CAD, BigDecimal.valueOf(365), BigDecimal.valueOf(3.5), "GIC C"),
        gicHolding(Currency.USD, BigDecimal.valueOf(365), BigDecimal.valueOf(2.5), "GIC D"),
        fundHolding("ID1", BigDecimal.TEN),
        fundHolding("ID2", BigDecimal.TEN),
        new PortfolioHolding(BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
            new SecurityIdentifier("ID1", FiIdentifierType.FUNDSERV)));

    PeriodCommand command = new PeriodCommand();
    command.setHoldings(holdings);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCashHoldingsHaveNullCurrencies() {
    PeriodCommand command = new PeriodCommand();
    command.setHoldings(List.of(cashHolding(null), cashHolding(null)));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenHoldingsAreNull() {
    PeriodCommand command = new PeriodCommand();

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandHasNoHoldings() {
    CalculationCommand command = new CalculationCommand() {};

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  private static CashHolding cashHolding(Currency currency) {
    return CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(currency)
        .build();
  }

  private static GicHolding gicHolding(Currency currency, BigDecimal term, BigDecimal clientIntRate, String name) {
    return GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .currency(currency)
        .term(term)
        .clientIntRate(clientIntRate)
        .name(name)
        .build();
  }

  private static PortfolioHolding fundHolding(String id, BigDecimal value) {
    return new PortfolioHolding(value, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        new SecurityIdentifier(id, FiIdentifierType.TICKER));
  }
}
