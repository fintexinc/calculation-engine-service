package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_MFR_001;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.divide;
import static java.math.BigDecimal.ONE;

public class FxRatesConversionComponent {

  public Map<Holding, TreeMap<LocalDate, BigDecimal>> convert(
      final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns,
      final Map<Holding, Currency> holdingCurrencies,
      final Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates,
      final Currency toCurrency) {
    return returns.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> {
      Currency fromCurrency = holdingCurrencies.get(entry.getKey());
      if (fromCurrency == null || fromCurrency.equals(toCurrency)) {
        return entry.getValue();
      }
      NavigableMap<LocalDate, BigDecimal> rates = fxRates.get(new CurrencyExchangePair(fromCurrency, toCurrency));
      if (rates == null) {
        return entry.getValue();
      }
      return holdingPortfolioBaseTotalReturn(rates, entry.getValue());
    }));
  }

  private TreeMap<LocalDate, BigDecimal> holdingPortfolioBaseTotalReturn(
      final NavigableMap<LocalDate, BigDecimal> fxRates,
      final Map<LocalDate, BigDecimal> pReturns) {
    return pReturns.entrySet().stream().collect(toTreeMap(Map.Entry::getKey,
        entry -> holdingPortfolioBaseTotalReturnFormula(entry.getKey(), entry.getValue(), fxRates)));
  }

  private BigDecimal holdingPortfolioBaseTotalReturnFormula(final LocalDate date, final BigDecimal value,
      final NavigableMap<LocalDate, BigDecimal> fxRates) {
    final LocalDate previousDate = toLastDayOfMonth(date.minusMonths(1));
    final BigDecimal fxRateValue = validateFxRates(date, lookupRate(fxRates, date));
    final BigDecimal previousFxValue = validateFxRates(previousDate, lookupRate(fxRates, previousDate));
    final BigDecimal subtract = ONE.add(value).multiply(divide(fxRateValue, previousFxValue)).subtract(ONE);
    return subtract.multiply(HUNDRED);
  }

  private BigDecimal lookupRate(NavigableMap<LocalDate, BigDecimal> rates, LocalDate date) {
    var entry = rates.floorEntry(date);
    return entry != null ? entry.getValue() : null;
  }

  private BigDecimal validateFxRates(final LocalDate date, final BigDecimal fxRateValue) {
    if (fxRateValue == null) {
      throw ERR_RRC_MFR_001.error(date);
    }
    return fxRateValue;
  }
}
