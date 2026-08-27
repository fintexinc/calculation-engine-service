package ca.tangerine.pce.model.domain.result.composite;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Aggregate response of a composite calculation request. Successful metrics land in {@code results} keyed by metric;
 * metrics whose calculation failed land in {@code failures} with the notifications describing the failure, so one
 * failing metric does not discard the results of the others.
 */
@Getter
@Builder
@Schema(description = "Aggregated results of a composite calculation request")
public class CompositeCalculationResult {

  @Schema(description = "Successfully calculated results keyed by metric")
  private final Map<CalculationMetric, BaseCalculationResult> results;

  @Schema(description = "Notifications for metrics whose calculation failed, keyed by metric")
  private final Map<CalculationMetric, List<Notification>> failures;

}
