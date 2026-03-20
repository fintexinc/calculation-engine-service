package com.fintex.ce.dto.calculation;

import com.fintex.ce.application.dto.calculation.WeightedAverageInputDTO;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.Rebalanced;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.FxRates;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class WeightedAverageInputDtoTest {

  @Test
  void shouldMakeCopy_whenCheckResult() {
    // SETUP
    final var expected = new WeightedAverageInputDTO();
    final var fxRates = Map.of(LocalDate.MIN, mock(FxRates.FxRate.class));
    final var holdings = Map.of(mock(Holding.class), Currency.CAD);
    final var portfolioReturns = Map.of(mock(Holding.class),
        Map.of(LocalDate.MIN, mock(BigDecimal.class)));

    expected.setCipsd(LocalDate.MIN);
    expected.setCommonDates(mock(CommonDates.class));
    expected.setCurrency(Currency.CAD);
    expected.setFxRates(fxRates);
    expected.setHoldings(holdings);
    expected.setPortfolioReturns(portfolioReturns);
    expected.setRebalanced(Rebalanced.ANNUALLY);

    // ACT
    final WeightedAverageInputDTO actual = expected.makeCopy();

    // VERIFY
    assertEquals(expected, actual);
  }
}