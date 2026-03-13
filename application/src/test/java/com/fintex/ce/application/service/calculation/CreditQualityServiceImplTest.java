package com.fintex.ce.application.service.calculation;

import com.fintex.ce.port.output.cache.AssetAllocationCachePort;
import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.application.mapper.response.CreditQualityResponseMapper;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.CreditQualityResult;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.A;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.AA;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.AAA;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.B;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.BB;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.BBB;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.BELOW_B;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.NOT_RATED;
import static com.fintex.ce.domain.enumeration.calculation.FixedIncomeCreditQuality.HIGH_YIELD;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CreditQualityServiceImplTest {

  @Test
  void shouldPerform_whenVerifyLoad() {
    // SETUP
    final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
    final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityCacheStorage, assetAllocationCacheStorage,
        assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

    final Holding h = mock(Holding.class);
    final List<Holding> holdings = List.of(h);
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);

    when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(Map.of());
    when(reqDTO.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(creditQualityCacheStorage).load(eq(holdings), any(), anyList(), eq(new ParamHolderDTO()));
  }

  @Test
  void shouldPerform_whenVerifyAreAllValuesInMapEmpty() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
      final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityCacheStorage, assetAllocationCacheStorage,
          assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

      final Holding h = mock(Holding.class);
      final Map mockMap = Map.of();
      when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(mockMap);

      final List<Holding> holdings = List.of(h);

      final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);

      when(reqDTO.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(mockMap));
    }
  }

  @Test
  void shouldPerform_whenVerifyGetFixedIncomeCreditQuality() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
      final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityCacheStorage, assetAllocationCacheStorage,
          assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

      final Holding h = mock(Holding.class);

      when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(Map.of());

      final List<Holding> holdings = List.of(h);

      final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);

      when(reqDTO.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).getFixedIncomeCreditQuality(eq(reqDTO), anyList());
    }
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
      final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityCacheStorage, assetAllocationCacheStorage,
          assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

      final Holding h = mock(Holding.class);
      final List<Holding> holdings = List.of(h);

      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality = Map.of(h, Map.of());
      when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(creditQuality);

      final Map<Holding, BigDecimal> fixed = Map.of(h, TEN);
      when(sut.getFixedIncomeCreditQuality(any(), anyList())).thenReturn(fixed);

      final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);

      when(reqDTO.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).calculate(holdings, creditQuality, fixed);
    }
  }

  @Test
  void shouldPerform_whenVerifyResponseMapperFromCalculatedValues() {
    // SETUP
    final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
    final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityCacheStorage, assetAllocationCacheStorage,
        assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

    final var holding = mock(Holding.class);
    final var creditQuality = Map.of(holding, Map.of(CreditQualityRating.AAA, ONE));
    when(creditQualityCacheStorage.load(any(), any(), any(), any())).thenReturn(creditQuality);

    final Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    when(sut.calculate(any(), any(), any())).thenReturn(map);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(mock(PortfolioHoldingsCommand.class));

    // VERIFY
    verify(responseMapper).fromCalculatedValues(eq(map), anyList());
  }

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
    final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityCacheStorage, assetAllocationCacheStorage,
        assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

    final var holding = mock(Holding.class);
    final var creditQuality = Map.of(holding, Map.of(CreditQualityRating.AAA, ONE));
    when(creditQualityCacheStorage.load(any(), any(), anyList(), any())).thenReturn(creditQuality);

    final Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    final CreditQualityResult expected = new CreditQualityResult();
    expected.setCreditQuality(map);
    expected.setWarnings(List.of());
    when(responseMapper.fromCalculatedValues(any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final CreditQualityResult actual = sut.perform(mock(PortfolioHoldingsCommand.class));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyLoad() {
    // SETUP
    final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
    final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityCacheStorage, assetAllocationCacheStorage,
        assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

    final Holding h = mock(Holding.class);

    final List<Warning> warnings = List.of(mock(Warning.class));
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
    final List<Holding> holdings = List.of(h);
    final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getDataProviders()).thenReturn(providers);

    doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
    // ACT
    sut.getFixedIncomeCreditQuality(reqDTO, warnings);

    // VERIFY
    verify(assetAllocationCacheStorage).load(holdings, providers, warnings, new ParamHolderDTO());
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
      final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityCacheStorage, assetAllocationCacheStorage,
          assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

      final var warnings = List.of(mock(Warning.class));
      final var reqDTO = mock(PortfolioHoldingsCommand.class);
      final var providers = List.of(DataProvider.MORNINGSTAR);
      final DataProvider[] specifiedProviders = {DataProvider.MORNINGSTAR, DataProvider.EAGLE};

      when(reqDTO.getDataProviders()).thenReturn(providers);
      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(providers, specifiedProviders)).thenReturn(
          providers);

      doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
      // ACT
      sut.getFixedIncomeCreditQuality(reqDTO, warnings);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, specifiedProviders));
    }
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyValidate() {
    // SETUP
    final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
    final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityCacheStorage, assetAllocationCacheStorage,
        assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

    final var req = mock(PortfolioHoldingsCommand.class);
    final List<Warning> warnings = List.of();
    final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
    when(assetAllocationCacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(assetAllocationDataDto);

    doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), any());
    // ACT
    sut.getFixedIncomeCreditQuality(req, warnings);

    // VERIFY
    verify(assetAllocationDataValidator).validate(assetAllocationDataDto, warnings);
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyMapForAA() {
    // SETUP
    final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
    final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityCacheStorage, assetAllocationCacheStorage,
        assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

    final var req = mock(PortfolioHoldingsCommand.class);
    final List<Warning> warnings = List.of();
    final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
    when(assetAllocationCacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(assetAllocationDataDto);

    doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), any());
    // ACT
    sut.getFixedIncomeCreditQuality(req, warnings);

    // VERIFY
    verify(assetAllocationDataMapper).mapForAA(assetAllocationDataDto);
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenCheckResult() {
    // SETUP
    final var creditQualityCacheStorage = mock(HoldingDataLoader.class);
    final var assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityCacheStorage, assetAllocationCacheStorage,
        assetAllocationDataValidator, assetAllocationDataMapper, responseMapper));

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN,
        AssetAllocationRegion.FIXED_INCOME, HUNDRED);

    final var assetAllocationDataDTO = mock(AssetAllocationDataDTO.class);
    when(assetAllocationCacheStorage.load(any(), any(), anyList(), any())).thenReturn(assetAllocationDataDTO);
    final var expected = Map.of(h, asset);
    when(assetAllocationDataMapper.mapForAA(assetAllocationDataDTO)).thenReturn(expected);

    final List<Warning> warnings = List.of(mock(Warning.class));
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
    final List<Holding> holdings = List.of(h);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    when(reqDTO.getDataProviders()).thenReturn(providers);

    doCallRealMethod().when(sut).getFixedIncomeValue(any());
    doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
    // ACT
    final Map<Holding, BigDecimal> actual = sut.getFixedIncomeCreditQuality(reqDTO, warnings);

    // VERIFY
    assertEquals(Map.of(h, HUNDRED), actual);
  }

  @Test
  void shouldGetFixedIncomeValue_whenCheckResult() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN,
        AssetAllocationRegion.FIXED_INCOME, HUNDRED);

    doCallRealMethod().when(c).getFixedIncomeValue(any());
    // ACT
    final Map<Holding, BigDecimal> actual = Map.of(h, asset).entrySet().stream().collect(toMap(Map.Entry::getKey,
        c::getFixedIncomeValue));

    // VERIFY
    assertEquals(Map.of(h, HUNDRED), actual);
  }

  @Test
  void shouldGetFixedIncomeValue_whenCheckResult2() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN);

    doCallRealMethod().when(c).getFixedIncomeValue(any());
    // ACT
    Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry = Map.of(h, asset).entrySet().iterator().next();
    assertThrows(NoSuchElementException.class, () -> c.getFixedIncomeValue(entry));

    // VERIFY
  }

  @Test
  void shouldCalculateSumProductRating_whenCheckResult() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Holding h2 = new Holding().setType(HoldingType.CASH);

    final int creditQValue = 2;
    final int fixedIncomeValue = 3;
    final int weightValue = 10;

    final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality = Map.of(h, Map.of(AAA, BigDecimal.valueOf(
        creditQValue)));
    final Map<Holding, BigDecimal> fixedIncomeCreditQuality = Map.of(h, BigDecimal.valueOf(fixedIncomeValue));
    final Map<Holding, BigDecimal> weights = Map.of(h, BigDecimal.valueOf(weightValue), h2, BigDecimal.ONE);

    doCallRealMethod().when(c).calculateSumProductRating(any(), any(), any(), any());
    // ACT
    final BigDecimal actual = c.calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, AAA);

    // VERIFY
    assertEquals(0, actual.compareTo(BigDecimal.valueOf(creditQValue * fixedIncomeValue * weightValue)));
  }

  @Test
  void shouldCalculateCreditQualityRatings_whenVerifyCalculateInitialPortfolioWeight() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final List<Holding> holdings = List.of(mock(Holding.class));

      when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

      doCallRealMethod().when(sut).calculateCreditQualityRatings(any(), any(), any());
      // ACT
      sut.calculateCreditQualityRatings(holdings, Map.of(), Map.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldCalculateCreditQualityRatings_whenVerifyCalculateSumProductRating() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final Holding h = mock(Holding.class);
      Map<Holding, BigDecimal> weights = Map.of(h, TEN);

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(weights);

      final List<Holding> holdings = List.of(h);
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

      doCallRealMethod().when(sut).calculateCreditQualityRatings(any(), any(), any());
      // ACT
      final Map<CreditQualityRating, BigDecimal> actual = sut.calculateCreditQualityRatings(holdings, creditQ,
          fixedCreditQ);

      // VERIFY
      for (CreditQualityRating rating : CreditQualityRating.values()) {
        verify(sut).calculateSumProductRating(creditQ, fixedCreditQ, weights, rating);
      }
      assertEquals(CreditQualityRating.values().length, actual.size());
    }
  }

  @Test
  void shouldToFixedIncomeCreditQuality_whenCheckResult() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Map<CreditQualityRating, BigDecimal> ratings = Map.of(
        AAA, BigDecimal.valueOf(100),
        AA, BigDecimal.valueOf(2),
        A, BigDecimal.valueOf(3),
        BBB, BigDecimal.valueOf(4),
        BB, BigDecimal.valueOf(5),
        B, BigDecimal.valueOf(60),
        BELOW_B, BigDecimal.valueOf(7),
        NOT_RATED, BigDecimal.valueOf(80));

    doCallRealMethod().when(c).toFixedIncomeCreditQuality(any());
    // ACT
    final Map<FixedIncomeCreditQuality, BigDecimal> actual = c.toFixedIncomeCreditQuality(ratings);

    // VERIFY
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
        FixedIncomeCreditQuality.NOT_RATED, BigDecimal.valueOf(80));
    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateCreditQualityRatings() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Holding h = mock(Holding.class);
    final List<Holding> holdings = List.of(h);
    final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
    final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

    doCallRealMethod().when(c).calculate(any(), any(), any());
    // ACT
    final Map<FixedIncomeCreditQuality, BigDecimal> actual = c.calculate(holdings, creditQ, fixedCreditQ);

    // VERIFY
    verify(c).calculateCreditQualityRatings(holdings, creditQ, fixedCreditQ);
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      // SETUP
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final Holding h = mock(Holding.class);

      Map<CreditQualityRating, BigDecimal> rescaled = Map.of(AAA, TEN);

      when(sut.calculateCreditQualityRatings(any(), any(), any())).thenReturn(rescaled);

      final List<Holding> holdings = List.of(h);
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(rescaled));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToFixedIncomeCreditQuality() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      // SETUP
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final Holding h = mock(Holding.class);

      Map<CreditQualityRating, BigDecimal> rescaled = Map.of(AAA, TEN);

      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(rescaled);

      final List<Holding> holdings = List.of(h);
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      // VERIFY
      verify(sut).toFixedIncomeCreditQuality(rescaled);
    }
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      // SETUP
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
      // ACT
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}