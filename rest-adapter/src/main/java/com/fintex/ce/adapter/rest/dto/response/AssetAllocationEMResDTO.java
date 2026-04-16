package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegionEmType;
import com.fintex.ce.model.error.Warning;

import com.fasterxml.jackson.annotation.JsonProperty;

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
@Schema(description = "Response for asset-allocations-em metric. Contains portfolio asset allocation breakdown by emerging markets region.")
public class AssetAllocationEMResDTO extends WarningDTO {

  @Schema(description = "Asset allocation percentages by emerging markets region")
  @JsonProperty("assetAllocationEmergingMarkets")
  Map<AssetAllocationRegionEmType, BigDecimal> assetAllocationEmergingMarkets;

  public AssetAllocationEMResDTO() {

  }

  public AssetAllocationEMResDTO(Map<AssetAllocationRegionEmType, BigDecimal> assetAllocationEmergingMarkets,
      List<Warning> warnings) {
    super(warnings);
    this.assetAllocationEmergingMarkets = assetAllocationEmergingMarkets;
  }
}
