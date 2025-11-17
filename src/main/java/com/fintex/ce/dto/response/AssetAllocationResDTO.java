package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegionType;
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
public class AssetAllocationResDTO extends WarningDTO {

    private Map<AssetAllocationRegionType, BigDecimal> assetAllocation;

    public AssetAllocationResDTO() {
    }

    public AssetAllocationResDTO(Map<AssetAllocationRegionType, BigDecimal> assetAllocation, List<Warning> warnings) {
        super(warnings);
        this.assetAllocation = assetAllocation;
    }

}
