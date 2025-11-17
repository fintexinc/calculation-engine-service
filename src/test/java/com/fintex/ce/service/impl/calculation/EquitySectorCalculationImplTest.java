package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.EquitySectorResDTO;
import com.fintex.ce.service.impl.cache.EquitySectorCacheStorage;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquitySectorCalculationImplTest {

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var cacheStorage = mock(EquitySectorCacheStorage.class);
        final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
                .useConstructor(cacheStorage, requestValidator));

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
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var cacheStorage = mock(EquitySectorCacheStorage.class);
            final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
                    .useConstructor(cacheStorage, requestValidator));

            final var holding = mock(Holding.class);
            final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));

            when(cacheStorage.load(any(), any(), any(), any())).thenReturn(exposures);
            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsReqDTO.class), List.of());

            //VERIFY
            Assertions.assertEquals(exposures, actual);
        }
    }

    @Test
    void calculate_verifyCalculateNetProducts() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {

            //SETUP
            final var sut = mock(EquitySectorCalculationImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            verify(sut).calculateNetProducts(exposures, holdings, EquitySectorAllocationType.values());
        }
    }

    @Test
    void calculate_verifyReScale() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var sut = mock(EquitySectorCalculationImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));
            final var netProducts = mock(Map.class);

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
            when(sut.calculateNetProducts(exposures, holdings, EquitySectorAllocationType.values())).thenReturn(netProducts);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
        }
    }

    @Test
    void calculate_verifyToUserScale() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class);
             var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var sut = mock(EquitySectorCalculationImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));
            final var netProducts = mock(Map.class);

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
            mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(netProducts);

            when(sut.calculateNetProducts(exposures, holdings, EquitySectorAllocationType.values())).thenReturn(netProducts);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
        }
    }

    @Test
    void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var cacheStorage = mock(EquitySectorCacheStorage.class);
            final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
                    .useConstructor(cacheStorage, requestValidator));

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
            final var cacheStorage = mock(EquitySectorCacheStorage.class);
            final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
                    .useConstructor(cacheStorage, requestValidator));

            final var exposures = mock(Map.class);
            final var expected = new EquitySectorResDTO(EquitySectorCalculationImpl.DEFAULT_MAP, List.of());

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final var actual = sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            Assertions.assertEquals(expected, actual);
        }
    }

}