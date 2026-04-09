package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.calculation.AssetAllocationRegionType;

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
public class AssetAllocationResult extends WarningResult {

  private Map<AssetAllocationRegionType, BigDecimal> assetAllocation;
}
