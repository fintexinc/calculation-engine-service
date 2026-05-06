package com.fintex.ce.application.returns;

/**
 * Identifies whether a {@link ReturnsSnapshot} represents the portfolio side or the benchmark side of a calculation.
 * Used by the orchestrator to pick the correct {@link ProcessingCase} for the requested weighted-average flow.
 */
public enum ReturnsRole {
  PORTFOLIO,
  BENCHMARK
}
