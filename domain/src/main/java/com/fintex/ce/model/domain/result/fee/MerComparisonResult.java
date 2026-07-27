package com.fintex.ce.model.domain.result.fee;

import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

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

/**
 * Response for the {@code mer-benchmark-comparison} metric: one {@link MerComparison} per requested aggregation view
 * (funds-only / whole-portfolio / funds-only-strict).
 */
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for mer-benchmark-comparison: portfolio MER vs benchmark MER by aggregation view")
public class MerComparisonResult extends BaseCalculationResult {

  @Schema(description = "Comparison by aggregation view (scaled/absolute/forceReportFee)")
  @Builder.Default
  private Map<FeeAggregationMode, MerComparison> comparison = new EnumMap<>(FeeAggregationMode.class);
}
