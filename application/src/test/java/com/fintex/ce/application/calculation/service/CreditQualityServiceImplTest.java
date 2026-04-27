package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.application.mapping.response.CreditQualityResponseMapper;
import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.CreditQualityResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality.HIGH_YIELD;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.A;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.AA;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.AAA;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.B;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.BB;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.BBB;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.BELOW_B;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.NOT_RATED;
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
        assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final List<PortfolioHolding> holdings = List.of(h);
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
          assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

      final PortfolioHolding h = mock(PortfolioHolding.class);
      when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of());

      final List<PortfolioHolding> holdings = List.of(h);
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
          assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

      final PortfolioHolding h = mock(PortfolioHolding.class);
      when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of());

      final List<PortfolioHolding> holdings = List.of(h);
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
          assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

      final PortfolioHolding h = mock(PortfolioHolding.class);
      final List<PortfolioHolding> holdings = List.of(h);

      final CreditQuality rawCq = new CreditQuality();
      rawCq.setRatings(Map.of());
      when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of(h, rawCq));

      final Map<PortfolioHolding, BigDecimal> fixed = Map.of(h, TEN);
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
        assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

    final var holding = mock(PortfolioHolding.class);
    final CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of(AAA, ONE));
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
        assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

    final var holding = mock(PortfolioHolding.class);
    final CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of(AAA, ONE));
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
        assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final List<Warning> warnings = List.of(mock(Warning.class));
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
    final List<PortfolioHolding> holdings = List.of(h);
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
          assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

      final var warnings = List.of(mock(Warning.class));
      final var reqDTO = mock(PortfolioHoldingsCommand.class);
      final var providers = List.of(DataProvider.MORNINGSTAR);
      final List<DataProvider> defaultProviders = List.of(DataProvider.MORNINGSTAR);

      when(reqDTO.getDataProviders()).thenReturn(providers);
      when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
      when(assetAllocationDataMapper.toRegionExposures(any())).thenReturn(Map.of());
      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(providers, defaultProviders)).thenReturn(
          providers);

      doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
      sut.getFixedIncomeCreditQuality(reqDTO, warnings);

      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, defaultProviders));
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
        assetAllocationDataMapper, responseMapper, DEFAULT_DATA_PROPERTIES));

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN,
        AssetAllocationRegion.FIXED_INCOME, HUNDRED);

    when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
    when(assetAllocationDataMapper.toRegionExposures(any())).thenReturn(Map.of(h, asset));

    final List<Warning> warnings = List.of(mock(Warning.class));
    final PortfolioHoldingsCommand reqDTO = mock(PortfolioHoldingsCommand.class);
    final List<PortfolioHolding> holdings = List.of(h);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    when(reqDTO.getDataProviders()).thenReturn(providers);

    doCallRealMethod().when(sut).getFixedIncomeValue(any());
    doCallRealMethod().when(sut).getFixedIncomeCreditQuality(any(), anyList());
    final Map<PortfolioHolding, BigDecimal> actual = sut.getFixedIncomeCreditQuality(reqDTO, warnings);

    assertEquals(Map.of(h, HUNDRED), actual);
  }

  @Test
  void shouldGetFixedIncomeValue_whenCheckResult() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN,
        AssetAllocationRegion.FIXED_INCOME, HUNDRED);

    doCallRealMethod().when(c).getFixedIncomeValue(any());
    final Map<PortfolioHolding, BigDecimal> actual = Map.of(h, asset).entrySet().stream().collect(toMap(
        Map.Entry::getKey,
        c::getFixedIncomeValue));

    assertEquals(Map.of(h, HUNDRED), actual);
  }

  @Test
  void shouldGetFixedIncomeValue_whenCheckResult2() {
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final Map<AssetAllocationRegion, BigDecimal> asset = Map.of(AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN);

    doCallRealMethod().when(c).getFixedIncomeValue(any());
    Map.Entry<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> entry = Map.of(h, asset).entrySet().iterator()
        .next();
    assertThrows(NoSuchElementException.class, () -> c.getFixedIncomeValue(entry));
  }

  @Test
  void shouldCalculateSumProductRating_whenCheckResult() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final PortfolioHolding h2 = new PortfolioHolding(null, FinancialInstrumentType.CASH, null);

    final int creditQValue = 2;
    final int fixedIncomeValue = 3;
    final int weightValue = 10;

    final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQuality = Map.of(h, Map.of(AAA,
        BigDecimal
            .valueOf(
                creditQValue)));
    final Map<PortfolioHolding, BigDecimal> fixedIncomeCreditQuality = Map.of(h, BigDecimal.valueOf(fixedIncomeValue));
    final Map<PortfolioHolding, BigDecimal> weights = Map.of(h, BigDecimal.valueOf(weightValue), h2, BigDecimal.ONE);

    doCallRealMethod().when(c).calculateSumProductRating(any(), any(), any(), any());
    final BigDecimal actual = c.calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, AAA);

    assertEquals(0, actual.compareTo(BigDecimal.valueOf(creditQValue * fixedIncomeValue * weightValue)));
  }

  @Test
  void shouldCalculateCreditQualityRatingTypes_whenVerifyCalculateInitialPortfolioWeight() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final List<PortfolioHolding> holdings = List.of(mock(PortfolioHolding.class));

      when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

      doCallRealMethod().when(sut).calculateCreditQualityRatingTypes(any(), any(), any());
      sut.calculateCreditQualityRatingTypes(holdings, Map.of(), Map.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldCalculateCreditQualityRatingTypes_whenVerifyCalculateSumProductRating() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final PortfolioHolding h = mock(PortfolioHolding.class);
      Map<PortfolioHolding, BigDecimal> weights = Map.of(h, TEN);

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(weights);

      final List<PortfolioHolding> holdings = List.of(h);
      final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<PortfolioHolding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      when(sut.calculateSumProductRating(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

      doCallRealMethod().when(sut).calculateCreditQualityRatingTypes(any(), any(), any());
      final Map<CreditQualityRatingType, BigDecimal> actual = sut.calculateCreditQualityRatingTypes(holdings, creditQ,
          fixedCreditQ);

      for (CreditQualityRatingType rating : CreditQualityRatingType.values()) {
        verify(sut).calculateSumProductRating(creditQ, fixedCreditQ, weights, rating);
      }
      assertEquals(CreditQualityRatingType.values().length, actual.size());
    }
  }

  @Test
  void shouldToFixedIncomeCreditQuality_whenCheckResult() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final Map<CreditQualityRatingType, BigDecimal> ratings = Map.of(
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
  void shouldCalculate_whenVerifyCalculateCreditQualityRatingTypes() {
    // SETUP
    final CreditQualityServiceImpl c = mock(CreditQualityServiceImpl.class);

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final List<PortfolioHolding> holdings = List.of(h);
    final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQ = Map.of(h, Map.of());
    final Map<PortfolioHolding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

    doCallRealMethod().when(c).calculate(any(), any(), any());
    final Map<FixedIncomeCreditQuality, BigDecimal> actual = c.calculate(holdings, creditQ, fixedCreditQ);

    verify(c).calculateCreditQualityRatingTypes(holdings, creditQ, fixedCreditQ);
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final PortfolioHolding h = mock(PortfolioHolding.class);

      Map<CreditQualityRatingType, BigDecimal> rescaled = Map.of(AAA, TEN);

      when(sut.calculateCreditQualityRatingTypes(any(), any(), any())).thenReturn(rescaled);

      final List<PortfolioHolding> holdings = List.of(h);
      final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<PortfolioHolding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(rescaled));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToFixedIncomeCreditQuality() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final PortfolioHolding h = mock(PortfolioHolding.class);

      Map<CreditQualityRatingType, BigDecimal> rescaled = Map.of(AAA, TEN);

      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(rescaled);

      final List<PortfolioHolding> holdings = List.of(h);
      final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<PortfolioHolding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      verify(sut).toFixedIncomeCreditQuality(rescaled);
    }
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final CreditQualityServiceImpl sut = mock(CreditQualityServiceImpl.class);

      final PortfolioHolding h = mock(PortfolioHolding.class);

      Map<CreditQualityRatingType, BigDecimal> rescaled = Map.of(AAA, TEN);

      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(rescaled);

      final HashMap<FixedIncomeCreditQuality, BigDecimal> expected = new HashMap<>();
      when(sut.toFixedIncomeCreditQuality(rescaled)).thenReturn(expected);

      final List<PortfolioHolding> holdings = List.of(h);
      final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQ = Map.of(h, Map.of());
      final Map<PortfolioHolding, BigDecimal> fixedCreditQ = Map.of(h, ONE);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final Map<FixedIncomeCreditQuality, BigDecimal> actual = sut.calculate(holdings, creditQ, fixedCreditQ);

      assertSame(expected, actual);
    }
  }

}
