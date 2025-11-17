package com.fintex.ce.dto.calculation;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.Rebalanced;
import com.fintex.ce.dto.CommonDates;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.smclient.dto.FxRatesDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class WeightedAverageInputDtoTest {

    @Test
    void makeCopy_checkResult() {
        //SETUP
        final var expected = new WeightedAverageInputDTO();
        final var fxRates = Map.of(LocalDate.MIN, mock(FxRatesDTO.class));
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

        //ACT
        final WeightedAverageInputDTO actual = expected.makeCopy();

        //VERIFY
        assertEquals(expected, actual);
    }
}