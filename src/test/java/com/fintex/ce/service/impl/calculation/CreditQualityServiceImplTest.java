package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.config.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.config.enumeration.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.mapper.AssetAllocationDataMapper;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.CreditQualityResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.AssetAllocationCacheStorage;
import com.fintex.ce.service.impl.cache.CreditQualityCacheStorage;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.fintex.ce.config.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.A;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.AA;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.AAA;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.B;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.BB;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.BBB;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.BELOW_B;
import static com.fintex.ce.config.enumeration.calculation.CreditQualityRating.NOT_RATED;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeCreditQuality.HIGH_YIELD;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CreditQualityServiceImplTest {

    @Test
    void perform_verifyLoad() {
        //SETUP
        final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                creditQualityCacheStorage, assetAllocationCacheStorage,
                assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

        final Holding h = mock(Holding.class);
        final List<Holding> holdings = List.of(h);
        final PortfolioHoldingsReqDTO reqDTO = mock(PortfolioHoldingsReqDTO.class);

        when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(Map.of());
        when(reqDTO.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(creditQualityCacheStorage).load(eq(holdings), any(), anyList(), eq(new ParamHolderDTO()));
    }

    @Test
    void perform_verifyAreAllValuesInMapEmpty() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                    creditQualityCacheStorage, assetAllocationCacheStorage,
                    assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

            final Holding h = mock(Holding.class);
            final Map mockMap = Map.of();
            when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(mockMap);

            final List<Holding> holdings = List.of(h);

            final PortfolioHoldingsReqDTO reqDTO = mock(PortfolioHoldingsReqDTO.class);

            when(reqDTO.getHoldings()).thenReturn(holdings);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(mockMap));
        }
    }

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                creditQualityCacheStorage, assetAllocationCacheStorage,
                assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

        final Holding h = mock(Holding.class);

        when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(Map.of());

        final List<Holding> holdings = List.of(h);

        final PortfolioHoldingsReqDTO reqDTO = mock(PortfolioHoldingsReqDTO.class);

        when(reqDTO.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(requestValidator).validate(reqDTO);

    }

    @Test
    void perform_verifyGetFixedIncomeCreditQuality() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                    creditQualityCacheStorage, assetAllocationCacheStorage,
                    assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

            final Holding h = mock(Holding.class);

            when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(Map.of());

            final List<Holding> holdings = List.of(h);

            final PortfolioHoldingsReqDTO reqDTO = mock(PortfolioHoldingsReqDTO.class);

            when(reqDTO.getHoldings()).thenReturn(holdings);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).getFixedIncomeCreditQuality(eq(reqDTO), anyList());
        }
    }

    @Test
    void perform_verifyCalculate() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                    creditQualityCacheStorage, assetAllocationCacheStorage,
                    assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

            final Holding h = mock(Holding.class);
            final List<Holding> holdings = List.of(h);

            final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality = Map.of(h, Map.of());
            when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(creditQuality);

            final Map<Holding, BigDecimal> fixed = Map.of(h, TEN);
            when(sut.getFixedIncomeCreditQuality(any(), anyList())).thenReturn(fixed);

            final PortfolioHoldingsReqDTO reqDTO = mock(PortfolioHoldingsReqDTO.class);

            when(reqDTO.getHoldings()).thenReturn(holdings);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).calculate(holdings, creditQuality, fixed);
        }
    }

    @Test
    void perform_verifyToUserScale() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class);
             var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                    creditQualityCacheStorage, assetAllocationCacheStorage,
                    assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));


            final Holding h = mock(Holding.class);

            when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(Map.of());

            final Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
            when(sut.calculate(any(), any(), any())).thenReturn(map);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(mock(PortfolioHoldingsReqDTO.class));

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(map));
        }
    }

    @Test
    void perform_checkResult() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class);
             var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                    creditQualityCacheStorage, assetAllocationCacheStorage,
                    assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

            final Holding h = mock(Holding.class);

            when(creditQualityCacheStorage.load(any(), any(), anyList(), any())).thenReturn(Map.of());

            final Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
            mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(anyMap())).thenReturn(map);
            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(anyMap())).thenReturn(false);

            DecimalUtils.toUserScale(map);

            doCallRealMethod().when(sut).perform(any());
            //ACT
            final CreditQualityResDTO actual = sut.perform(mock(PortfolioHoldingsReqDTO.class));

            //VERIFY
            assertEquals(map, actual.getCreditQuality());
            Assertions.assertEquals(List.of(), actual.getWarnings());
        }
    }

    @Test
    void getFixedIncomeCreditQuality_verifyLoad() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                    creditQualityCacheStorage, assetAllocationCacheStorage,
                    assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

            final Holding h = mock(Holding.class);

            final List<Warning> warnings = List.of(mock(Warning.class));
            final PortfolioHoldingsReqDTO reqDTO = mock(PortfolioHoldingsReqDTO.class);
            final List<Holding> holdings = List.of(h);
            final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

            when(reqDTO.getHoldings()).thenReturn(holdings);
            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(any(), any())).thenReturn(providers);

            doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
            //ACT
            sut.getFixedIncomeCreditQuality(reqDTO, warnings);

            //VERIFY
            verify(assetAllocationCacheStorage).load(holdings, providers, warnings, new ParamHolderDTO());
        }
    }

    @Test
    void getFixedIncomeCreditQuality_verifyGetSpecifiedIfEmpty() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                    creditQualityCacheStorage, assetAllocationCacheStorage,
                    assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

            final var warnings = List.of(mock(Warning.class));
            final var reqDTO = mock(PortfolioHoldingsReqDTO.class);
            final var providers = List.of(DataProvider.MORNINGSTAR);
            final DataProvider[] specifiedProviders = {DataProvider.MORNINGSTAR, DataProvider.EAGLE};

            when(reqDTO.getDataProviders()).thenReturn(providers);
            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(providers, specifiedProviders)).thenReturn(providers);

            doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
            //ACT
            sut.getFixedIncomeCreditQuality(reqDTO, warnings);

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, specifiedProviders));
        }
    }

    @Test
    void getFixedIncomeCreditQuality_verifyValidate() {
        //SETUP
        final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                creditQualityCacheStorage, assetAllocationCacheStorage,
                assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

        final var req = mock(PortfolioHoldingsReqDTO.class);
        final List<Warning> warnings = List.of();
        final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
        when(assetAllocationCacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(assetAllocationDataDto);

        doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), any());
        //ACT
        sut.getFixedIncomeCreditQuality(req, warnings);

        //VERIFY
        verify(assetAllocationDataValidator).validate(assetAllocationDataDto, warnings);
    }

    @Test
    void getFixedIncomeCreditQuality_verifyMapForAA() {
        //SETUP
        final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                creditQualityCacheStorage, assetAllocationCacheStorage,
                assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

        final var req = mock(PortfolioHoldingsReqDTO.class);
        final List<Warning> warnings = List.of();
        final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
        when(assetAllocationCacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(assetAllocationDataDto);

        doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), any());
        //ACT
        sut.getFixedIncomeCreditQuality(req, warnings);

        //VERIFY
        verify(assetAllocationDataMapper).mapForAA(assetAllocationDataDto);
    }

    @Test
    void getFixedIncomeCreditQuality_checkResult() {
        //SETUP
        final var creditQualityCacheStorage = mock(CreditQualityCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
                creditQualityCacheStorage, assetAllocationCacheStorage,
                assetAllocationDataValidator, assetAllocationDataMapper, requestValidator));

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN, AssetAllocationRegion.FIXED_INCOME, HUNDRED);

        final var assetAllocationDataDTO = mock(AssetAllocationDataDTO.class);
        when(assetAllocationCacheStorage.load(any(), any(), anyList(), any())).thenReturn(assetAllocationDataDTO);
        final var expected = Map.of(h, asset);
        when(assetAllocationDataMapper.mapForAA(assetAllocationDataDTO)).thenReturn(expected);

        final List<Warning> warnings = List.of(mock(Warning.class));
        final PortfolioHoldingsReqDTO reqDTO = mock(PortfolioHoldingsReqDTO.class);
        final List<Holding> holdings = List.of(h);
        when(reqDTO.getHoldings()).thenReturn(holdings);
        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
        when(reqDTO.getDataProviders()).thenReturn(providers);

        doCallRealMethod().when(sut).getFixedIncomeValue(any());
        doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
        //ACT
        final Map<Holding, BigDecimal> actual = sut.getFixedIncomeCreditQuality(reqDTO, warnings);

        //VERIFY
        assertEquals(Map.of(h, HUNDRED), actual);
    }

    @Test
    void getFixedIncomeValue_checkResult() {
        //SETUP
        final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN, AssetAllocationRegion.FIXED_INCOME, HUNDRED);

        doCallRealMethod().when(c).getFixedIncomeValue(any());
        //ACT
        final Map<Holding, BigDecimal> actual = Map.of(h, asset).entrySet().stream().collect(toMap(Map.Entry::getKey, c::getFixedIncomeValue));

        //VERIFY
        assertEquals(Map.of(h, HUNDRED), actual);
    }

    @Test
    void getFixedIncomeValue_checkResult2() {
        //SETUP
        final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN);

        doCallRealMethod().when(c).getFixedIncomeValue(any());
        //ACT
        Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry =
                Map.of(h, asset).entrySet().iterator().next();
        assertThrows(NoSuchElementException.class, () -> c.getFixedIncomeValue(entry));

        //VERIFY
    }

    @Test
    void calculateSumProductRating_checkResult() {
        //SETUP
        final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Holding h2 = new Holding().setType(HoldingType.CASH);

        final int creditQValue = 2;
        final int fixedIncomeValue = 3;
        final int weightValue = 10;

        final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality = Map.of(h, Map.of(AAA, BigDecimal.valueOf(creditQValue)));
        final Map<Holding, BigDecimal> fixedIncomeCreditQuality = Map.of(h, BigDecimal.valueOf(fixedIncomeValue));
        final Map<Holding, BigDecimal> weights = Map.of(h, BigDecimal.valueOf(weightValue), h2, BigDecimal.ONE);

        doCallRealMethod().when(c).calculateSumProductRating(any(), any(), any(), any());
        //ACT
        final BigDecimal actual = c.calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, AAA);

        //VERIFY
        assertEquals(0, actual.compareTo(BigDecimal.valueOf(creditQValue * fixedIncomeValue * weightValue)));
    }

    @Test
    void calculateCreditQualityRatings_verifyCalculateInitialPortfolioWeight() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

            final List<Holding> holdings = List.of(mock(Holding.class));

            when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

            doCallRealMethod().when(sut).calculateCreditQualityRatings(any(), any(), any());
            //ACT
            sut.calculateCreditQualityRatings(holdings, Map.of(), Map.of());

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
        }
    }

    @Test
    void calculateCreditQualityRatings_verifyCalculateSumProductRating() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

            final Holding h = mock(Holding.class);
            Map<Holding, BigDecimal> weights = Map.of(h, TEN);

            mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(weights);

            final List<Holding> holdings = List.of(h);
            final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
            final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

            when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

            doCallRealMethod().when(sut).calculateCreditQualityRatings(any(), any(), any());
            //ACT
            final Map<CreditQualityRating, BigDecimal> actual = sut.calculateCreditQualityRatings(holdings, creditQ, fixedCreditQ);

            //VERIFY
            for (CreditQualityRating rating : CreditQualityRating.values()) {
                verify(sut).calculateSumProductRating(creditQ, fixedCreditQ, weights, rating);
            }
            assertEquals(CreditQualityRating.values().length, actual.size());
        }
    }

    @Test
    void toFixedIncomeCreditQuality_checkResult() {
        //SETUP
        final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

        final Map<CreditQualityRating, BigDecimal> ratings = Map.of(
                AAA, BigDecimal.valueOf(100),
                AA, BigDecimal.valueOf(2),
                A, BigDecimal.valueOf(3),
                BBB, BigDecimal.valueOf(4),
                BB, BigDecimal.valueOf(5),
                B, BigDecimal.valueOf(60),
                BELOW_B, BigDecimal.valueOf(7),
                NOT_RATED, BigDecimal.valueOf(80)
        );

        doCallRealMethod().when(c).toFixedIncomeCreditQuality(any());
        //ACT
        final Map<FixedIncomeCreditQuality, BigDecimal> actual = c.toFixedIncomeCreditQuality(ratings);

        //VERIFY
        Map<FixedIncomeCreditQuality, BigDecimal> expected = Map.of(
                FixedIncomeCreditQuality.AAA, BigDecimal.valueOf(100),
                FixedIncomeCreditQuality.AA, BigDecimal.valueOf(2),
                FixedIncomeCreditQuality.A, BigDecimal.valueOf(3),
                FixedIncomeCreditQuality.BBB, BigDecimal.valueOf(4),
                FixedIncomeCreditQuality.BB, BigDecimal.valueOf(5),
                FixedIncomeCreditQuality.B, BigDecimal.valueOf(60),
                FixedIncomeCreditQuality.BELOW_B, BigDecimal.valueOf(7),
                FixedIncomeCreditQuality.INVESTMENT_GRADE, BigDecimal.valueOf(100 + 2 + 3 + 4),
                FixedIncomeCreditQuality.HIGH_YIELD, BigDecimal.valueOf(5 + 60 + 7),
                FixedIncomeCreditQuality.NOT_RATED, BigDecimal.valueOf(80)
        );
        assertEquals(expected, actual);
    }

    @Test
    void calculate_verifyCalculateCreditQualityRatings() {
        //SETUP
        final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

        final Holding h = mock(Holding.class);
        final List<Holding> holdings = List.of(h);
        final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
        final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

        doCallRealMethod().when(c).calculate(any(), any(), any());
        //ACT
        final Map<FixedIncomeCreditQuality, BigDecimal> actual = c.calculate(holdings, creditQ, fixedCreditQ);

        //VERIFY
        verify(c).calculateCreditQualityRatings(holdings, creditQ, fixedCreditQ);
    }

    @Test
    void calculate_verifyReScale() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
            //SETUP
            final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

            final Holding h = mock(Holding.class);

            Map<CreditQualityRating, BigDecimal> rescaled = Map.of(AAA, TEN);

            when(sut.calculateCreditQualityRatings(any(), any(), any())).thenReturn(rescaled);

            final List<Holding> holdings = List.of(h);
            final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
            final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

            //VERIFY
            mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(rescaled));
        }
    }

    @Test
    void calculate_verifyToFixedIncomeCreditQuality() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
            //SETUP
            final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

            final Holding h = mock(Holding.class);

            Map<CreditQualityRating, BigDecimal> rescaled = Map.of(AAA, TEN);

            mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(rescaled);

            final List<Holding> holdings = List.of(h);
            final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
            final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

            //VERIFY
            verify(sut).toFixedIncomeCreditQuality(rescaled);
        }
    }

    @Test
    void calculate_checkResult() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
            //SETUP
            final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

            final Holding h = mock(Holding.class);

            Map<CreditQualityRating, BigDecimal> rescaled = Map.of(AAA, TEN);

            mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(rescaled);

            final HashMap<FixedIncomeCreditQuality, BigDecimal> expected = new HashMap<>();
            when(sut.toFixedIncomeCreditQuality(rescaled)).thenReturn(expected);

            final List<Holding> holdings = List.of(h);
            final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
            final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}