package com.fintex.ce.adapter.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionEmType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AssetAllocationEMResDTO extends WarningDTO {

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
