package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.holding.HoldingType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    command.setHoldings(List.of(createHolding("ID1")));
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
    PortfolioHolding nullValueHolding = new PortfolioHolding(
        null,
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

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
    command.setHoldings(List.of(createHolding("ID1"), createHolding("ID2"), gicHolding));
    command.setAccumulateHoldingTypes(Set.of(HoldingType.E, HoldingType.B));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  private PortfolioHolding createHolding(String id) {
    return new PortfolioHolding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        new SecurityIdentifier(id, FiIdentifierType.TICKER));
  }
}
