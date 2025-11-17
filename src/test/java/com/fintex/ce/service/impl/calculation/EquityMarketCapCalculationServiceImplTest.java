package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.EquityMarketCapResDTO;
import com.fintex.ce.service.impl.cache.EquityMarketCapitalizationCacheStorage;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.GIANT;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.LARGE;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.MEDIUM;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.MICRO;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.SMALL;
import static com.fintex.ce.service.impl.calculation.EquityMarketCapCalculationServiceImpl.DEFAULT_MAP;
import static com.fintex.ce.service.impl.calculation.EquityMarketCapCalculationServiceImpl.GROUPS;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityMarketCapCalculationServiceImplTest {

    @Test
    void staticFieldsInitialization_verifyGROUPS() {
        //SETUP
        final var groupsExpected = Map.of(
                LARGE, Set.of(LARGE, GIANT),
                MEDIUM, Set.of(MEDIUM),
                SMALL, Set.of(SMALL, MICRO)
        );

        //ACT

        //VERIFY
        Assertions.assertNotNull(groupsExpected);
        ComparisonUtils.compareMaps(groupsExpected, GROUPS);
    }

    @Test
    void staticFieldsInitialization_verifyDEFAULT_MAP() {
        //SETUP
        final Map<EquityMarketCapType, BigDecimal> defaultMapExpected = new HashMap<>();
        defaultMapExpected.put(LARGE, null);
        defaultMapExpected.put(MEDIUM, null);
        defaultMapExpected.put(SMALL, null);

        //ACT

        //VERIFY
        Assertions.assertNotNull(defaultMapExpected);
        ComparisonUtils.compareMaps(defaultMapExpected, DEFAULT_MAP);
    }

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var marketCapCacheStorage = mock(EquityMarketCapitalizationCacheStorage.class);
        final var sut = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
                .useConstructor(marketCapCacheStorage, requestValidator));

        final var holdings = List.of(mock(Holding.class));
        final var req = mock(PortfolioHoldingsReqDTO.class);

        when(req.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(req);

        //VERIFY
        verify(requestValidator).validate(req);
    }

    @Test
    void perform_verifyLoad() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var marketCapCacheStorage = mock(EquityMarketCapitalizationCacheStorage.class);
        final var sut = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
                .useConstructor(marketCapCacheStorage, requestValidator));

        final var holdings = List.of(mock(Holding.class));
        final var req = mock(PortfolioHoldingsReqDTO.class);

        when(req.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
        //ACT
        sut.getLoadFromCacheStorage(req, List.of());

        //VERIFY
        verify(marketCapCacheStorage).load(req.getHoldings(), List.of(), List.of(), new ParamHolderDTO());
    }

    @Test
    void perform_verifyAreAllValuesZerosInMapOfExposure() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var marketCapCacheStorage = mock(EquityMarketCapitalizationCacheStorage.class);
            final var sut = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
                    .useConstructor(marketCapCacheStorage, requestValidator));

            final var exposures = mock(Map.class);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
        }
    }

    @Test
    void perform_checkResultWhenExposureIsAllZeroValuesMap() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var marketCapCacheStorage = mock(EquityMarketCapitalizationCacheStorage.class);
            final var sut = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
                    .useConstructor(marketCapCacheStorage, requestValidator));

            final var exposures = mock(Map.class);
            final var expected = new EquityMarketCapResDTO(EquityMarketCapCalculationServiceImpl.DEFAULT_MAP, List.of());

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(true);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final var actual = sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            Assertions.assertEquals(expected, actual);
        }
    }


    @Test
    void calculate_verifyCalculateNetProducts() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {

            //SETUP
            final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(EquityMarketCapType.SMALL, TEN));

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            verify(sut).calculateNetProducts(exposures, holdings, EquityMarketCapType.values());
        }
    }

    @Test
    void calculate_verifyReScale() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(EquityMarketCapType.SMALL, TEN));
            final var netProducts = mock(Map.class);

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
            when(sut.calculateNetProducts(exposures, holdings, EquityMarketCapType.values())).thenReturn(netProducts);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
        }
    }

    @Test
    void calculate_verifyGroupedResults() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

            final var holdings = mock(List.class);
            final var exposures = mock(Map.class);
            final var reScaled = mock(Map.class);
            final var netProducts = mock(Map.class);


            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
            mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(netProducts)).thenReturn(reScaled);
            when(sut.calculateNetProducts(exposures, holdings, EquityMarketCapType.values())).thenReturn(netProducts);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            verify(sut).groupedResults(reScaled);
        }
    }

    @Test
    void calculate_verifyToUserScale() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

            final var holdings = mock(List.class);
            final var exposures = mock(Map.class);
            final var groupedResults = mock(Map.class);

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
            when(sut.groupedResults(any())).thenReturn(groupedResults);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(groupedResults));
        }
    }

    @Test
    void calculate_checkResult() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP

            final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

            final var holdings = mock(List.class);
            final var exposures = mock(Map.class);
            final var groupedResults = mock(Map.class);
            final var expected = mock(Map.class);

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
            mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(groupedResults)).thenReturn(expected);
            when(sut.groupedResults(any())).thenReturn(groupedResults);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final var actual = sut.calculate(exposures, holdings, List.of());

            //VERIFY
            Assertions.assertNotNull(actual);
            ComparisonUtils.compareMaps(expected, actual.getEquityMarketCapitalization());
        }
    }

    @Test
    void groupedResults_verifyCalculateSumWithingTheSameGroupForEachOfGROUPS() {
        //SETUP
        final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

        final var netProducts = mock(Map.class);

        when(sut.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

        doCallRealMethod().when(sut).groupedResults(any());
        //ACT
        sut.groupedResults(netProducts);

        //VERIFY
        for (var entry : GROUPS.entrySet()) {
            verify(sut).calculateSumWithinTheSameGroup(netProducts, entry);
        }
    }

    @Test
    void groupedResults_checkResult() {
        //SETUP
        final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

        final var netProducts = Map.of(EquityMarketCapType.SMALL, TEN);
        final var expectedResult = GROUPS.keySet().stream().collect(toMap(e -> e, e -> TEN));

        when(sut.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

        doCallRealMethod().when(sut).groupedResults(any());
        //ACT
        final var actualResult = sut.groupedResults(netProducts);

        //VERIFY
        Assertions.assertNotNull(actualResult);
        ComparisonUtils.compareMaps(expectedResult, actualResult);
    }

    @Test
    void calculateSumWithinTheSameGroup_checkResult1() {
        //SETUP
        final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

        final var netProducts = Map.of(
                EquityMarketCapType.MICRO, BigDecimal.valueOf(5),
                EquityMarketCapType.SMALL, BigDecimal.valueOf(6)
        );
        final var expected = Map.of(EquityMarketCapType.SMALL, BigDecimal.valueOf(11));
        final var entry = new AbstractMap.SimpleEntry<>(SMALL, Set.of(SMALL, MICRO));

        doCallRealMethod().when(sut).calculateSumWithinTheSameGroup(any(), any());
        //ACT
        final var actual = sut.calculateSumWithinTheSameGroup(netProducts, entry);

        //VERIFY
        assertEquals(expected.get(SMALL), actual);
    }

    @Test
    void calculateSumWithinTheSameGroup_checkResult2() {
        //SETUP
        final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

        final var netProducts = Map.of(EquityMarketCapType.MEDIUM, ZERO);
        final var expected = Map.of(EquityMarketCapType.MEDIUM, ZERO);
        final var entry = new AbstractMap.SimpleEntry<>(MEDIUM, Set.of(MEDIUM));

        doCallRealMethod().when(sut).calculateSumWithinTheSameGroup(any(), any());
        //ACT
        final var actual = sut.calculateSumWithinTheSameGroup(netProducts, entry);

        //VERIFY
        assertEquals(expected.get(MEDIUM), actual);
    }

    @Test
    void calculateSumWithinTheSameGroup_checkResult3() {
        //SETUP
        final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

        final var netProducts = Map.of(
                EquityMarketCapType.LARGE, BigDecimal.valueOf(7),
                EquityMarketCapType.GIANT, BigDecimal.valueOf(8)
        );
        final var expected = Map.of(EquityMarketCapType.LARGE, BigDecimal.valueOf(15));
        final var entry = new AbstractMap.SimpleEntry<>(LARGE, Set.of(LARGE, GIANT));

        doCallRealMethod().when(sut).calculateSumWithinTheSameGroup(any(), any());
        //ACT
        final var actual = sut.calculateSumWithinTheSameGroup(netProducts, entry);

        //VERIFY
        assertEquals(expected.get(LARGE), actual);
    }

}