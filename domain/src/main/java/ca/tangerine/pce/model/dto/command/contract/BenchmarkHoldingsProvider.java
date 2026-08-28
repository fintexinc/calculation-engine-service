package ca.tangerine.pce.model.dto.command.contract;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

import java.util.List;

public interface BenchmarkHoldingsProvider {

  List<PortfolioHolding> getBenchmarkHoldings();
}
