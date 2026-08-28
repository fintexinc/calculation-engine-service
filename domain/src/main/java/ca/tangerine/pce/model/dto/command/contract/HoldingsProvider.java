package ca.tangerine.pce.model.dto.command.contract;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

import java.util.List;

public interface HoldingsProvider {

  List<PortfolioHolding> getHoldings();

  void setHoldings(List<PortfolioHolding> holdings);
}
