package ca.tangerine.pce.model.domain.result.holding;

import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ca.tangerine.pce.model.domain.result.correlation.HoldingsKeyResult;
import ca.tangerine.wm.commons.domain.holding.HoldingType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

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
  private HoldingType holdingType;
  private BigDecimal allocation;
  private int numOfFunds;
  private Set<HoldingsKeyResult> parentHolding;
}
