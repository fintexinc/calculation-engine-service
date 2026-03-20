package com.fintex.ce.domain.model.result.correlation;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.*;
import com.fintex.sm.model.domain.SecurityIdentifier;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HoldingsKeyResult {

  private HoldingType type;
  private SecurityIdentifier securityIdentifier;
  private String fundServCode;
  private String ticker;
  private String exchangeCode;
  private String key;
  private BigDecimal allocation;
  private String name;
  private Currency currency;
  private String morningstarId;

  public static HoldingsKeyResult buildHoldingsKeyResult(final Holding holding) {
    return buildFromHolding(holding, null);
  }

  public static HoldingsKeyResult buildFromHolding(final Holding holding, final BigDecimal allocation) {
    HoldingsKeyResult result = new HoldingsKeyResult();
    HoldingType holdingType = holding.getType();
    result.setType(holdingType);
    result.setSecurityIdentifier(holding.getSecurityIdentifier());
    if (holdingType == HoldingType.CANADA_MUTUAL_FUNDS || holdingType == HoldingType.SEGREGATED_FUND_CANADA) {
      result.setFundServCode(((FundSeriesHolding) holding).getFundServCode());
    } else if (holdingType == HoldingType.US_ETF || holdingType == HoldingType.CANADA_ETF) {
      result.setTicker(((EtfHolding) holding).getTicker());
      result.setExchangeCode(((EtfHolding) holding).getExchangeCode());
    } else if (holdingType == HoldingType.US_STOCKS || holdingType == HoldingType.CANADA_STOCKS) {
      result.setTicker(((StockHolding) holding).getTicker());
      result.setExchangeCode(((StockHolding) holding).getExchangeCode());
    } else if (holdingType == HoldingType.CASH) {
      result.setCurrency(((CashHolding) holding).getCurrency());
    } else if (holdingType == HoldingType.GIC) {
      result.setName(((GicHolding) holding).getName());
    } else if (holdingType == HoldingType.CANADA_POOLED_FUNDS) {
      result.setMorningstarId(((CanadaPooledFundHolding) holding).getMorningstarId());
    } else if (holdingType == HoldingType.CANADA_HEDGE_FUNDS) {
      result.setMorningstarId(((CanadaHedgeFundHolding) holding).getMorningstarId());
    } else if (holdingType == HoldingType.US_MUTUAL_FUNDS) {
      result.setTicker(((UsMutualFundHolding) holding).getTicker());
    } else if (holdingType == HoldingType.SEPARATELY_MANAGED_ACCOUNT) {
      result.setTicker(((SmaHolding) holding).getIdentifier());
    }
    result.setKey(holding.generateUserIdentifier());
    result.setAllocation(allocation);
    return result;
  }
}
