package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.response.MeanResDTO;
import com.fintex.ce.util.CalculationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeanCalculationTest {

    @Test
    void getPeriodStartDateWithOneMonthOffset_verifyGetPeriodStartDate() {
        try (var util = Mockito.mockStatic(CalculationUtils.class)) {
            //SETUP
            final var growth10K = mock(TreeMap.class);
            final var sut = mock(MeanCalculation.class);
            final var returns = mock(NavigableMap.class);
            final var periodStartDate = mock(LocalDate.class);
            final var portfolioTotalReturnsByPeriod = mock(SortedMap.class);

            final var numberOfMonths = 12;
            final var nowDate = LocalDate.now();

            when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);
            when(sut.getPortfolioTotalReturns()).thenReturn(returns);
            when(returns.size()).thenReturn(15);
            when(sut.getPeriodStartDate(Mockito.anyInt(), Mockito.any(NavigableMap.class))).thenReturn(periodStartDate);
            when(sut.getSubMapByPeriodStartDate(Mockito.any(), Mockito.any())).thenReturn(portfolioTotalReturnsByPeriod);

            util.when(() -> CalculationUtils.average(Mockito.any())).thenReturn(BigDecimal.ONE);
            doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

            //ACT
            final BigDecimal result = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

            //VERIFY
            Assertions.assertNotNull(result);
            Assertions.assertEquals(BigDecimal.ONE, result);
        }
    }

    @Test
    void defineResponseType() {
        //SETUP
        final MeanCalculation<MeanResDTO> sut = mock(MeanCalculation.class);
        final var results = mock(Set.class);
        final var timeIntervals = mock(Set.class);


        when(sut.formTimeIntervalResDTO(results)).thenReturn(timeIntervals);

        doCallRealMethod().when(sut).defineResponseType(any());

        //ACT
        final MeanResDTO result = sut.defineResponseType(results);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(timeIntervals, result.getMean());

    }

}
