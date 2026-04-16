package com.fintex.ce.domain.dto;

import com.fintex.ce.domain.dto.calculation.WeightedAverageInputDTO;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.CurrencyExchangePair;
import com.fintex.ce.domain.model.enumeration.Rebalanced;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class WeightedAverageInputDtoTest {

  @Test
  void shouldMakeCopy_whenCheckResult() {
    final var expected = new WeightedAverageInputDTO();
    NavigableMap<LocalDate, BigDecimal> rates = new TreeMap<>(Map.of(LocalDate.MIN, BigDecimal.ONE));
    final var fxRates = Map.of(new CurrencyExchangePair(CurrencyType.USD, CurrencyType.CAD), rates);
    final var holdings = Map.of(mock(Holding.class), CurrencyType.CAD);
    final var portfolioReturns = Map.of(mock(Holding.class),
        Map.of(LocalDate.MIN, mock(BigDecimal.class)));

    expected.setCipsd(LocalDate.MIN);
    expected.setCommonDates(mock(CommonDates.class));
    expected.setCurrency(CurrencyType.CAD);
    expected.setFxRates(fxRates);
    expected.setHoldings(holdings);
    expected.setPortfolioReturns(portfolioReturns);
    expected.setRebalanced(Rebalanced.ANNUALLY);

    final WeightedAverageInputDTO actual = expected.makeCopy();

    assertEquals(expected, actual);
  }
}