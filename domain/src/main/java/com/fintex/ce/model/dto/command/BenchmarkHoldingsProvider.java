package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.util.List;

public interface BenchmarkHoldingsProvider {

  List<PortfolioHolding> getBenchmarkHoldings();
}
