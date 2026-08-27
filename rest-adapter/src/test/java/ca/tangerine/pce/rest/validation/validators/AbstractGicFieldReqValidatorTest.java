package ca.tangerine.pce.rest.validation.validators;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

abstract class AbstractGicFieldReqValidatorTest {

  abstract RequestValidator createValidator();

  abstract GicHolding createInvalidGicHolding();

  abstract GicHolding createValidGicHolding();

  abstract String expectedErrorCode();

  abstract String expectedMessage();

  @Test
  void shouldThrow_whenGicHoldingMissingRequiredField() {
    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of(createInvalidGicHolding()));

    assertThatThrownBy(() -> createValidator().validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo(expectedErrorCode());
          assertThat(rve.getMessage()).isEqualTo(expectedMessage());
        });
  }

  @Test
  void shouldNotThrow_whenGicHoldingHasRequiredField() {
    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of(createValidGicHolding()));

    assertThatCode(() -> createValidator().validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenNoGicHoldings() {
    PortfolioHolding holding = holding(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN);

    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of(holding));

    assertThatCode(() -> createValidator().validate(cmd)).doesNotThrowAnyException();
  }
}
