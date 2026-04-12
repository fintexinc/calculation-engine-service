package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;

import java.util.List;

public interface BenchmarkHoldingsProvider {

  List<Holding> getBenchmarkHoldings();
}
