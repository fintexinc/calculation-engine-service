package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

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
    cmd.setCurrency(CurrencyType.CAD);
    cmd.setBenchmarkHoldings(List.of());
    return cmd;
  }

  @Override
  CalculationCommand createCommandWithNullList() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);
    cmd.setBenchmarkHoldings(null);
    return cmd;
  }

  @Override
  CalculationCommand createCommandWithNonEmptyList() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);
    cmd.setBenchmarkHoldings(List.of(
        new Holding(BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA,
            new SecurityIdentifier("ID1", FiIdentifierType.TICKER))));
    return cmd;
  }

  @Override
  String expectedMessage() {
    return "Benchmarks should not be empty";
  }
}
