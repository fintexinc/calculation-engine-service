package ca.tangerine.pce.model.domain.enumeration;

/**
 * The role a return series plays within a metric calculation. Metrics that compare two series (e.g. tracking error,
 * information ratio, beta) distinguish the subject portfolio from the benchmark it is measured against.
 */
public enum MetricRole {
  PORTFOLIO,
  BENCHMARK
}
