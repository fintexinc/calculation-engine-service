package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class AbstractGicFieldReqValidatorTest {

  abstract RequestValidator createValidator();

  abstract GicHolding createInvalidGicHolding();

  abstract GicHolding createValidGicHolding();

  abstract String expectedErrorCode();

  @Test
  void shouldThrow_whenGicHoldingMissingRequiredField() {
    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of(createInvalidGicHolding()));

    assertThatThrownBy(() -> createValidator().validate(cmd))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo(expectedErrorCode());
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
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of(holding));

    assertThatCode(() -> createValidator().validate(cmd)).doesNotThrowAnyException();
  }
}
