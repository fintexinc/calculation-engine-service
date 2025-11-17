package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.calculation.Growth10KCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.MaxDrawdownReqValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MaxDrawdownServiceImplTest {

    @Test
    void defineCalculationMethod_verifyBuildCalculationDto() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(MaxDrawdownReqValidator.class);
        final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var benchmarkCalculationDTO = mock(CalculationDTO.class);
        final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));

        final var req = mock(PeriodsReqDTO.class);
        when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).defineCalculationMethod(req);
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
    }

    @Test
    void defineCalculationMethod_verifyInitializeGrowthOf10KMap() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(MaxDrawdownReqValidator.class);
        final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var benchmarkCalculationDTO = mock(CalculationDTO.class);
        final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));

        final var req = mock(PeriodsReqDTO.class);
        when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).defineCalculationMethod(req);
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(sut).initializeGrowthOf10KMap(eq(benchmarkCalculationDTO), any());
    }

    @Test
    void initializeGrowthOf10KMap_checkResult() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(MaxDrawdownReqValidator.class);
        final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var inputDTO = mock(CalculationDTO.class);
        final var weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));
        final var growth10KCalculation = new Growth10KCalculation(null, null, false);

        when(inputDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).initializeGrowthOf10KMap(any(), any());
        //ACT
        final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeGrowthOf10KMap(inputDTO, growth10KCalculation);

        //VERIFY
        assertNotNull(actual.entrySet().stream().findFirst());
    }

    @Test
    void initializeGrowthOf10KMap_checkResult2() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(MaxDrawdownReqValidator.class);
        final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var inputDTO = mock(CalculationDTO.class);
        final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();
        final var growth10KCalculation = new Growth10KCalculation(null, null, false);

        when(inputDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).initializeGrowthOf10KMap(any(), any());
        //ACT
        final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeGrowthOf10KMap(inputDTO, growth10KCalculation);

        //VERIFY
        assertFalse(actual.entrySet().stream().findFirst().isPresent());
    }
}
