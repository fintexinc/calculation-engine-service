package ca.tangerine.pce.rest.validation.validators;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.cash;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.tangerine.pce.model.domain.holding.CashHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.MultiplePortfoliosCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

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
