package com.fintex.ce.domain.model.result.correlation;

import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

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
  private CurrencyType currency;

  public static HoldingsKeyResult buildHoldingsKeyResult(final Holding holding) {
    return buildFromHolding(holding, null);
  }

  public static HoldingsKeyResult buildFromHolding(final Holding holding, final BigDecimal allocation) {
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

  private static String createKey(final Holding holding) {
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
