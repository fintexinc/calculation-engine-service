package ca.tangerine.pce.model.domain.result.fee;

import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for mer metric. Contains weighted average management expense ratio (MER) by parameter type.")
public class AverageMerResult extends BaseCalculationResult {

  @Schema(description = "Management expense ratio by parameter type (scaled/absolute)")
  @Builder.Default
  private Map<FeeAggregationMode, BigDecimal> managementExpenseRatio = new EnumMap<>(FeeAggregationMode.class);

  /**
   * The FX-converted market-value denominator behind each mode's weighted-average MER, i.e. the exact asset base that
   * mode's ratio is normalised over (MER-bearing holdings only for {@code FUNDS_ONLY}, the whole portfolio for
   * {@code WHOLE_PORTFOLIO}). Consumed by {@code mer-benchmark-comparison} to turn a ratio difference into an annual
   * dollar impact without re-running FX conversion or fee resolution. Internal to the calculation pipeline — excluded
   * from the {@code mer} metric's response contract.
   */
  @JsonIgnore
  @Builder.Default
  private Map<FeeAggregationMode, BigDecimal> baseValue = new EnumMap<>(FeeAggregationMode.class);
}
