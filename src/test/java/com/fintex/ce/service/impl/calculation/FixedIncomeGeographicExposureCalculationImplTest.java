package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.GeographicExposureResDTO;
import com.fintex.ce.service.impl.cache.FixedIncomeGeographicExposureCacheStorage;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeGeographicExposureCalculationImplTest {

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
        final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
                withSettings().useConstructor(storage, requestValidator));

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
    void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
            final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
                    withSettings().useConstructor(storage, requestValidator));

            final var exposures = mock(Map.class);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
        }
    }

    @Test
    void calculate_checkResultWhenExposureIsAllZeroValuesMap() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
            final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
                    withSettings().useConstructor(storage, requestValidator));

            final var exposures = mock(Map.class);
            final var expected = new GeographicExposureResDTO(FixedIncomeGeographicExposureCalculationImpl.DEFAULT_MAP, List.of());

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final var actual = sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            assertEquals(expected, actual);
        }
    }

    @Test
    void getLoadFromCacheStorage_checkResult() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
            final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
                    withSettings().useConstructor(storage, requestValidator));

            final var holding = mock(Holding.class);
            final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

            when(storage.load(any(), any(), any(), any())).thenReturn(exposures);
            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsReqDTO.class), List.of());

            //VERIFY
            assertEquals(exposures, actual);
        }
    }

    @Test
    void calculate_verifyCalculateNetProducts() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
            final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
                    withSettings().useConstructor(storage, requestValidator));

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            verify(sut).calculateNetProducts(exposures, holdings, GeographicRegionType.values());
        }
    }

}
