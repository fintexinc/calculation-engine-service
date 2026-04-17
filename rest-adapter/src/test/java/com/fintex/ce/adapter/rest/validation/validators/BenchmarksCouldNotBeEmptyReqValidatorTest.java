package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.List;

class BenchmarksCouldNotBeEmptyReqValidatorTest extends AbstractHoldingsNotEmptyReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new BenchmarksCouldNotBeEmptyReqValidator();
  }

  @Override
  CalculationCommand createCommandWithEmptyList() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(List.of());
    return cmd;
  }

  @Override
  CalculationCommand createCommandWithNullList() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(null);
    return cmd;
  }

  @Override
  CalculationCommand createCommandWithNonEmptyList() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);
    cmd.setBenchmarkHoldings(List.of(
        new PortfolioHolding(BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
            new SecurityIdentifier("ID1", FiIdentifierType.TICKER))));
    return cmd;
  }

  @Override
  String expectedMessage() {
    return "Benchmarks should not be empty";
  }
}
