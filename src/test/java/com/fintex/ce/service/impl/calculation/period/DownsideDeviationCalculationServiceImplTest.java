package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodsReqDtoValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DownsideDeviationCalculationServiceImplTest {

    @Test
    void defineCalculationMethod_verifyDefineCalculationMethod() {
        //SETUP
        final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
        final var requestValidator = mock(PeriodsReqDtoValidator.class);
        final var sut = mock(DownsideDeviationCalculationServiceImpl.class, withSettings()
                .useConstructor(null, tBillsCacheStorage, Set.of(), requestValidator));

        final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
        final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));

        final var req = mock(PeriodsReqDTO.class);
        when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
        when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
        when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(weightedAverageReturns);

        doCallRealMethod().when(sut).defineCalculationMethod(req);
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_ONE);
    }
}