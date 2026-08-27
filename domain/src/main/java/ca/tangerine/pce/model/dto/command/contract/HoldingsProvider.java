package ca.tangerine.pce.model.dto.command.contract;

import java.util.List;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

public interface HoldingsProvider {

  List<PortfolioHolding> getHoldings();

  void setHoldings(List<PortfolioHolding> holdings);
}
