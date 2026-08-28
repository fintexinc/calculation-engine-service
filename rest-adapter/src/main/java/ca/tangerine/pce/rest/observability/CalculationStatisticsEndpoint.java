package ca.tangerine.pce.rest.observability;

import ca.tangerine.pce.port.observability.CalculationStatisticsProvider;
import ca.tangerine.pce.port.observability.CalculationStatisticsReport;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Serves the per-metric calculation statistics at {@code /actuator/calculation-stats}, once the id is added to
 * {@code management.endpoints.web.exposure.include}.
 *
 * <p>
 * Exposure is all this does. What the numbers are, where they come from and how they are ranked belongs to whatever
 * implements {@link CalculationStatisticsProvider}, so the same read model can be served over another transport, and
 * the aggregation can be tested without an HTTP boundary in the way.
 */
@Component
@RequiredArgsConstructor
@Endpoint(id = "calculation-stats")
public class CalculationStatisticsEndpoint {

  private final CalculationStatisticsProvider statisticsProvider;

  @ReadOperation
  public CalculationStatisticsReport statistics() {
    return statisticsProvider.statistics();
  }
}
