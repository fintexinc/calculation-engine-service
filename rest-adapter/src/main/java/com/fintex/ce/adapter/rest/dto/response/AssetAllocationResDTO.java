package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.core.Warning;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Response for asset-allocations metric. Contains portfolio asset allocation breakdown by region.")
public class AssetAllocationResDTO extends WarningDTO {

  @Schema(description = "Asset allocation percentages by region")
  private Map<AssetAllocationRegionType, BigDecimal> assetAllocation;

  public AssetAllocationResDTO() {
  }

  public AssetAllocationResDTO(Map<AssetAllocationRegionType, BigDecimal> assetAllocation, List<Warning> warnings) {
    super(warnings);
    this.assetAllocation = assetAllocation;
  }

}
