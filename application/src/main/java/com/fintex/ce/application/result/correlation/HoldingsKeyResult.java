package com.fintex.ce.application.result.correlation;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.*;
import com.fintex.ce.util.FilterUtils;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.fintex.ce.util.PortfolioUtils.createKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HoldingsKeyResult {

  private HoldingType type;
  private HoldingIdentifierType holdingIdentifier;
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
    result.setType(holding.getType());
    result.setHoldingIdentifier(holding.getHoldingIdentifier());
    if (FilterUtils.CANADA_MUTUAL_PREDICATE.test(holding)) {
      result.setFundServCode(((FundSeriesHolding) holding).getFundServCode());
    } else if (FilterUtils.ETF_PREDICATE.test(holding)) {
      result.setTicker(((EtfHolding) holding).getTicker());
      result.setExchangeCode(((EtfHolding) holding).getExchangeCode());
    } else if (FilterUtils.STOCK_PREDICATE.test(holding)) {
      result.setTicker(((StockHolding) holding).getTicker());
      result.setExchangeCode(((StockHolding) holding).getExchangeCode());
    } else if (FilterUtils.CASH_PREDICATE.test(holding)) {
      result.setCurrency(((CashHolding) holding).getCurrency());
    } else if (FilterUtils.GIC_PREDICATE.test(holding)) {
      result.setName(((GicHolding) holding).getName());
    } else if (FilterUtils.CANADA_POOLED_FUND_PREDICATE.test(holding)) {
      result.setMorningstarId(((CanadaPooledFundHolding) holding).getMorningstarId());
    } else if (FilterUtils.CANADA_HEDGE_FUND_PREDICATE.test(holding)) {
      result.setMorningstarId(((CanadaHedgeFundHolding) holding).getMorningstarId());
    } else if (FilterUtils.US_MUTUAL_FUND_PREDICATE.test(holding)) {
      result.setTicker(((UsMutualFundHolding) holding).getTicker());
    } else if (FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE.test(holding)) {
      result.setTicker(((SmaHolding) holding).getIdentifier());
    }
    result.setKey(createKey(holding));
    result.setAllocation(allocation);
    return result;
  }
}
