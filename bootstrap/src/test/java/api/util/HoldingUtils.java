package api.util;

import api.config.constant.HoldingGroups;
import api.exception.TestException;
import api.model.CashHoldingDTO;
import api.model.HoldingDataDTO;
import com.fintex.ce.domain.dto.IncomeForecastDto;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.util.FilterUtils;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import static com.fintex.ce.domain.model.holding.GicHolding.DEFAULT_START_DATE;

public class HoldingUtils {

  private HoldingUtils() {
  }

  public static Holding createAppSpecificHoldingBasedOnType(final String holdingName, final BigDecimal holdingValue,
      HoldingDataDTO holdingDataDTO) {
    Holding holding = defineHolding(holdingName, holdingDataDTO);

    holding.setSecurityIdentifier(holdingDataDTO.getSecurityIdentifier());

    holding.setType(holdingDataDTO.getHoldingType());
    holding.setValue(holdingValue);

    return holding;
  }

  public static Holding defineHolding(String holdingName, HoldingDataDTO holdingDataDTO) {
    final HoldingType type = holdingDataDTO.getHoldingType();
    if (HoldingGroups.MUTUAL_FUND.contains(type)) {
      FundSeriesHolding h = new FundSeriesHolding();
      h.setFundServCode(holdingName);
      return h;
    } else if (HoldingGroups.ETFS.contains(type)) {
      EtfHolding h = new EtfHolding();
      h.setTicker(holdingName);
      h.setExchangeCode("");
      return h;
    } else if (HoldingGroups.STOCKS.contains(type)) {
      StockHolding h = new StockHolding();
      h.setTicker(holdingName);
      h.setExchangeCode(holdingDataDTO.getExchangeCode());
      return h;
    } else if (HoldingGroups.CASH.contains(type)) {
      CashHoldingDTO h = new CashHoldingDTO();
      h.setHoldingCode(holdingDataDTO.getHoldingCode());
      h.setCurrency(mapCashNameToCurrency(holdingName));
      return h;
    } else if (HoldingGroups.GIC.contains(type)) {
      GicHolding h = new GicHolding();
      if (Objects.nonNull(holdingDataDTO.getGicInvestmentDate())) {
        h.setInvestmentDate(holdingDataDTO.getGicInvestmentDate());
      } else {
        h.setInvestmentDate(DEFAULT_START_DATE);
      }
      if (Objects.nonNull(holdingDataDTO.getGicTerm())) {
        h.setTerm(holdingDataDTO.getGicTerm());
      } else {
        h.setTerm(BigDecimal.ZERO);
      }
      h.setCurrency(mapGicNameToCurrency(holdingName));
      h.setClientIntRate(holdingDataDTO.getGicClientIntRate());
      h.setInterestFreq(holdingDataDTO.getGicInterestFreq());
      h.setName(holdingDataDTO.getGicName());
      return h;
    } else if (HoldingGroups.CANADA_POOLED_FUNDS.contains(type)) {
      CanadaPooledFundHolding h = new CanadaPooledFundHolding();
      h.setMorningstarId(holdingName);
      return h;
    } else if (HoldingGroups.CANADA_HEDGE_FUNDS.contains(type)) {
      CanadaHedgeFundHolding h = new CanadaHedgeFundHolding();
      h.setMorningstarId(holdingName);
      return h;
    } else if (HoldingGroups.US_MUTUAL_FUNDS.contains(type)) {
      UsMutualFundHolding h = new UsMutualFundHolding();
      h.setTicker(holdingName);
      return h;
    }
    throw new TestException("There is no such holding type as: " + type);
  }

  public static void setHoldingResponseDetails(Map.Entry<Holding, IncomeForecast> entry,
      IncomeForecastDto incomeForecastDTO) {
    if (FilterUtils.STOCK_PREDICATE.test(entry.getKey())) {
      StockHolding stockHolding = (StockHolding) entry.getKey();
      incomeForecastDTO.setExchangeCode(stockHolding.getExchangeCode());
      incomeForecastDTO.setTicker(stockHolding.getTicker());
    } else if (FilterUtils.CANADA_MUTUAL_PREDICATE.test(entry.getKey())) {
      FundSeriesHolding fundSeriesHolding = (FundSeriesHolding) entry.getKey();
      incomeForecastDTO.setFundServeCode(fundSeriesHolding.getFundServCode());
    } else if (FilterUtils.US_MUTUAL_FUND_PREDICATE.test(entry.getKey())) {
      UsMutualFundHolding usMutualFundHolding = (UsMutualFundHolding) entry.getKey();
      incomeForecastDTO.setTicker(usMutualFundHolding.getTicker());
    } else if (FilterUtils.ETF_PREDICATE.test(entry.getKey())) {
      EtfHolding etfHolding = (EtfHolding) entry.getKey();
      incomeForecastDTO.setTicker(etfHolding.getTicker());
      incomeForecastDTO.setExchangeCode(etfHolding.getExchangeCode());
    }
  }

  private static Currency mapCashNameToCurrency(String holdingName) {
    switch (holdingName) {
      case "CASH.U" :
        return Currency.USD;
      case "CASH.C" :
        return Currency.CAD;
      default :
        return null;
    }
  }

  private static Currency mapGicNameToCurrency(String holdingName) {
    switch (holdingName) {
      case "GIC.U" :
        return Currency.USD;
      case "GIC.C" :
        return Currency.CAD;
      default :
        return null;
    }
  }

}
