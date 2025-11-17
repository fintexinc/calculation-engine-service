package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.dto.CommonHoldingsDTO;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.calculation.HoldingAggregatorDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.dto.response.TopCommonHoldingsResDTO;
import com.fintex.ce.dto.response.commonholdings.ParentHoldingDTO;
import com.fintex.ce.dto.response.commonholdings.TopCommonHoldingsDTO;
import com.fintex.ce.dto.response.correlation.HoldingsKeyDTO;
import com.fintex.ce.service.impl.cache.CommonHoldingsCacheStorage;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.TopCommonHoldingsReqValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.fintex.ce.config.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CommonHoldingsServiceImplTest {

    @Test
    void perform_verifyValidateCommonHoldings() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            when(reqDTO.getHoldings()).thenReturn(holdings);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(requestValidator).validate(reqDTO);
        }
    }


    @Test
    void perform_verifyCalculateInitialPortfolioWeight() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            when(reqDTO.getHoldings()).thenReturn(holdings);
            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
        }
    }

    @Test
    void perform_verifyLoad() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));
            final var allocations = Map.of(mock(Holding.class), TEN);

            when(reqDTO.getHoldings()).thenReturn(holdings);
            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(cacheStorage).load(holdings, List.of(), List.of(), new ParamHolderDTO(allocations));
        }
    }

    @Test
    void perform_verifyGetNumOfFundsMin() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(Map.of());

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).getNumOfFundsMin(reqDTO);
        }
    }

    @Test
    void perform_verifyverifyGetAccumulativeTypes() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(Map.of());
            when(sut.getNumOfFundsMin(any())).thenReturn(1);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).getAccumulativeTypes(reqDTO);
        }
    }

    @Test
    void perform_verifyCalculateCalculateTopCommonHoldings() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            final var holdingsFromCache = Map.of(mock(Holding.class), List.of(mock(CommonHoldingsDTO.class)));
            final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
            final var accumulativeTypes = Set.of("E");
            final var leaves = Map.of(mock(HoldingAggregatorDTO.class), List.of(mock(CommonHoldingsDTO.class)));

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(holdingsFromCache);
            when(sut.getNumOfFundsMin(any())).thenReturn(1);
            when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
            when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).calculateTopCommonHoldings(holdingsFromCache, allocations, accumulativeTypes);
        }
    }

    @Test
    void perform_verifyCalculateFilterTop10Common() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            final var holdingsFromCache = Map.of(mock(Holding.class), List.of(mock(CommonHoldingsDTO.class)));
            final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
            final var accumulativeTypes = Set.of("E");
            final var leaves = Map.of(mock(HoldingAggregatorDTO.class), List.of(mock(CommonHoldingsDTO.class)));

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(holdingsFromCache);
            when(sut.getNumOfFundsMin(any())).thenReturn(1);
            when(sut.getTopCommonHoldingsNumber(any())).thenReturn(123);
            when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
            when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).filterTopCommon(1, 123, leaves);
        }
    }

    @Test
    void perform_verifytoFinalResult() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            final var holdingsFromCache = Map.of(mock(Holding.class), List.of(mock(CommonHoldingsDTO.class)));
            final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
            final var accumulativeTypes = Set.of("E");
            final var leaves = Map.of(mock(HoldingAggregatorDTO.class), List.of(mock(CommonHoldingsDTO.class)));
            final var sortedLeaves = Map.of(mock(HoldingAggregatorDTO.class), TEN);

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(holdingsFromCache);
            when(sut.getNumOfFundsMin(any())).thenReturn(1);
            when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
            when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);
            when(sut.filterTopCommon(anyInt(), anyInt(), any())).thenReturn(sortedLeaves);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).toFinalResult(leaves, sortedLeaves);
        }
    }

    @Test
    void perform_checkResult() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            final var holdingsFromCache = Map.of(mock(Holding.class), List.of(mock(CommonHoldingsDTO.class)));
            final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
            final var accumulativeTypes = Set.of("E");
            final var leaves = Map.of(mock(HoldingAggregatorDTO.class), List.of(mock(CommonHoldingsDTO.class)));
            final var sortedLeaves = Map.of(mock(HoldingAggregatorDTO.class), TEN);
            final var topCommonHoldingsDTO = List.of(mock(TopCommonHoldingsDTO.class));

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(holdingsFromCache);
            when(sut.getNumOfFundsMin(any())).thenReturn(1);
            when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
            when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);
            when(sut.filterTopCommon(anyInt(), anyInt(), any())).thenReturn(sortedLeaves);
            when(sut.toFinalResult(anyMap(), anyMap())).thenReturn(topCommonHoldingsDTO);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            final TopCommonHoldingsResDTO actual = sut.perform(reqDTO);

            //VERIFY
            assertEquals(topCommonHoldingsDTO, actual.getCommonHoldings());
        }
    }

    @Test
    void toFinalResult_verifyMapToFinalResult() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var leaves = Map.of(mock(HoldingAggregatorDTO.class), List.of(mock(CommonHoldingsDTO.class)));
        final var aggregatorDTO = mock(HoldingAggregatorDTO.class);
        final var ten = TEN;
        final var sortedLeaves = Map.of(aggregatorDTO, ten);
        final var entry = new AbstractMap.SimpleEntry<>(aggregatorDTO, ten);

        doCallRealMethod().when(sut).toFinalResult(anyMap(), anyMap());
        //ACT
        var actual = sut.toFinalResult(leaves, sortedLeaves);

        //VERIFY
        verify(sut).mapToFinalResult(leaves, entry);
    }

    @Test
    void toFinalResult_checkResult() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var leaves = Map.of(mock(HoldingAggregatorDTO.class), List.of(mock(CommonHoldingsDTO.class)));
        final var sortedLeaves = Map.of(mock(HoldingAggregatorDTO.class), TEN);
        final var expected = new TopCommonHoldingsDTO();

        when(sut.mapToFinalResult(anyMap(), any())).thenReturn(expected);

        doCallRealMethod().when(sut).toFinalResult(anyMap(), anyMap());
        //ACT
        final List<TopCommonHoldingsDTO> actual = sut.toFinalResult(leaves, sortedLeaves);

        //VERIFY
        assertEquals(expected, actual.get(0));
    }

    @Test
    void getNumOfFundsMin_checkResult() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsReqDTO.class);
        final var expected = 1;

        when(topCommonHoldingsReqDTO.getNumOfFundsMin()).thenReturn(null);

        doCallRealMethod().when(sut).getNumOfFundsMin(any());
        //ACT
        final int actual = sut.getNumOfFundsMin(topCommonHoldingsReqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getNumOfFundsMin_checkResult2() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsReqDTO.class);
        final var expected = 7;

        when(topCommonHoldingsReqDTO.getNumOfFundsMin()).thenReturn(expected);

        doCallRealMethod().when(sut).getNumOfFundsMin(any());
        //ACT
        final int actual = sut.getNumOfFundsMin(topCommonHoldingsReqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getAccumulativeTypes_checkResult() {
        //SETUP
        final var accumulativeTypes = Set.of("E");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulativeTypes, requestValidator));

        final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsReqDTO.class);

        when(topCommonHoldingsReqDTO.getAccumulateHoldingTypes()).thenReturn(accumulativeTypes);

        doCallRealMethod().when(sut).getAccumulativeTypes(any());
        //ACT
        final Set<String> actual = sut.getAccumulativeTypes(topCommonHoldingsReqDTO);

        //VERIFY
        assertEquals(accumulativeTypes, actual);
    }

    @Test
    void getAccumulativeTypes_checkResult2() {
        //SETUP
        final var accumulativeTypes = Set.of();
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulativeTypes, requestValidator));

        final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsReqDTO.class);

        when(topCommonHoldingsReqDTO.getAccumulateHoldingTypes()).thenReturn(Set.of());

        doCallRealMethod().when(sut).getAccumulativeTypes(any());
        //ACT
        final Set<String> actual = sut.getAccumulativeTypes(topCommonHoldingsReqDTO);

        //VERIFY
        assertEquals(accumulativeTypes, actual);

    }

    @Test
    void secondLevelLeaves_checkResult() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final var firstLvlChild = mock(CommonHoldingsDTO.class);

        when(firstLvlChild.getUnderlyingHoldings()).thenReturn(null);

        doCallRealMethod().when(sut).secondLevelLeaves(firstLvlChild);
        //ACT
        final Stream<CommonHoldingsDTO> actual = sut.secondLevelLeaves(firstLvlChild);

        //VERIFY
        assertEquals(firstLvlChild, actual.findFirst().orElseThrow());
    }

    @Test
    void secondLevelLeaves_checkResult2() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final var firstLvlChild = mock(CommonHoldingsDTO.class);
        final var underlyingHolding = mock(CommonHoldingsDTO.class);
        final var underlyingHoldings = List.of(underlyingHolding);

        when(underlyingHolding.getCompanyName()).thenReturn(null);
        when(underlyingHolding.getName()).thenReturn(null);
        when(firstLvlChild.getUnderlyingHoldings()).thenReturn(underlyingHoldings);

        doCallRealMethod().when(sut).secondLevelLeaves(firstLvlChild);
        //ACT
        final Stream<CommonHoldingsDTO> actual = sut.secondLevelLeaves(firstLvlChild);

        //VERIFY
        assertEquals(Stream.of().findFirst(), actual.findFirst());
    }

    @Test
    void filterTop10Common_checkResult() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var holdingAggregatorDTO = mock(HoldingAggregatorDTO.class);
        final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
        final var leaves = Map.of(holdingAggregatorDTO, List.of(commonHoldingsDTO));
        final var expected = Map.of(holdingAggregatorDTO, TEN);

        when(commonHoldingsDTO.getWeight()).thenReturn(TEN);

        doCallRealMethod().when(sut).filterTopCommon(anyInt(), anyInt(), anyMap());
        //ACT
        final Map<HoldingAggregatorDTO, BigDecimal> actual = sut.filterTopCommon(1, 10, leaves);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void setParentAndCalculateWeight_verifyIsLeafStock() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var allocations = Map.of(new Holding(), TEN);
        final var parent = new Holding();
        final var child = mock(CommonHoldingsDTO.class);
        final var expected = new CommonHoldingsDTO();
        expected.setWeight(TEN);
        expected.setHolding(parent);

        when(child.getValue()).thenReturn(TEN);
        when(child.setHolding(parent)).thenReturn(expected);

        doCallRealMethod().when(sut).setParentAndCalculateWeight(anyMap(), any(), any());
        //ACT
        final CommonHoldingsDTO actual = sut.setParentAndCalculateWeight(allocations, parent, child);

        //VERIFY
        verify(sut).isLeafStock(parent, child);
    }


    @Test
    void setParentAndCalculateWeight_checkResult() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var allocations = Map.of(new Holding(), TEN);
        final var parent = new Holding();
        final var child = mock(CommonHoldingsDTO.class);
        final var expected = new CommonHoldingsDTO();
        expected.setWeight(BigDecimal.valueOf(100));
        expected.setHolding(parent);

        when(sut.isLeafStock(any(), any())).thenReturn(true);
        when(child.getValue()).thenReturn(TEN);
        when(child.setHolding(parent)).thenReturn(expected);

        doCallRealMethod().when(sut).setParentAndCalculateWeight(anyMap(), any(), any());
        //ACT
        final CommonHoldingsDTO actual = sut.setParentAndCalculateWeight(allocations, parent, child);

        //VERIFY
        assertEquals(expected.getWeight(), actual.getWeight());
        assertEquals(expected.getHolding(), actual.getHolding());
    }

    @Test
    void setParentAndCalculateWeight_checkResult2() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var parent = mock(Holding.class);
        final var allocations = Map.of(parent, TEN);
        final var child = mock(CommonHoldingsDTO.class);
        final var expected = new CommonHoldingsDTO();
        expected.setWeight(BigDecimal.valueOf(100));
        expected.setHolding(parent);

        when(sut.isLeafStock(any(), any())).thenReturn(false);
        when(child.getValue()).thenReturn(TEN);
        when(child.getCompanyName()).thenReturn("Apple Inc");
        when(child.getType()).thenReturn("E");
        when(child.setHolding(parent)).thenReturn(expected);

        doCallRealMethod().when(sut).setParentAndCalculateWeight(anyMap(), any(), any());
        //ACT
        final CommonHoldingsDTO actual = sut.setParentAndCalculateWeight(allocations, parent, child);

        //VERIFY
        assertEquals(expected.getWeight(), actual.getWeight());
        assertEquals(expected.getHolding(), actual.getHolding());
    }

    @Test
    void calculateWeightWithinSameLeaves_verifyToUserScale() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var holdingsDTO = mock(CommonHoldingsDTO.class);
            final var sameLeaves = List.of(holdingsDTO);
            final var parentHolding = mock(Holding.class);
            final var expected = TEN;

            when(holdingsDTO.getHolding()).thenReturn(parentHolding);
            when(sameLeaves.get(0).getHolding()).thenReturn(parentHolding);
            when(holdingsDTO.getWeight()).thenReturn(expected);

            doCallRealMethod().when(sut).calculateWeightWithinSameLeaves(anyList(), any());
            //ACT
            final BigDecimal actual = sut.calculateWeightWithinSameLeaves(sameLeaves, parentHolding);

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(TEN));
        }
    }

    @Test
    void calculateWeightWithinSameLeaves_checkResult() {
        //SETUP
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, defaultPeriods, requestValidator));

        final var holdingsDTO = mock(CommonHoldingsDTO.class);
        final var sameLeaves = List.of(holdingsDTO);
        final var parentHolding = mock(Holding.class);
        final var expected = TEN;

        when(holdingsDTO.getHolding()).thenReturn(parentHolding);
        when(sameLeaves.get(0).getHolding()).thenReturn(parentHolding);
        when(holdingsDTO.getWeight()).thenReturn(expected);

        doCallRealMethod().when(sut).calculateWeightWithinSameLeaves(anyList(), any());
        //ACT
        final BigDecimal actual = sut.calculateWeightWithinSameLeaves(sameLeaves, parentHolding);

        //VERIFY
        assertEquals(expected.doubleValue(), actual.doubleValue());
    }

    @Test
    void mapToFinalResult_verifyBuildDTO() {
        try (var mockedParentHoldingDTO = Mockito.mockStatic(ParentHoldingDTO.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var holdingAggregatorDTO = mock(HoldingAggregatorDTO.class);
            final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
            final var leaves = Map.of(holdingAggregatorDTO, List.of(commonHoldingsDTO));
            final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregatorDTO, TEN);

            final var holdingsKeyDTO = mock(HoldingsKeyDTO.class);
            final var holding = mock(Holding.class);

            when(commonHoldingsDTO.getHolding()).thenReturn(holding);
            when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
            when(sut.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
            mockedParentHoldingDTO.when(() -> ParentHoldingDTO.buildDTO(any(), any())).thenReturn(holdingsKeyDTO);

            doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
            //ACT
            sut.mapToFinalResult(leaves, sortedLeafEntry);

            //VERIFY
            mockedParentHoldingDTO.verify(() -> ParentHoldingDTO.buildDTO(holding, TEN));
        }
    }

    @Test
    void mapToFinalResult_verifyCalculateWeightWithinSameLeaves() {
        try (var mockedParentHoldingDTO = Mockito.mockStatic(ParentHoldingDTO.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var holdingAggregatorDTO = mock(HoldingAggregatorDTO.class);
            final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
            final var leaves = Map.of(holdingAggregatorDTO, List.of(commonHoldingsDTO));
            final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregatorDTO, TEN);

            final var holdingsKeyDTO = mock(HoldingsKeyDTO.class);
            final var holding = mock(Holding.class);

            when(commonHoldingsDTO.getHolding()).thenReturn(holding);
            when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
            mockedParentHoldingDTO.when(() -> ParentHoldingDTO.buildDTO(any(), any())).thenReturn(holdingsKeyDTO);

            doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
            //ACT
            sut.mapToFinalResult(leaves, sortedLeafEntry);

            //VERIFY
            verify(sut).calculateWeightWithinSameLeaves(List.of(commonHoldingsDTO), holding);
        }
    }

    @Test
    void mapToFinalResult_checkResult() {
        try (var mockedParentHoldingDTO = Mockito.mockStatic(ParentHoldingDTO.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var holdingAggregatorDTO = new HoldingAggregatorDTO("Tesla", null, null);
            final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
            final var leaves = Map.of(holdingAggregatorDTO, List.of(commonHoldingsDTO));
            final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregatorDTO, TEN);

            final var holdingsKeyDTO = mock(HoldingsKeyDTO.class);
            final var holding = mock(Holding.class);
            final var expected = new TopCommonHoldingsDTO("Tesla", null, null, null, null, toUserScale(TEN), 1, Set.of(holdingsKeyDTO));


            when(commonHoldingsDTO.getHolding()).thenReturn(holding);
            when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
            when(sut.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
            mockedParentHoldingDTO.when(() -> ParentHoldingDTO.buildDTO(any(), any())).thenReturn(holdingsKeyDTO);

            doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
            //ACT
            final TopCommonHoldingsDTO actual = sut.mapToFinalResult(leaves, sortedLeafEntry);

            //VERIFY
            assertEquals(expected, actual);
        }
    }

    @Test
    void mapToFinalResult_checkResult2() {
        try (var mockedParentHoldingDTO = Mockito.mockStatic(ParentHoldingDTO.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var holdingAggregatorDTO = new HoldingAggregatorDTO("Tesla", null, null);
            final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
            final var leaves = Map.of(holdingAggregatorDTO, List.of(commonHoldingsDTO));
            final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregatorDTO, TEN);

            final var holdingsKeyDTO = mock(HoldingsKeyDTO.class);
            final var holding = mock(Holding.class);
            final var expected = new TopCommonHoldingsDTO("Tesla", null, "H", null, null, toUserScale(TEN), 1, Set.of(holdingsKeyDTO));

            when(commonHoldingsDTO.getTicker()).thenReturn("H");
            when(commonHoldingsDTO.getHolding()).thenReturn(holding);
            when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
            when(sut.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
            mockedParentHoldingDTO.when(() -> ParentHoldingDTO.buildDTO(any(), any())).thenReturn(holdingsKeyDTO);

            doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
            //ACT
            final TopCommonHoldingsDTO actual = sut.mapToFinalResult(leaves, sortedLeafEntry);

            //VERIFY
            assertEquals(expected, actual);
        }
    }

    @Test
    void calculateTopCommonHoldings_verifyFirstLevelLeaves() {
        //SETUP
        final var accumulativeTypes = Set.of("E");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulativeTypes, requestValidator));

        final var holding = mock(Holding.class);
        final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
        final var holdings = Map.of(holding, List.of(commonHoldingsDTO));
        final var allocations = Map.of(holding, TEN);

        doCallRealMethod().when(sut).calculateTopCommonHoldings(anyMap(), anyMap(), anySet());
        //ACT
        sut.calculateTopCommonHoldings(holdings, allocations, accumulativeTypes);

        //VERIFY
        verify(sut).firstLevelLeaves(allocations, holding, List.of(commonHoldingsDTO));
    }

    @Test
    void calculateTopCommonHoldings_verifySecondLevelLeaves() {
        //SETUP
        final var accumulativeTypes = Set.of("E");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulativeTypes, requestValidator));

        final var holding = mock(Holding.class);
        final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
        final var holdings = Map.of(holding, List.of(commonHoldingsDTO));
        final var allocations = Map.of(holding, TEN);

        when(sut.firstLevelLeaves(anyMap(), any(), anyList())).thenReturn(Stream.of(commonHoldingsDTO));

        doCallRealMethod().when(sut).calculateTopCommonHoldings(anyMap(), anyMap(), anySet());
        //ACT
        sut.calculateTopCommonHoldings(holdings, allocations, accumulativeTypes);

        //VERIFY
        verify(sut).secondLevelLeaves(commonHoldingsDTO);
    }


    @Test
    void firstLevelLeaves_verifySetParentAndCalculateWeight() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var defaultPeriods = Set.of();
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final var allocations = Map.of(mock(Holding.class), TEN);
        final var parent = mock(Holding.class);
        final var child = mock(CommonHoldingsDTO.class);
        final var firstLevelChildren = List.of(child);

        when(child.getType()).thenReturn("E");
        when(child.getUnderlyingHoldings()).thenReturn(null);
        when(sut.setParentAndCalculateWeight(anyMap(), any(), any())).thenReturn(child);

        doCallRealMethod().when(sut).firstLevelLeaves(anyMap(), any(), anyList());
        //ACT
        final Stream<CommonHoldingsDTO> actual = sut.firstLevelLeaves(allocations, parent, firstLevelChildren);

        //VERIFY
        assertEquals(1, actual.toList().size());
        verify(sut).setParentAndCalculateWeight(allocations, parent, child);
    }

    @Test
    void firstLevelLeaves_checkResult() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final var allocations = Map.of(mock(Holding.class), TEN);
        final var parent = mock(StockHolding.class);
        final var child = mock(CommonHoldingsDTO.class);
        final var firstLevelChildren = List.of(child);
        final var expected = new CommonHoldingsDTO("Apple Inc", null, TEN, null, null);

        when(child.getType()).thenReturn("E");
        when(child.getCompanyName()).thenReturn("Apple Inc");
        when(child.getValue()).thenReturn(TEN);
        when(child.getUnderlyingHoldings()).thenReturn(null);
        when(sut.setParentAndCalculateWeight(anyMap(), any(), any())).thenReturn(child);

        doCallRealMethod().when(sut).firstLevelLeaves(anyMap(), any(), anyList());
        //ACT
        final Stream<CommonHoldingsDTO> actual = sut.firstLevelLeaves(allocations, parent, firstLevelChildren);

        //VERIFY
        assertEquals(expected.getWeight(), actual.findFirst().orElseThrow().getWeight());
    }

    @Test
    void setParentAndCalculateWeightSecondLvlLeaf_checkResult() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var accumulateTypes = Set.of("FE");
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, accumulateTypes, requestValidator));

            final var firstLvlParent = mock(CommonHoldingsDTO.class);
            final var child = mock(CommonHoldingsDTO.class);

            when(child.setHolding(any())).thenReturn(child);
            when(child.setWeight(any())).thenReturn(child);
            when(child.getValue()).thenReturn(TEN);
            when(child.getWeight()).thenReturn(HUNDRED);
            when(firstLvlParent.getWeight()).thenReturn(TEN);
            mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(TEN)).thenReturn(TEN);
            doCallRealMethod().when(sut).setParentAndCalculateWeightSecondLvlLeaf(any(), any());
            //ACT
            final CommonHoldingsDTO actual = sut.setParentAndCalculateWeightSecondLvlLeaf(firstLvlParent, child);

            //VERIFY
            assertEquals(HUNDRED, actual.getWeight());
        }
    }

    @Test
    void isLeafStock_checkResult() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final var parent = mock(StockHolding.class);
        final var child = mock(CommonHoldingsDTO.class);

        when(child.getCompanyName()).thenReturn("test");
        when(child.getType()).thenReturn("E");

        doCallRealMethod().when(sut).isLeafStock(any(), any());
        //ACT
        final boolean actual = sut.isLeafStock(parent, child);

        //VERIFY
        assertTrue(actual);
    }

    @Test
    void isLeafStock_checkResult2() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final var parent = mock(StockHolding.class);
        final var child = mock(CommonHoldingsDTO.class);

        when(child.getCompanyName()).thenReturn("test");
        when(child.getType()).thenReturn("FE");

        doCallRealMethod().when(sut).isLeafStock(any(), any());
        //ACT
        final boolean actual = sut.isLeafStock(parent, child);

        //VERIFY
        assertFalse(actual);
    }

    @Test
    void getTopCommonHoldingsNumber_returnDefault10WhenGetNumOfTopCommonHoldingsIsNull() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final TopCommonHoldingsReqDTO req = mock(TopCommonHoldingsReqDTO.class);
        doReturn(null).when(req).getNumOfTopCommonHoldings();

        doCallRealMethod().when(sut).getTopCommonHoldingsNumber(req);
        //ACT
        final int actual = sut.getTopCommonHoldingsNumber(req);

        //VERIFY
        final int expected = 10;
        assertEquals(expected, actual);
    }

    @Test
    void getTopCommonHoldingsNumber_returnProvidedNumberIfNotNull() {
        //SETUP
        final var accumulateTypes = Set.of("FE");
        final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
        final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
        final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                .useConstructor(cacheStorage, accumulateTypes, requestValidator));

        final TopCommonHoldingsReqDTO req = mock(TopCommonHoldingsReqDTO.class);
        doReturn(11).when(req).getNumOfTopCommonHoldings();

        doCallRealMethod().when(sut).getTopCommonHoldingsNumber(req);
        //ACT
        final int actual = sut.getTopCommonHoldingsNumber(req);

        //VERIFY
        final int expected = 11;
        assertEquals(expected, actual);
    }

    @Test
    void perform_verifyGetTopCommonHoldingsNumber() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var cacheStorage = mock(CommonHoldingsCacheStorage.class);
            final var defaultPeriods = Set.of();
            final var requestValidator = mock(TopCommonHoldingsReqValidator.class);
            final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
                    .useConstructor(cacheStorage, defaultPeriods, requestValidator));

            final var reqDTO = mock(TopCommonHoldingsReqDTO.class);
            final var holdings = List.of(mock(Holding.class));

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(Map.of());

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).getTopCommonHoldingsNumber(reqDTO);
        }
    }
}
