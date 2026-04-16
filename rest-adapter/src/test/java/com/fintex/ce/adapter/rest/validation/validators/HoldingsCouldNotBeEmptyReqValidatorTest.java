package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.List;

class HoldingsCouldNotBeEmptyReqValidatorTest extends AbstractHoldingsNotEmptyReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new HoldingsCouldNotBeEmptyReqValidator();
  }

  @Override
  CalculationCommand createCommandWithEmptyList() {
    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of());
    return cmd;
  }

  @Override
  CalculationCommand createCommandWithNullList() {
    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(null);
    return cmd;
  }

  @Override
  CalculationCommand createCommandWithNonEmptyList() {
    var cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of(
        new Holding(BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
            new SecurityIdentifier("ID1", FiIdentifierType.TICKER))));
    return cmd;
  }

  @Override
  String expectedMessage() {
    return "Holdings could not be empty";
  }
}
