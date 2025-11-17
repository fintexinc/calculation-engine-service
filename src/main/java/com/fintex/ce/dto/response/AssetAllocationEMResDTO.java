package com.fintex.ce.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegionEmType;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
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

    public AssetAllocationEMResDTO(Map<AssetAllocationRegionEmType, BigDecimal> assetAllocationEmergingMarkets, List<Warning> warnings) {
        super(warnings);
        this.assetAllocationEmergingMarkets = assetAllocationEmergingMarkets;
    }
}
