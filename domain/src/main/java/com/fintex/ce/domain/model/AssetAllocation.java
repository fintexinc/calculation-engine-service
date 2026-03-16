package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.core.ProviderAware;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @deprecated Use domain models with FinancialInstrumentType instead
 */
@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AssetAllocation implements ProviderAware {

  private HoldingType holdingType;
  private Map<String, BigDecimal> assetAllocation;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public AssetAllocation(HoldingType holdingType, Map<String, BigDecimal> assetAllocation) {
    this.holdingType = holdingType;
    this.assetAllocation = assetAllocation;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
