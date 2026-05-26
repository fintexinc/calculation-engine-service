package com.fintex.ce.model.domain.result.holding;

import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopCommonHoldingData {

  private String name;
  /**
   * Primary identifier of the holding. Source priority: MORNINGSTAR_ID → TICKER → FUNDSERV → ISIN → CUSIP. May be null
   * when the data provider returned no identifiers for the security.
   */
  private SecurityIdentifier identifier;
  private String holdingType;
  private BigDecimal allocation;
  private int numOfFunds;
  private Set<HoldingsKeyResult> parentHolding;
}
