package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopCommonHoldingsReqValidatorTest {

  private final TopCommonHoldingsReqValidator validator = new TopCommonHoldingsReqValidator(new HoldingsValidator(
      new HoldingsValidationProperties()));

  @Test
  void shouldThrow_whenAccumulateHoldingTypesExceedsTwelve() {
    Set<String> thirteenTypes = IntStream.rangeClosed(1, 13)
        .mapToObj(i -> "TYPE_" + i)
        .collect(Collectors.toSet());

    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(List.of(createHolding("ID1")));
    command.setAccumulateHoldingTypes(thirteenTypes);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.ACCUMULATE_HOLDING_TYPES_EXCEED_MAX);
        });
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
    command.setAccumulateHoldingTypes(Set.of("TYPE_1", "TYPE_2"));

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
