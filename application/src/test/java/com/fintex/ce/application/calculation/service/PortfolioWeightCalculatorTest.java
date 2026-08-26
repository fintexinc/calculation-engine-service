package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortfolioWeightCalculatorTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final PortfolioWeightCalculator calculator = new PortfolioWeightCalculator(
      new HoldingCurrencyConverter(fxRateService, new FxProperties()));

  @Test
  void shouldWeightConvertedValues_whenPortfolioContainsMultipleCurrencies() {
    PortfolioHolding cadHolding = etfCa("CAD", 100);
    PortfolioHolding usdHolding = etfCa("USD", 100);
    when(fxRateService.spotRates(eq(Set.of(Currency.CAD, Currency.USD)), eq(Currency.CAD), any(LocalDate.class)))
        .thenReturn(Map.of(Currency.CAD, BigDecimal.ONE, Currency.USD, new BigDecimal("1.5")));

    PortfolioWeightCalculator.Result result = calculator.compute(List.of(cadHolding, usdHolding),
        Map.of(cadHolding, Currency.CAD, usdHolding, Currency.USD));

    assertThat(result.weights()).hasSize(2);
    assertThat(result.weights().get(cadHolding)).isEqualByComparingTo("0.4");
    assertThat(result.weights().get(usdHolding)).isEqualByComparingTo("0.6");
    assertThat(result.warnings()).isEmpty();
  }

  @Test
  void shouldUseRawValueAndAddWarning_whenFxRateIsUnavailable() {
    PortfolioHolding cadHolding = etfCa("CAD", 100);
    PortfolioHolding usdHolding = etfCa("USD", 100);
    Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
    rates.put(Currency.CAD, BigDecimal.ONE);
    rates.put(Currency.USD, null);
    when(fxRateService.spotRates(eq(Set.of(Currency.CAD, Currency.USD)), eq(Currency.CAD), any(LocalDate.class)))
        .thenReturn(rates);

    PortfolioWeightCalculator.Result result = calculator.compute(List.of(cadHolding, usdHolding),
        Map.of(cadHolding, Currency.CAD, usdHolding, Currency.USD));

    assertThat(result.weights().get(cadHolding)).isEqualByComparingTo("0.5");
    assertThat(result.weights().get(usdHolding)).isEqualByComparingTo("0.5");
    assertThat(result.warnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.FX_RATES_UNAVAILABLE);
      assertThat(warning.getMessage()).isEqualTo(
          "FX rates unavailable for holding " + usdHolding.getIdsString() + ": USD -> CAD");
      assertThat(warning.getMetadata()).isEqualTo(Map.of(
          "holdingId", usdHolding.getIdsString(),
          "param-1", usdHolding.getIdsString(),
          "param-2", Currency.USD,
          "param-3", Currency.CAD));
    });
  }

}
