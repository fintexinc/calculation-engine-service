package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionType;
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
public class AssetAllocationResDTO extends WarningDTO {

  private Map<AssetAllocationRegionType, BigDecimal> assetAllocation;

  public AssetAllocationResDTO() {
  }

  public AssetAllocationResDTO(Map<AssetAllocationRegionType, BigDecimal> assetAllocation, List<Warning> warnings) {
    super(warnings);
    this.assetAllocation = assetAllocation;
  }

}
