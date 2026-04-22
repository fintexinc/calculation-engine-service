package com.fintex.ce.model.domain.result.allocation;

import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegionEmType;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response for asset-allocations-em metric. Contains portfolio asset allocation breakdown by emerging markets region.")
public class AssetAllocationEMResult extends BaseCalculationResult {

  @Schema(description = "Asset allocation percentages by emerging markets region")
  private Map<AssetAllocationRegionEmType, BigDecimal> assetAllocationEmergingMarkets;
}
