package com.fintex.ce.application.util;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TBillsValidatorTest {

  @Test
  void shouldReturnSameSeries_whenTBillRatesAreAvailable() {
    NavigableMap<LocalDate, BigDecimal> rates = new TreeMap<>(
        Map.of(LocalDate.parse("2024-01-31"), new BigDecimal("0.04")));

    NavigableMap<LocalDate, BigDecimal> result = TBillsValidator.requireNonEmpty(rates, Currency.CAD);

    assertThat(result).isSameAs(rates);
    assertThat(result).containsExactlyEntriesOf(rates);
  }

  @ParameterizedTest
  @MethodSource("unavailableSeries")
  void shouldThrowCurrencySpecificError_whenTBillSeriesIsUnavailable(NavigableMap<LocalDate, BigDecimal> rates) {
    assertThatThrownBy(() -> TBillsValidator.requireNonEmpty(rates, Currency.CAD))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY);
          assertThat(exception).hasMessage("T-Bill rates are not available for currency CAD");
          assertThat(exception.getMetadata()).isEqualTo(Map.of("param-1", Currency.CAD));
        });
  }

  private static Stream<NavigableMap<LocalDate, BigDecimal>> unavailableSeries() {
    return Stream.of(null, new TreeMap<>());
  }
}
