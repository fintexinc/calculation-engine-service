package com.fintex.ce.model.domain.result.fee;

import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

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
@Schema(description = "Annual and monthly portfolio fee dollar amounts. Each map is keyed by aggregation mode "
    + "(scaled / absolute / forceReportFee). Annual = Σ (holding value × resolved MER); Monthly = Annual ÷ 12.")
public class FeesResult extends BaseCalculationResult {

  @Schema(description = "Annual fee CAD amount by aggregation mode")
  @Builder.Default
  private Map<FeeAggregationMode, BigDecimal> annualFee = new EnumMap<>(FeeAggregationMode.class);

  @Schema(description = "Monthly fee CAD amount by aggregation mode (annual / 12)")
  @Builder.Default
  private Map<FeeAggregationMode, BigDecimal> monthlyFee = new EnumMap<>(FeeAggregationMode.class);
}
