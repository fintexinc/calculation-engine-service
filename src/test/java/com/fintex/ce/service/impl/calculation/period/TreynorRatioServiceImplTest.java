package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.config.enumeration.Currency.CAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;


class TreynorRatioServiceImplTest {

    TreynorRatioServiceImplTest() {
    }

    @Test
    void defineCalculationMethod_verifyBuildCalculationDto() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
        final var requestValidator = mock(PeriodReqDtoForBenchmarkCalculationsValidator.class);
        final var sut = mock(TreynorRatioServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, tBillsCacheStorage, Set.of(), requestValidator));

        final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
        final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

        final var req = mock(PeriodsReqDTO.class);
        when(req.getCurrency()).thenReturn(CAD);
        when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
        when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
        when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
        when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).defineCalculationMethod(req);
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(sut, times(2)).buildCalculationDto(eq(req), any());
    }

    @Test
    void defineCalculationMethod_verifyLoadTBillsFor() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
        final var requestValidator = mock(PeriodReqDtoForBenchmarkCalculationsValidator.class);
        final var sut = mock(TreynorRatioServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, tBillsCacheStorage, Set.of(), requestValidator));

        final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
        final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

        final var req = mock(PeriodsReqDTO.class);
        when(req.getCurrency()).thenReturn(CAD);
        when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
        when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
        when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
        when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).defineCalculationMethod(req);
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(tBillsCacheStorage).loadTBillsFor(CAD);
    }

    @Test
    void defineCalculationMethod_verifyCalculateExcessReturn() {
        try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
            //SETUP
            final var monthlyReturnsService = mock(MonthlyReturnsService.class);
            final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
            final var requestValidator = mock(PeriodReqDtoForBenchmarkCalculationsValidator.class);
            final var sut = mock(TreynorRatioServiceImpl.class, withSettings()
                    .useConstructor(monthlyReturnsService, tBillsCacheStorage, Set.of(), requestValidator));

            final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
            final TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>();
            final var req = mock(PeriodsReqDTO.class);

            when(req.getCurrency()).thenReturn(CAD);
            when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(treeMap);
            when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
            when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
            when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(treeMap);

            doCallRealMethod().when(sut).defineCalculationMethod(req);
            //ACT
            sut.defineCalculationMethod(req);

            //VERIFY
            mockedPeriodCalculationAbstract.verify(Mockito.times(2), () -> PeriodCalculationAbstract.calculateExcessReturn(treeMap, treeMap));
        }
    }
}