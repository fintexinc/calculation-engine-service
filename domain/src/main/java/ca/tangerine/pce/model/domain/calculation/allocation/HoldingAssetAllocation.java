package ca.tangerine.pce.model.domain.calculation.allocation;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.allocation.AssetAllocationRegionType;
import ca.tangerine.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Per-security asset allocation breakdown plus the currency in which the underlying values are quoted. The currency is
 * sourced from the MIC asset-allocation response (AssetAllocationWithCurrency) and is used by callers that need to
 * normalize holding market values across currencies before weighting.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HoldingAssetAllocation extends BaseCalculationData {

  private Map<AssetAllocationRegionType, BigDecimal> allocations;
  private Currency currency;
}
