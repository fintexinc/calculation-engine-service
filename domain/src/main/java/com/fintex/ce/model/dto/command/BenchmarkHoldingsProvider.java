package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.Holding;

import java.util.List;

public interface BenchmarkHoldingsProvider {

  List<Holding> getBenchmarkHoldings();
}
