package ca.tangerine.pce.model.domain.result.fee;

import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.HashMap;
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
@Schema(description = "Response for management-fee metric. Contains weighted average management fee by parameter type.")
public class ManagementFeeResult extends BaseCalculationResult {

  @Schema(description = "Management fee by parameter type (scaled/absolute)")
  @Builder.Default
  private Map<FeeAggregationMode, BigDecimal> managementFee = new HashMap<>();
}
