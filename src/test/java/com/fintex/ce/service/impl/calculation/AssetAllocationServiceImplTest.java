package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegionType;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.mapper.AssetAllocationDataMapper;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.AssetAllocationCacheStorage;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.data.DataProviderChecker;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.config.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AssetAllocationServiceImplTest {

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final PortfolioHoldingsReqDTO req = mock(PortfolioHoldingsReqDTO.class);
        final List<Holding> holdings = List.of(mock(Holding.class));
        when(req.getHoldings()).thenReturn(holdings);
        final List<DataProvider> providers = List.of(DataProvider.values());
        when(req.getDataProviders()).thenReturn(providers);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(req);

        //VERIFY
        verify(requestValidator).validate(req);
    }

    @Test
    void getLoadFromCacheStorage_checkResult() {
        //SETUP
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));
        final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
        final var expected = mock(Map.class);
        when(assetAllocationDataMapper.mapForAA(assetAllocationDataDto)).thenReturn(expected);

        when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(assetAllocationDataDto);
        doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
        //ACT
        final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsReqDTO.class), List.of());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getLoadFromCacheStorage_verifyMapForAA() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                    assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));
            final var req = mock(PortfolioHoldingsReqDTO.class);
            final List<Warning> warnings = List.of();
            final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
            when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(assetAllocationDataDto);

            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(req, warnings);

            //VERIFY
            verify(assetAllocationDataMapper).mapForAA(assetAllocationDataDto);
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyValidate() {
        //SETUP
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final var req = mock(PortfolioHoldingsReqDTO.class);
        final List<Warning> warnings = List.of();
        final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
        when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(assetAllocationDataDto);

        doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
        //ACT
        sut.getLoadFromCacheStorage(req, warnings);

        //VERIFY
        verify(assetAllocationDataValidator).validate(assetAllocationDataDto, warnings);
    }

    @Test
    void getLoadFromCacheStorage_verifyDtaProviderCheckerCheck() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                    assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var req = mock(PortfolioHoldingsReqDTO.class);
            when(req.getDataProviders()).thenReturn(null);
            final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
            when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(assetAllocationDataDto);
            final var defaultProviders = List.of(DEFAULT_PROVIDERS);
            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(any(), any())).thenReturn(defaultProviders);

            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(req, List.of());

            //VERIFY
            verify(dataProviderChecker).check(defaultProviders, assetAllocationDataDto);
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyGetSpecifiedIfEmpty() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                    assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var reqDTO = mock(PortfolioHoldingsReqDTO.class);
            final var holdings = mock(List.class);
            final List<Warning> warnings = List.of();
            final var providers = mock(List.class);

            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(reqDTO.getDataProviders()).thenReturn(providers);

            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(reqDTO, warnings);

            //VERIFY
            mockedFilterUtils.verify(Mockito.times(2), () -> FilterUtils.getSpecifiedIfEmpty(providers, DEFAULT_PROVIDERS));
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyLoadWithDataProvidesCheck1() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                    assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var reqDTO = mock(PortfolioHoldingsReqDTO.class);
            final var holdings = mock(List.class);
            final List<Warning> warnings = List.of();
            final var defaultProviders = mock(List.class);

            when(reqDTO.getHoldings()).thenReturn(holdings);
            mockedFilterUtils.when((() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any()))).thenReturn(defaultProviders);

            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(reqDTO, warnings);

            //VERIFY
            verify(assetAllocationCacheStorage).loadWithDataProvidesCheck(holdings, defaultProviders, warnings);
        }
    }

    @Test
    void calculate_verifyCalculateNetProducts() {
        //SETUP
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
                assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final var holding = mock(Holding.class);
        final var holdings = List.of(holding);
        final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));
        doCallRealMethod().when(sut).calculate(any(), any(), any());

        //ACT
        sut.calculate(exposures, holdings, List.of());

        //VERIFY
        verify(sut).calculateNetProducts(exposures, holdings, AssetAllocationRegion.values());
    }

    @Test
    void calculate_verifyCalculateAssetAllocationResponse() {
        //SETUP
        final var sut = mock(AssetAllocationServiceImpl.class);

        final var holding = mock(Holding.class);
        final var holdings = List.of(holding);
        final Map netProducts = mock(Map.class);
        final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));
        when(sut.calculateNetProducts(any(), any(), any())).thenReturn(netProducts);
        doCallRealMethod().when(sut).calculate(any(), any(), any());

        //ACT
        sut.calculate(exposures, holdings, List.of());

        //VERIFY
        verify(sut).calculateAssetAllocationResponse(netProducts);
    }

    @Test
    void calculate_verifyToUserScale() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
             var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var sut = mock(AssetAllocationServiceImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.FIXED_INCOME, TEN));
            final var netProducts = mock(Map.class);

            mockedCalculationUtils.when((() -> CalculationUtils.reScaleAbs(any()))).thenReturn(netProducts);

            when(sut.calculateNetProducts(exposures, holdings, AssetAllocationRegion.values())).thenReturn(netProducts);
            when(sut.calculateAssetAllocationResponse(netProducts)).thenReturn(netProducts);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
        }
    }

    @Test
    void calculateAssetAllocationResponse_checkResult() {
        //SETUP
        final AssetAllocationServiceImpl m = mock(AssetAllocationServiceImpl.class);
        final Map<AssetAllocationRegionType, BigDecimal> expected = Stream.of(AssetAllocationRegionType.values())
                .filter(type -> !AssetAllocationRegionType.INTERNATIONAL_EQUITY.equals(type))
                .collect(Collectors.toMap(k -> k, v -> TEN));
        expected.put(AssetAllocationRegionType.INTERNATIONAL_EQUITY, BigDecimal.valueOf(40));
        expected.put(AssetAllocationRegionType.OTHER, BigDecimal.valueOf(20));
        doCallRealMethod().when(m).calculateAssetAllocationResponse(anyMap());
        //ACT
        final Map<AssetAllocationRegion, BigDecimal> allocations = Stream.of(AssetAllocationRegion.values())
                .collect(Collectors.toMap(k -> k, v -> TEN));
        final Map<AssetAllocationRegionType, BigDecimal> actual = m.calculateAssetAllocationResponse(allocations);
        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareMaps(expected, actual);
    }

}
