package com.fintex.ce.adapter.rest.observability;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * A registry configured the way the running application configures its own, so a test sees the percentiles the
 * production registry would publish. Mirrors {@code management.metrics.distribution.percentiles} for the
 * {@code portfolio.calculation} prefix in {@code application.yml}, which is the single place that decides them.
 */
final class ConfiguredMeterRegistry {

  private static final double[] PERCENTILES = {0.5, 0.95, 0.99};

  private ConfiguredMeterRegistry() {
  }

  static SimpleMeterRegistry withPercentiles() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    registry.config().meterFilter(new MeterFilter() {

      @Override
      public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
        if (!id.getName().startsWith("portfolio.calculation")) {
          return config;
        }
        return DistributionStatisticConfig.builder()
            .percentiles(PERCENTILES)
            .build()
            .merge(config);
      }
    });
    return registry;
  }
}
