package com.fintex.ce.model.domain.calculation.holding;

import com.fintex.wm.commons.domain.holding.HoldingType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonHolding {

  public static final HoldingType EQUITY_TYPE = HoldingType.E;

  private String name;
  private String companyName;
  /** Provider's holding-type code; null when SM saw a code outside the {@link HoldingType} vocabulary. */
  private HoldingType type;
  private BigDecimal value;
  private List<CommonHolding> underlyingHoldings;
  /**
   * Primary identifier of this holding, chosen by the mapper from the SM payload using the priority MORNINGSTAR_ID →
   * TICKER → FUNDSERV → ISIN → CUSIP. Null only when SM returned no identifiers at all.
   */
  private SecurityIdentifier primaryIdentifier;
  /** SM-provided weighting ratio for this node, as returned by the data provider. Never written by the calculation. */
  private BigDecimal weight;

  public HoldingAggregator aggregator() {
    if (type == EQUITY_TYPE && companyName != null && !companyName.isEmpty()) {
      return new HoldingAggregator(null, companyName);
    }
    return new HoldingAggregator(name, null);
  }

}
