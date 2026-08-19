package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
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
import java.util.Set;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonPerformanceDatesReqValidatorTest {

  private final CommonPerformanceDatesReqValidator validator = new CommonPerformanceDatesReqValidator(
      new HoldingsValidator(new HoldingsValidationProperties()));

  @Test
  void shouldThrow_whenBenchmarkHasCashWithNullCurrency() {
    CashHolding cashHolding = cash(null, BigDecimal.TEN);

    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(List.of(cashHolding));
    cmd.setPortfolios(Collections.emptySet());

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_MISSING_CURRENCY);
        });
  }

  @Test
  void shouldThrow_whenPortfolioHasCashWithNullCurrency() {
    CashHolding cashHolding = cash(null, BigDecimal.TEN);

    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(Collections.emptyList());
    cmd.setPortfolios(Set.of(new MultiplePortfoliosCommand.Portfolio(List.of(cashHolding))));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_MISSING_CURRENCY);
        });
  }

  @Test
  void shouldNotThrow_whenAllHoldingsAreValid() {
    PortfolioHolding holding = holding(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);
    CashHolding cashHolding = cash(Currency.CAD, BigDecimal.TEN);

    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(List.of(holding));
    cmd.setPortfolios(Set.of(new MultiplePortfoliosCommand.Portfolio(List.of(cashHolding))));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenBenchmarkAndPortfoliosAreEmpty() {
    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(Collections.emptyList());
    cmd.setPortfolios(Collections.emptySet());

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotMultiplePortfolios() {
    var cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }
}
