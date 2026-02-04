package com.fintex.ce.util;

import com.fintex.smclient.dto.FxRatesDTO;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.IncomeForecastDto;
import com.fintex.ce.domain.model.holding.*;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

public class PortfolioUtils {

  private PortfolioUtils() {
  }

  public static Map<Holding, BigDecimal> calculateInitialPortfolioWeight(final Collection<Holding> holdings) {
    final BigDecimal sum = holdings.stream().map(Holding::getValue).reduce(ZERO, BigDecimal::add);
    return holdings.stream().collect(toMap(e -> e, e -> DecimalUtils.divide(e.getValue(), sum)));
  }

  public static Map<Holding, Map<LocalDate, BigDecimal>> fxRatesForHoldings(
      final Map<Holding, Currency> holdings, final Currency toCurrency, final Map<LocalDate, FxRatesDTO> fxRates) {
    return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> fxRatesForHolding(fxRates, entry
        .getValue(), toCurrency)));
  }

  public static Map<LocalDate, BigDecimal> fxRatesForHolding(final Map<LocalDate, FxRatesDTO> fxRates,
      final Currency from, final Currency to) {
    return fxRates.entrySet().stream().collect(CollectorUtils.toTreeMap(Map.Entry::getKey, mapFxRateBasedOnCurrency(
        from, to)));
  }

  private static Function<Map.Entry<LocalDate, FxRatesDTO>, BigDecimal> mapFxRateBasedOnCurrency(final Currency from,
      final Currency to) {
    return entry -> {
      if (Currency.USD.equals(from) && Currency.CAD.equals(to)) {
        return entry.getValue().getUsdCad();
      } else if (Currency.CAD.equals(from) && Currency.USD.equals(to)) {
        return entry.getValue().getCadUsd();
      } else
        if (Currency.CAD.equals(from) && Currency.CAD.equals(to) || Currency.USD.equals(from) && Currency.USD.equals(
            to)) {
              return ONE;
            }
      final String message = String.format("Currency exchange %s->%s not supported", from, to);
      throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    };
  }

  public static void setHoldingResponseDetails(final Holding holding,
      final IncomeForecastDto incomeForecastDTO) {
    if (FilterUtils.STOCK_PREDICATE.test(holding)) {
      final StockHolding stockHolding = (StockHolding) holding;
      incomeForecastDTO.setExchangeCode(stockHolding.getExchangeCode());
      incomeForecastDTO.setTicker(stockHolding.getTicker());
    } else if (FilterUtils.CANADA_MUTUAL_PREDICATE.test(holding)) {
      final FundSeriesHolding fundSeriesHolding = (FundSeriesHolding) holding;
      incomeForecastDTO.setFundServeCode(fundSeriesHolding.getFundServCode());
    } else if (FilterUtils.US_MUTUAL_FUND_PREDICATE.test(holding)) {
      final UsMutualFundHolding usMutualFundHolding = (UsMutualFundHolding) holding;
      incomeForecastDTO.setTicker(usMutualFundHolding.getTicker());
    } else if (FilterUtils.ETF_PREDICATE.test(holding)) {
      final EtfHolding etfHolding = (EtfHolding) holding;
      incomeForecastDTO.setTicker(etfHolding.getTicker());
      incomeForecastDTO.setExchangeCode(etfHolding.getExchangeCode());
    } else if (FilterUtils.FIXED_INCOME_PREDICATE.test(holding)) {
      final FixedIncomeHolding fixedIncomeHolding = (FixedIncomeHolding) holding;
      incomeForecastDTO.setIdentifier(fixedIncomeHolding.getIdentifier());
    }
  }

  /**
   * return true if no holdings in the portfolio contain any values
   *
   * @param map
   * @param <T>
   * @return
   */
  public static <T> boolean areAllValuesInMapEmpty(final Map<Holding, Map<T, BigDecimal>> map) {
    for (final Map.Entry<Holding, Map<T, BigDecimal>> entry : map.entrySet()) {
      if (!CollectionUtils.isEmpty(entry.getValue())) {
        return false;
      }
    }
    return true;
  }

  /**
   * return true if all holdings in the portfolio contain BigDecimal.ZERO values
   *
   * @param map
   *          contains Map with values for each Holding type
   * @param <T>
   *          generic key
   * @return
   */
  public static <T> boolean areAllValuesZerosInMap(final Map<Holding, Map<T, BigDecimal>> map) {
    return map.values().stream().flatMap(e -> e.values().stream()).allMatch(v -> v.compareTo(ZERO) == 0);
  }

  public static String createKey(final Holding holding) {
    var result = "";
    if (FilterUtils.CANADA_MUTUAL_PREDICATE.test(holding)) {
      final FundSeriesHolding fundSeriesHolding = (FundSeriesHolding) holding;
      result = fundSeriesHolding.getFundServCode();
    } else if (FilterUtils.ETF_PREDICATE.test(holding)) {
      final EtfHolding etfHolding = (EtfHolding) holding;
      result = etfHolding.getTicker();
    } else if (FilterUtils.STOCK_PREDICATE.test(holding)) {
      final StockHolding stockHolding = (StockHolding) holding;
      result = stockHolding.getTicker() + "_" + stockHolding.getExchangeCode();
    } else if (FilterUtils.BENCHMARKS_PREDICATE.test(holding)) {
      final BenchmarkIndexHolding benchmarkIndexHolding = (BenchmarkIndexHolding) holding;
      result = benchmarkIndexHolding.getMrStarId();
    } else if (FilterUtils.CASH_PREDICATE.test(holding)) {
      final CashHolding cashHolding = (CashHolding) holding;
      result = cashHolding.getCurrency().name();
    } else if (FilterUtils.CANADA_POOLED_FUND_PREDICATE.test(holding)) {
      final CanadaPooledFundHolding canadaPooledFundHolding = (CanadaPooledFundHolding) holding;
      result = canadaPooledFundHolding.getMorningstarId();
    } else if (FilterUtils.CANADA_HEDGE_FUND_PREDICATE.test(holding)) {
      final CanadaHedgeFundHolding canadaHedgeFundHolding = (CanadaHedgeFundHolding) holding;
      result = canadaHedgeFundHolding.getMorningstarId();
    } else if (FilterUtils.US_MUTUAL_FUND_PREDICATE.test(holding)) {
      final UsMutualFundHolding usMutualFundHolding = (UsMutualFundHolding) holding;
      result = usMutualFundHolding.getTicker();
    } else if (FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE.test(holding)) {
      final SmaHolding smaHolding = (SmaHolding) holding;
      result = smaHolding.getIdentifier();
    }
    return holding.getType() + "_" + result;
  }

}
