package com.fintex.ce.model.dto.command.contract;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.util.List;

public interface HoldingsProvider {

  List<PortfolioHolding> getHoldings();

  void setHoldings(List<PortfolioHolding> holdings);
}
