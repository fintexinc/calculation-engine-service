package ca.tangerine.pce.model.domain.result.fee;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.EnumMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;

/**
 * Response for the {@code mer-benchmark-comparison} metric: one {@link FeeComparison} per requested aggregation view
 * (funds-only / whole-portfolio / funds-only-strict).
 */
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for mer-benchmark-comparison: portfolio fee rate vs benchmark fee rate, and the projected spend of each, by aggregation view")
public class MerComparisonResult extends BaseCalculationResult {

  @Schema(description = "Comparison by aggregation view (scaled/absolute/forceReportFee)")
  @Builder.Default
  private Map<FeeAggregationMode, FeeComparison> comparison = new EnumMap<>(FeeAggregationMode.class);
}
