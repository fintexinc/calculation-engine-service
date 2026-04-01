package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.application.mapping.response.CreditQualityResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.CreditQualityRating;
import com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CreditQualityResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.A;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.AA;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.AAA;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.B;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.BB;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.BBB;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.BELOW_B;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.NOT_RATED;
import static com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality.HIGH_YIELD;
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

@SuppressWarnings("unchecked")
class CreditQualityServiceImplTest {

  @Test
  void shouldPerform_whenVerifyLoad() {
    final var creditQualityFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, responseMapper));

    final Holding h = mock(Holding.class);
    final List<Holding> holdings = List.of(h);
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);

    when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of());
    when(reqDTO.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(creditQualityFetcher).fetch(eq(holdings), any());
  }

  @Test
  void shouldPerform_whenVerifyAreAllValuesInMapEmpty() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var creditQualityFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, responseMapper));

      final Holding h = mock(Holding.class);
      when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of());

      final List<Holding> holdings = List.of(h);
      final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
      when(reqDTO.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).perform(any());
      sut.perform(reqDTO);

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(any()));
    }
  }

  @Test
  void shouldPerform_whenVerifyGetFixedIncomeCreditQuality() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var creditQualityFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, responseMapper));

      final Holding h = mock(Holding.class);
      when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of());

      final List<Holding> holdings = List.of(h);
      final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
      when(reqDTO.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).perform(any());
      sut.perform(reqDTO);

      verify(sut).getFixedIncomeCreditQuality(eq(reqDTO), anyList());
    }
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var creditQualityFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, responseMapper));

      final Holding h = mock(Holding.class);
      final List<Holding> holdings = List.of(h);

      final CreditQuality rawCq = new CreditQuality();
      rawCq.setRatings(Map.of());
      when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of(h, rawCq));

      final Map<Holding, BigDecimal> fixed = Map.of(h, TEN);
      when(sut.getFixedIncomeCreditQuality(any(), anyList())).thenReturn(fixed);

      final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
      when(reqDTO.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).perform(any());
      sut.perform(reqDTO);

      verify(sut).calculate(eq(holdings), any(), eq(fixed));
    }
  }

  @Test
  void shouldPerform_whenVerifyResponseMapperFromCalculatedValues() {
    final var creditQualityFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, responseMapper));

    final var holding = mock(Holding.class);
    final CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of("AAA", ONE));
    when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawCq));

    final Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    when(sut.calculate(any(), any(), any())).thenReturn(map);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(mock(PortfolioHoldingsCommand.class));

    verify(responseMapper).fromCalculatedValues(eq(map), anyList());
  }

  @Test
  void shouldPerform_whenCheckResult() {
    final var creditQualityFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, responseMapper));

    final var holding = mock(Holding.class);
    final CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of("AAA", ONE));
    when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawCq));

    final Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    final CreditQualityResult expected = new CreditQualityResult();
    expected.setCreditQuality(map);
    expected.setWarnings(List.of());
    when(responseMapper.fromCalculatedValues(any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(sut).perform(any());
    final CreditQualityResult actual = sut.perform(mock(PortfolioHoldingsCommand.class));

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyLoad() {
    final var creditQualityFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, responseMapper));

    final Holding h = mock(Holding.class);
    final List<Warning> warnings = List.of(mock(Warning.class));
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
    final List<Holding> holdings = List.of(h);
    final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getDataProviders()).thenReturn(providers);
    when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
    when(assetAllocationDataMapper.toRegionExposures(any())).thenReturn(Map.of());

    doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
    sut.getFixedIncomeCreditQuality(reqDTO, warnings);

    verify(assetAllocationFetcher).fetch(eq(holdings), any());
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      final var creditQualityFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var responseMapper = mock(CreditQualityResponseMapper.class);

      final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
          creditQualityFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, responseMapper));

      final var warnings = List.of(mock(Warning.class));
      final var reqDTO = mock(PortfolioHoldingsCommand.class);
      final var providers = List.of(DataProvider.MORNINGSTAR);
      final DataProvider[] specifiedProviders = {DataProvider.MORNINGSTAR};

      when(reqDTO.getDataProviders()).thenReturn(providers);
      when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
      when(assetAllocationDataMapper.toRegionExposures(any())).thenReturn(Map.of());
      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(providers, specifiedProviders)).thenReturn(
          providers);

      doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
      sut.getFixedIncomeCreditQuality(reqDTO, warnings);

      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, specifiedProviders));
    }
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_checkResult() {
    final var creditQualityFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(CreditQualityResponseMapper.class);

    final var sut = mock(CreditQualityServiceImpl.class, withSettings().useConstructor(
        creditQualityFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, responseMapper));

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN,
        AssetAllocationRegion.FIXED_INCOME, HUNDRED);

    when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
    when(assetAllocationDataMapper.toRegionExposures(any())).thenReturn(Map.of(h, asset));

    final List<Warning> warnings = List.of(mock(Warning.class));
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
    final List<Holding> holdings = List.of(h);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    when(reqDTO.getDataProviders()).thenReturn(providers);

    doCallRealMethod().when(sut).getFixedIncomeValue(any());
    doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
    final Map<Holding, BigDecimal> actual = sut.getFixedIncomeCreditQuality(reqDTO, warnings);

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
    final Map<Holding, BigDecimal> actual = Map.of(h, asset).entrySet().stream().collect(toMap(Map.Entry::getKey,
        c::getFixedIncomeValue));

    assertEquals(Map.of(h, HUNDRED), actual);
  }

  @Test
  void shouldGetFixedIncomeValue_whenCheckResult2() {
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN);

    doCallRealMethod().when(c).getFixedIncomeValue(any());
    Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry = Map.of(h, asset).entrySet().iterator().next();
    assertThrows(NoSuchElementException.class, () -> c.getFixedIncomeValue(entry));
  }

  @Test
  void shouldCalculateSumProductRating_whenCheckResult() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Holding h2 = new Holding().setHoldingType(FinancialInstrumentType.CASH);

    final int creditQValue = 2;
    final int fixedIncomeValue = 3;
    final int weightValue = 10;

    final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality = Map.of(h, Map.of(AAA, BigDecimal.valueOf(
        creditQValue)));
    final Map<Holding, BigDecimal> fixedIncomeCreditQuality = Map.of(h, BigDecimal.valueOf(fixedIncomeValue));
    final Map<Holding, BigDecimal> weights = Map.of(h, BigDecimal.valueOf(weightValue), h2, BigDecimal.ONE);

    doCallRealMethod().when(c).calculateSumProductRating(any(), any(), any(), any());
    final BigDecimal actual = c.calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, AAA);

    assertEquals(0, actual.compareTo(BigDecimal.valueOf(creditQValue * fixedIncomeValue * weightValue)));
  }

  @Test
  void shouldCalculateCreditQualityRatings_whenVerifyCalculateInitialPortfolioWeight() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final List<Holding> holdings = List.of(mock(Holding.class));

      when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

      doCallRealMethod().when(sut).calculateCreditQualityRatings(any(), any(), any());
      sut.calculateCreditQualityRatings(holdings, Map.of(), Map.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldCalculateCreditQualityRatings_whenVerifyCalculateSumProductRating() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final Holding h = mock(Holding.class);
      Map<Holding, BigDecimal> weights = Map.of(h, TEN);

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(weights);

      final List<Holding> holdings = List.of(h);
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

      doCallRealMethod().when(sut).calculateCreditQualityRatings(any(), any(), any());
      final Map<CreditQualityRating, BigDecimal> actual = sut.calculateCreditQualityRatings(holdings, creditQ,
          fixedCreditQ);

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
    final Map<FixedIncomeCreditQuality, BigDecimal> actual = c.toFixedIncomeCreditQuality(ratings);

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
    final Map<FixedIncomeCreditQuality, BigDecimal> actual = c.calculate(holdings, creditQ, fixedCreditQ);

    verify(c).calculateCreditQualityRatings(holdings, creditQ, fixedCreditQ);
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final Holding h = mock(Holding.class);

      Map<CreditQualityRating, BigDecimal> rescaled = Map.of(AAA, TEN);

      when(sut.calculateCreditQualityRatings(any(), any(), any())).thenReturn(rescaled);

      final List<Holding> holdings = List.of(h);
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(rescaled));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToFixedIncomeCreditQuality() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final Holding h = mock(Holding.class);

      Map<CreditQualityRating, BigDecimal> rescaled = Map.of(AAA, TEN);

      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(rescaled);

      final List<Holding> holdings = List.of(h);
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<Holding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      verify(sut).toFixedIncomeCreditQuality(rescaled);
    }
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
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
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      assertSame(expected, actual);
    }
  }

}
