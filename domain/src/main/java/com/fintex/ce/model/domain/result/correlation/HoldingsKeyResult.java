package com.fintex.ce.model.domain.result.correlation;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HoldingsKeyResult {

  private FinancialInstrumentType type;
  private SecurityIdentifier securityIdentifier;
  private String key;
  private BigDecimal allocation;
  private String name;
  private Currency currency;

  public static HoldingsKeyResult buildHoldingsKeyResult(final PortfolioHolding holding) {
    return buildFromHolding(holding, null);
  }

  public static HoldingsKeyResult buildFromHolding(final PortfolioHolding holding, final BigDecimal allocation) {
    HoldingsKeyResult result = new HoldingsKeyResult();
    result.setType(holding.getHoldingType());
    result.setSecurityIdentifier(holding.getSecurityIdentifier());

    if (FinancialInstrumentType.CASH.equals(holding.getHoldingType())) {
      result.setCurrency(((CashHolding) holding).getCurrency());
    } else if (FinancialInstrumentType.GIC.equals(holding.getHoldingType())) {
      result.setName(((GicHolding) holding).getName());
    }

    result.setKey(createKey(holding));
    result.setAllocation(allocation);
    return result;
  }

  private static String createKey(final PortfolioHolding holding) {
    String result;
    SecurityIdentifier secId = holding.getSecurityIdentifier();

    if (FinancialInstrumentType.CASH.equals(holding.getHoldingType())) {
      CashHolding cashHolding = (CashHolding) holding;
      result = cashHolding.getCurrency() != null ? cashHolding.getCurrency().name() : "";
    } else if (secId instanceof EquitySecurityIdentifier eqId) {
      result = secId.getId() + "_" + eqId.getExchangeId();
    } else {
      result = secId != null ? secId.getId() : "";
    }
    return holding.getHoldingType() + "_" + result;
  }
}
