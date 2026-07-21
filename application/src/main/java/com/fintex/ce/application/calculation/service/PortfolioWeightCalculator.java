package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter.Conversion;
import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter.CurrencyValue;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static java.math.BigDecimal.ZERO;

/**
 * Computes per-holding portfolio weights normalized to the default target currency. Holding values denominated in
 * non-target currencies are converted via {@link DefaultTargetCurrencyConverter} before the weight denominator is
 * formed; holdings without a known source currency, or with a currency that lacks an FX rate, fall back to their raw
 * value (with a warning emitted by the converter). When the normalized total is zero the calculator falls back to raw
 * value weights so that downstream consumers still get a valid weight distribution.
 */
@Service
@RequiredArgsConstructor
public class PortfolioWeightCalculator {

  private final DefaultTargetCurrencyConverter currencyConverter;

  public Result compute(List<PortfolioHolding> holdings, Map<PortfolioHolding, Currency> currencies) {
    Map<PortfolioHolding, CurrencyValue> input = HashMap.newHashMap(holdings.size());
    for (PortfolioHolding holding : holdings) {
      BigDecimal value = holding.getValue() == null ? ZERO : holding.getValue();
      input.put(holding, new CurrencyValue(currencies.get(holding), value));
    }
    Conversion conversion = currencyConverter.convert(input);
    List<Notification> warnings = new ArrayList<>(conversion.warnings());

    Map<PortfolioHolding, BigDecimal> normalized = HashMap.newHashMap(holdings.size());
    BigDecimal totalNormalized = ZERO;
    for (PortfolioHolding holding : holdings) {
      BigDecimal converted = conversion.converted().get(holding);
      if (converted == null) {
        BigDecimal raw = holding.getValue();
        converted = raw == null ? ZERO : raw;
      }
      normalized.put(holding, converted);
      totalNormalized = totalNormalized.add(converted);
    }

    if (totalNormalized.signum() == 0) {
      return new Result(calculateInitialPortfolioWeight(holdings), warnings);
    }
    Map<PortfolioHolding, BigDecimal> weights = HashMap.newHashMap(holdings.size());
    for (PortfolioHolding holding : holdings) {
      weights.put(holding, DecimalUtils.divide(normalized.get(holding), totalNormalized));
    }
    return new Result(weights, warnings);
  }

  public record Result(Map<PortfolioHolding, BigDecimal> weights, List<Notification> warnings) {
  }
}
