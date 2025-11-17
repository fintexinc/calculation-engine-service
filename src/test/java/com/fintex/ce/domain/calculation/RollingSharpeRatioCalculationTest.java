package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.RollingIntervalResDTO;
import com.fintex.ce.dto.response.RollingSharpeRatioResDTO;
import com.fintex.ce.dto.response.core.IntervalResDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.config.constant.BigDecimalConstants.*;
import static java.math.BigDecimal.TEN;
import static org.mockito.Mockito.*;

class RollingSharpeRatioCalculationTest {

    private static NavigableMap<LocalDate, BigDecimal> portfolioReturns;

    @BeforeAll
    static void setUp() {
        portfolioReturns = new TreeMap<>();
        portfolioReturns.put(LocalDate.now().minusMonths(12), HUNDRED);
        portfolioReturns.put(LocalDate.now().minusMonths(11), TEN);
        portfolioReturns.put(LocalDate.now().minusMonths(10), HUNDRED);
        portfolioReturns.put(LocalDate.now().minusMonths(9), ONE);
        portfolioReturns.put(LocalDate.now().minusMonths(8), TWO);
        portfolioReturns.put(LocalDate.now().minusMonths(7), TEN_THOUSAND);
        portfolioReturns.put(LocalDate.now().minusMonths(6), ONE);
        portfolioReturns.put(LocalDate.now().minusMonths(5), HUNDRED);
        portfolioReturns.put(LocalDate.now().minusMonths(4), TEN_THOUSAND);
        portfolioReturns.put(LocalDate.now().minusMonths(3), ONE);
        portfolioReturns.put(LocalDate.now().minusMonths(2), HUNDRED);
        portfolioReturns.put(LocalDate.now().minusMonths(1), TEN_THOUSAND);
    }

    @Test
    void calculateRollingValue_checkResult() {
        //SETUP
        final var calculationDTO = mock(CalculationDTO.class);
        final var sharpeRatioCalculation = mock(SharpeRatioCalculation.class);
        final var sut = mock(RollingSharpeRatioCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(), sharpeRatioCalculation));
        final int numberOfMonths = 12;

        when(sharpeRatioCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(TEN);

        doCallRealMethod().when(sut).calculateRollingValue(numberOfMonths, portfolioReturns);
        //ACT
        final BigDecimal actual = sut.calculateRollingValue(numberOfMonths, portfolioReturns);

        //VERIFY
        Assertions.assertEquals(TEN, actual);
    }

    @Test
    void defineResponseType_verifyGetRollingIntervalResDTOS() {
        //SETUP
        final var sut = mock(RollingSharpeRatioCalculation.class);
        final var result = Set.of(Pair.of("12", portfolioReturns));

        doCallRealMethod().when(sut).defineResponseType(result);
        //ACT
        sut.defineResponseType(result);

        //VERIFY
        verify(sut).getRollingIntervalResDTOS(result);
    }


    @Test
    void defineResponseType_checkResult() {
        //SETUP
        final var sut = mock(RollingSharpeRatioCalculation.class);
        final var result = Set.of(Pair.of("12", portfolioReturns));

        final LinkedHashSet<IntervalResDTO> res = new LinkedHashSet<>();
        res.add(new IntervalResDTO(LocalDate.now().minusMonths(3), TEN));
        final var resDTO = new RollingIntervalResDTO("12", res);
        final var expected = new RollingSharpeRatioResDTO(Set.of(resDTO));

        when(sut.getRollingIntervalResDTOS(anySet())).thenReturn(Set.of(resDTO));

        doCallRealMethod().when(sut).defineResponseType(result);
        //ACT
        final RollingSharpeRatioResDTO actual = sut.defineResponseType(result);

        //VERIFY
        Assertions.assertEquals(expected.getRollingSharpeRatio(), actual.getRollingSharpeRatio());
    }

    @AfterAll
    static void tearDown() {
        portfolioReturns.clear();
    }
}