package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.CreditQualityResponseMapper;
import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.CreditQualityResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class FixedIncomeCreditQualityServiceTest {

  private FixedIncomeCreditQualityService mockService(SecurityDataFetcher<CreditQuality> creditQualityFetcher,
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher,
      CreditQualityResponseMapper responseMapper) {
    return mock(FixedIncomeCreditQualityService.class, withSettings().useConstructor(
        creditQualityFetcher, assetAllocationFetcher, responseMapper, DEFAULT_DATA_PROPERTIES));
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    SecurityDataFetcher<CreditQuality> creditQualityFetcher = mock(SecurityDataFetcher.class);
    SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(creditQualityFetcher, assetAllocationFetcher, responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    List<PortfolioHolding> holdings = List.of(holding);
    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of());
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getDataProviders()).thenReturn(providers);

    doCallRealMethod().when(service).perform(any());
    service.perform(command);

    verify(creditQualityFetcher).fetch(eq(holdings), eq(providers));
  }

  @Test
  void shouldPerform_whenVerifyAreAllValuesInMapEmpty() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      SecurityDataFetcher<CreditQuality> creditQualityFetcher = mock(SecurityDataFetcher.class);
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
      CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
      FixedIncomeCreditQualityService service = mockService(creditQualityFetcher, assetAllocationFetcher,
          responseMapper);

      PortfolioHolding holding = mock(PortfolioHolding.class);
      when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of());
      PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
      when(command.getHoldings()).thenReturn(List.of(holding));

      doCallRealMethod().when(service).perform(any());
      service.perform(command);

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(any()));
    }
  }

  @Test
  void shouldPerform_whenVerifyResponseMapperFromCalculatedValues() {
    SecurityDataFetcher<CreditQuality> creditQualityFetcher = mock(SecurityDataFetcher.class);
    SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(creditQualityFetcher, assetAllocationFetcher, responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of(AAA, ONE));
    when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawCq));

    Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    when(service.calculate(any(), any(), any())).thenReturn(map);

    doCallRealMethod().when(service).perform(any());
    service.perform(mock(PortfolioHoldingsCommand.class));

    verify(responseMapper).fromCalculatedValues(eq(map), anyList());
  }

  @Test
  void shouldPerform_whenCheckResult() {
    SecurityDataFetcher<CreditQuality> creditQualityFetcher = mock(SecurityDataFetcher.class);
    SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(creditQualityFetcher, assetAllocationFetcher, responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of(AAA, ONE));
    when(creditQualityFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawCq));

    Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    CreditQualityResult expected = CreditQualityResult.builder()
        .creditQuality(map)
        .warnings(List.of())
        .build();
    when(responseMapper.fromCalculatedValues(any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(service).perform(any());
    CreditQualityResult actual = service.perform(mock(PortfolioHoldingsCommand.class));

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyLoad() {
    SecurityDataFetcher<CreditQuality> creditQualityFetcher = mock(SecurityDataFetcher.class);
    SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(creditQualityFetcher, assetAllocationFetcher, responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    List<Notification> warnings = List.of(mock(Notification.class));
    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    List<PortfolioHolding> holdings = List.of(holding);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));
    when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());

    doCallRealMethod().when(service).getFixedIncomeCreditQuality(any(), anyList());
    service.getFixedIncomeCreditQuality(command, warnings);

    verify(assetAllocationFetcher).fetch(eq(holdings), any());
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_whenVerifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      SecurityDataFetcher<CreditQuality> creditQualityFetcher = mock(SecurityDataFetcher.class);
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
      CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
      FixedIncomeCreditQualityService service = mockService(creditQualityFetcher, assetAllocationFetcher,
          responseMapper);

      List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
      List<DataProvider> defaultProviders = List.of(DataProvider.MORNINGSTAR);
      PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
      when(command.getDataProviders()).thenReturn(providers);
      when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(providers, defaultProviders)).thenReturn(providers);

      doCallRealMethod().when(service).getFixedIncomeCreditQuality(any(), anyList());
      service.getFixedIncomeCreditQuality(command, List.of());

      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, defaultProviders));
    }
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_returnsFixedIncomeFromAllocations() {
    SecurityDataFetcher<CreditQuality> creditQualityFetcher = mock(SecurityDataFetcher.class);
    SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(creditQualityFetcher, assetAllocationFetcher, responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(AssetAllocationRegionType.EQUITY, TEN,
            AssetAllocationRegionType.FIXED_INCOME, HUNDRED))
        .build();
    when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of(holding, allocation));

    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(command.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));

    doCallRealMethod().when(service).getFixedIncomeValue(any());
    doCallRealMethod().when(service).getFixedIncomeCreditQuality(any(), anyList());

    Map<PortfolioHolding, BigDecimal> actual = service.getFixedIncomeCreditQuality(command, List.of());

    assertEquals(Map.of(holding, HUNDRED), actual);
  }

  @Test
  void shouldGetFixedIncomeValue_returnsFixedIncomeOrZero() {
    FixedIncomeCreditQualityService service = mock(FixedIncomeCreditQualityService.class);

    PortfolioHolding withFixed = mock(PortfolioHolding.class);
    PortfolioHolding withoutFixed = mock(PortfolioHolding.class);
    HoldingAssetAllocation withFixedAllocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(AssetAllocationRegionType.FIXED_INCOME, HUNDRED))
        .build();
    HoldingAssetAllocation withoutFixedAllocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(AssetAllocationRegionType.CASH, TEN))
        .build();

    doCallRealMethod().when(service).getFixedIncomeValue(any());
    Map<PortfolioHolding, BigDecimal> actual = Map.of(withFixed, withFixedAllocation, withoutFixed,
        withoutFixedAllocation).entrySet().stream().collect(toMap(Map.Entry::getKey, service::getFixedIncomeValue));

    assertEquals(HUNDRED, actual.get(withFixed));
    assertEquals(ZERO, actual.get(withoutFixed));
  }

  @Test
  void shouldCalculateSumProductRating_whenCheckResult() {
    FixedIncomeCreditQualityService service = mock(FixedIncomeCreditQualityService.class);

    PortfolioHolding h = mock(PortfolioHolding.class);
    PortfolioHolding h2 = new PortfolioHolding(null, FinancialInstrumentType.CASH, null);

    int creditQValue = 2;
    int fixedIncomeValue = 3;
    int weightValue = 10;
    Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQuality = Map.of(h, Map.of(AAA,
        BigDecimal.valueOf(creditQValue)));
    Map<PortfolioHolding, BigDecimal> fixedIncomeCreditQuality = Map.of(h, BigDecimal.valueOf(fixedIncomeValue));
    Map<PortfolioHolding, BigDecimal> weights = Map.of(h, BigDecimal.valueOf(weightValue), h2, BigDecimal.ONE);

    doCallRealMethod().when(service).calculateSumProductRating(any(), any(), any(), any());
    BigDecimal actual = service.calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, AAA);

    assertEquals(0, actual.compareTo(BigDecimal.valueOf(creditQValue * fixedIncomeValue * weightValue)));
  }

  @Test
  void shouldCalculateCreditQualityRatingTypes_whenVerifyCalculateInitialPortfolioWeight() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      FixedIncomeCreditQualityService service = mock(FixedIncomeCreditQualityService.class);
      List<PortfolioHolding> holdings = List.of(mock(PortfolioHolding.class));
      when(service.calculateSumProductRating(any(), any(), any(), any())).thenReturn(ZERO);

      doCallRealMethod().when(service).calculateCreditQualityRatingTypes(any(), any(), any());
      service.calculateCreditQualityRatingTypes(holdings, Map.of(), Map.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldToFixedIncomeCreditQuality_whenCheckResult() {
    FixedIncomeCreditQualityService service = mock(FixedIncomeCreditQualityService.class);

    Map<CreditQualityRatingType, BigDecimal> ratings = Map.of(
        AAA, BigDecimal.valueOf(100),
        AA, BigDecimal.valueOf(2),
        A, BigDecimal.valueOf(3),
        BBB, BigDecimal.valueOf(4),
        BB, BigDecimal.valueOf(5),
        B, BigDecimal.valueOf(60),
        BELOW_B, BigDecimal.valueOf(7),
        NOT_RATED, BigDecimal.valueOf(80));

    doCallRealMethod().when(service).toFixedIncomeCreditQuality(any());
    Map<FixedIncomeCreditQuality, BigDecimal> actual = service.toFixedIncomeCreditQuality(ratings);

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
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      FixedIncomeCreditQualityService service = mock(FixedIncomeCreditQualityService.class);
      Map<CreditQualityRatingType, BigDecimal> rescaled = Map.of(AAA, TEN);
      when(service.calculateCreditQualityRatingTypes(any(), any(), any())).thenReturn(rescaled);

      doCallRealMethod().when(service).calculate(any(), any(), any());
      service.calculate(List.of(mock(PortfolioHolding.class)), Map.of(), Map.of());

      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(rescaled));
    }
  }

  @Test
  void shouldCalculate_returnsToFixedIncomeCreditQualityResult() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      FixedIncomeCreditQualityService service = mock(FixedIncomeCreditQualityService.class);
      Map<CreditQualityRatingType, BigDecimal> rescaled = Map.of(AAA, TEN);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(rescaled);
      HashMap<FixedIncomeCreditQuality, BigDecimal> expected = new HashMap<>();
      when(service.toFixedIncomeCreditQuality(rescaled)).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any(), any());
      Map<FixedIncomeCreditQuality, BigDecimal> actual = service.calculate(
          List.of(mock(PortfolioHolding.class)), Map.of(), Map.of());

      assertSame(expected, actual);
    }
  }
}
