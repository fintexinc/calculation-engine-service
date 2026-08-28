package ca.tangerine.pce.model.domain.result.allocation;

import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.wm.commons.domain.allocation.AssetAllocationRegionType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
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
@Schema(description = "Response for asset-allocations metric. Contains portfolio asset allocation breakdown by region.")
public class AssetAllocationResult extends BaseCalculationResult {

  @Schema(description = "Asset allocation percentages by type")
  private Map<AssetAllocationRegionType, BigDecimal> assetAllocation;
}
