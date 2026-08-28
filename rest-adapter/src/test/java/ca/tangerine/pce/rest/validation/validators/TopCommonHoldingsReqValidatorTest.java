package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.TopCommonHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.holding.HoldingType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopCommonHoldingsReqValidatorTest {

  private final TopCommonHoldingsReqValidator validator = new TopCommonHoldingsReqValidator(new HoldingsValidator(
      new HoldingsValidationProperties()));

  /**
   * Every member of {@link HoldingType} at once — far more than the twelve the count check used to cap, which is the
   * point: the request no longer carries free-form strings, so that check could not fail and is gone. What replaces it
   * is the type itself, and an unrecognised code is rejected while the body is read, before any validator sees the
   * command.
   */
  @Test
  void shouldNotThrow_whenEveryHoldingTypeIsRequested() {
    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(List.of(holding("ID1", FiIdentifierType.TICKER, FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA, BigDecimal.TEN)));
    command.setAccumulateHoldingTypes(EnumSet.allOf(HoldingType.class));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenGicHoldingHasNoName() {
    GicHolding gicHolding = GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .build();

    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(List.of(gicHolding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.GIC_HOLDING_NAME_EMPTY);
        });
  }

  @Test
  void shouldThrow_whenAnyHoldingHasNullValue() {
    PortfolioHolding nullValueHolding = holding(
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER), FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        (BigDecimal) null);

    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(List.of(nullValueHolding));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL);
        });
  }

  @Test
  void shouldNotThrow_whenCommandIsValid() {
    GicHolding gicHolding = GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .name("My GIC")
        .build();

    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(List.of(
        holding("ID1", FiIdentifierType.TICKER, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN),
        holding("ID2", FiIdentifierType.TICKER, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.TEN),
        gicHolding));
    command.setAccumulateHoldingTypes(Set.of(HoldingType.E, HoldingType.B));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
