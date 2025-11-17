package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class UpsideCaptureCalculationServiceImplTest {

    @Test
    void defineCalculationMethod_verifyBuildCalculationDto() {
        //SETUP
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var requestValidator = mock(PeriodReqDtoForBenchmarkCalculationsValidator.class);
        final var sut = mock(UpsideCaptureCalculationServiceImpl.class, withSettings()
                .useConstructor(monthlyReturnsService, Set.of(), requestValidator));

        final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
        final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

        final var req = mock(PeriodsReqDTO.class);
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
        when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
        when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).defineCalculationMethod(req);
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(sut).buildCalculationDto(req, ReturnFactorScale.AS_IS);
    }
}