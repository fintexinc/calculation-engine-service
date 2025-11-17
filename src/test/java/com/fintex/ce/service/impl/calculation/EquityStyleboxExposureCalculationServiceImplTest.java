package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.EquityStyleboxExposureResDto;
import com.fintex.ce.service.impl.cache.EquityStyleboxExposureCacheStorage;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityStyleboxExposureCalculationServiceImplTest {

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var cacheStorage = mock(EquityStyleboxExposureCacheStorage.class);

        final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
                .useConstructor(requestValidator, cacheStorage));

        final PortfolioHoldingsReqDTO req = mock(PortfolioHoldingsReqDTO.class);
        final List<Holding> holdings = List.of(mock(Holding.class));

        when(req.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(req);

        //VERIFY
        verify(requestValidator).validate(req);
    }

    @Test
    void getLoadFromCacheStorage_checkResult() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var cacheStorage = mock(EquityStyleboxExposureCacheStorage.class);
        final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
                .useConstructor(requestValidator, cacheStorage));

        final var holding = mock(Holding.class);
        final var exposures = Map.of(holding, Map.of(EquityStyleboxType.LARGE_VALUE, BigDecimal.TEN));

        when(cacheStorage.load(any(), any(), any(), any())).thenReturn(exposures);
        doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
        //ACT
        final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsReqDTO.class), List.of());

        //VERIFY
        Assertions.assertEquals(exposures, actual);
    }

    @Test
    void calculate_verifyCalculateNetProducts() {
        //SETUP
        final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class);

        final var holding = mock(Holding.class);
        final var holdings = List.of(holding);
        final var exposures = Map.of(holding, Map.of(EquityStyleboxType.LARGE_VALUE, BigDecimal.TEN));

        doCallRealMethod().when(sut).calculate(any(), any(), any());
        //ACT
        sut.calculate(exposures, holdings, List.of());

        //VERIFY
        verify(sut).calculateNetProducts(exposures, holdings, EquityStyleboxType.values());
    }

    @Test
    void calculate_verifyReScale() {
        try (final var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
            //SETUP
            final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(EquityStyleboxType.LARGE_VALUE, BigDecimal.TEN));
            final var netProducts = mock(Map.class);
            when(sut.calculateNetProducts(exposures, holdings, EquityStyleboxType.values())).thenReturn(netProducts);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
        }
    }


    @Test
    void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
        //SETUP
        final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class);
        try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            final var exposures = mock(Map.class);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
        }
    }

    @Test
    void calculate_checkResultWhenExposureIsAllZeroValuesMap() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var cacheStorage = mock(EquityStyleboxExposureCacheStorage.class);
            final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
                    .useConstructor(requestValidator, cacheStorage));

            final var exposures = mock(Map.class);
            final var expected = new EquityStyleboxExposureResDto(EquityStyleboxExposureCalculationServiceImpl.DEFAULT_MAP, List.of());

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final var actual = sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            Assertions.assertEquals(expected, actual);
        }
    }
}