package ca.tangerine.pce.model.dto.command.contract;

import java.util.List;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

public interface BenchmarkHoldingsProvider {

  List<PortfolioHolding> getBenchmarkHoldings();
}
