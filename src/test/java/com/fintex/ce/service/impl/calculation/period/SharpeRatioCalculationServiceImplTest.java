package com.fintex.ce.service.impl.calculation.period;


import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodsReqDtoValidator;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SharpeRatioCalculationServiceImplTest {

    @Test
    void defineCalculationMethod_verifyBuildCalculationDto() {
        //SETUP
        final var requestValidator = mock(PeriodsReqDtoValidator.class);
        final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
        final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
                .useConstructor(null, tBillsCacheStorage, null, requestValidator));

        final var weightedAverageInputDTO = mock(CalculationDTO.class);
        final PeriodsReqDTO req = mock(PeriodsReqDTO.class);
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(new TreeMap<>());
        when(sut.buildCalculationDto(any(), any())).thenReturn(weightedAverageInputDTO);

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_ONE);
    }

    @Test
    void defineCalculationMethod_verifyLoadTBillsFor() {
        //SETUP
        final var requestValidator = mock(PeriodsReqDtoValidator.class);
        final var tBillsCacheStorage = mock(TBillsCacheStorage.class);
        final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
                .useConstructor(null, tBillsCacheStorage, null, requestValidator));

        final var calculationDTO = mock(CalculationDTO.class);
        final PeriodsReqDTO req = mock(PeriodsReqDTO.class);
        when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(new TreeMap<>());
        when(req.getCurrency()).thenReturn(Currency.CAD);
        when(sut.buildCalculationDto(any(), any())).thenReturn(calculationDTO);

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(tBillsCacheStorage).loadTBillsFor(Currency.CAD);
    }

}
