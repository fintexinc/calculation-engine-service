package com.fintex.ce.adapter.rest.observability;

import com.fintex.ce.port.observability.CalculationStatisticsProvider;
import com.fintex.ce.port.observability.CalculationStatisticsReport;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Serves the per-metric calculation statistics at {@code /actuator/calculationstats}, once the id is added to
 * {@code management.endpoints.web.exposure.include}.
 *
 * <p>
 * Exposure is all this does. What the numbers are, where they come from and how they are ranked belongs to whatever
 * implements {@link CalculationStatisticsProvider}, so the same read model can be served over another transport, and
 * the aggregation can be tested without an HTTP boundary in the way.
 */
@Component
@RequiredArgsConstructor
@Endpoint(id = "calculationstats")
public class CalculationStatisticsEndpoint {

  private final CalculationStatisticsProvider statisticsProvider;

  @ReadOperation
  public CalculationStatisticsReport statistics() {
    return statisticsProvider.statistics();
  }
}
