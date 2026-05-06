package com.fintex.ce.application.returns;

/**
 * Identifies a per-query returns-processing pipeline.
 *
 * <p>
 * Each value combines the role of the returns being processed (portfolio versus benchmark) with the variant of the
 * weighted-average computation requested by the orchestrator. {@code ReturnsProcessor} beans declare the cases they
 * apply to via {@code isApplicable(ProcessingCase)}; the orchestrator filters the injected processor list by case once
 * at startup and runs the resulting per-case pipeline in {@code @Order} order.
 * </p>
 *
 * <p>
 * The {@code *_PRE_PSD_TRIM} variants stop the pipeline after Cped validation, end-cut, and FX conversion — used by
 * correlation calculations that need the post-FX returns map before the per-holding start-date trim.
 * </p>
 */
public enum ProcessingCase {
  PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED,
  BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED,
  PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY,
  BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY,
  PORTFOLIO_PRE_PSD_TRIM,
  BENCHMARK_PRE_PSD_TRIM
}
