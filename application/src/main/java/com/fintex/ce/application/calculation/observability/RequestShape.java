package com.fintex.ce.application.calculation.observability;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.dto.command.contract.BenchmarkHoldingsProvider;
import com.fintex.ce.model.dto.command.contract.HoldingsProvider;

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Portfolio and benchmark holding counts carried by one calculation command. The counts are resolved through the
 * command contracts rather than concrete command types, so a new command is measured without changes here.
 */
public record RequestShape(int holdings, int benchmarkHoldings) {

  public static final RequestShape EMPTY = new RequestShape(0, 0);

  public static RequestShape of(CalculationCommand command) {
    return command == null
        ? EMPTY
        : new RequestShape(holdingsCount(command), benchmarkHoldingsCount(command));
  }

  public RequestShape plus(RequestShape other) {
    return other == null
        ? this
        : new RequestShape(holdings + other.holdings, benchmarkHoldings + other.benchmarkHoldings);
  }

  private static int holdingsCount(CalculationCommand command) {
    if (command instanceof MultiplePortfoliosCommand multiplePortfoliosCommand) {
      return multiplePortfoliosHoldingsCount(multiplePortfoliosCommand);
    }
    if (command instanceof HoldingsProvider holdingsProvider) {
      return sizeOf(holdingsProvider.getHoldings());
    }
    return 0;
  }

  private static int multiplePortfoliosHoldingsCount(MultiplePortfoliosCommand command) {
    if (CollectionUtils.isEmpty(command.getPortfolios())) {
      return 0;
    }
    return command.getPortfolios().stream()
        .map(MultiplePortfoliosCommand.Portfolio::getHoldings)
        .mapToInt(RequestShape::sizeOf)
        .sum();
  }

  private static int benchmarkHoldingsCount(CalculationCommand command) {
    return command instanceof BenchmarkHoldingsProvider provider ? sizeOf(provider.getBenchmarkHoldings()) : 0;
  }

  private static int sizeOf(List<PortfolioHolding> holdings) {
    return CollectionUtils.isEmpty(holdings) ? 0 : holdings.size();
  }
}
