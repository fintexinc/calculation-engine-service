package com.fintex.ce.port.output.sm.dto;

import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for Asset Allocation data.
 * Uses FinancialInstrumentType instead of deprecated HoldingType.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AssetAllocationDto {

  private FinancialInstrumentType holdingType;
  private Map<String, BigDecimal> assetAllocation;
  private String holdingId;
  private String provider;
}
