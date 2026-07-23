package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.CreditQualityResponseMapper;
import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.CreditQualityData;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.CreditQualityResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeCreditQualityServiceTest {

  private FixedIncomeCreditQualityService mockService(CreditQualityResponseMapper responseMapper) {
    return mock(FixedIncomeCreditQualityService.class, withSettings().useConstructor(responseMapper));
  }

  @Test
  void shouldRequireCreditQualityAndAssetAllocationAttributes_whenRequiredAttributesInvoked() {
    FixedIncomeCreditQualityService service = new FixedIncomeCreditQualityService(
        mock(CreditQualityResponseMapper.class));

    List<CompositeSecurityAttribute> requirements = service.requiredAttributes();

    assertThat(requirements).containsExactly(
        CompositeSecurityAttribute.CREDIT_QUALITY_RATINGS,
        CompositeSecurityAttribute.ASSET_ALLOCATION);
  }

  @Test
  void shouldPerform_whenVerifyAreAllValuesInMapEmpty() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
      FixedIncomeCreditQualityService service = mockService(responseMapper);

      PortfolioHolding holding = mock(PortfolioHolding.class);
      PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
      when(command.getHoldings()).thenReturn(List.of(holding));

      doCallRealMethod().when(service).perform(any(), any());
      service.perform(command, new CreditQualityData(Map.of(), Map.of()));

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(any()));
    }
  }

  @Test
  void shouldPerform_whenVerifyResponseMapperFromCalculatedValues() {
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of(AAA, ONE));
    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    CreditQualityData data = new CreditQualityData(Map.of(holding, rawCq), Map.of());

    Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    when(service.calculate(any(), any(), any())).thenReturn(map);

    doCallRealMethod().when(service).perform(any(), any());
    doCallRealMethod().when(service).getFixedIncomeCreditQuality(any(), any());
    service.perform(command, data);

    verify(responseMapper).fromCalculatedValues(Mockito.eq(map), anyList());
  }

  @Test
  void shouldPerform_whenCheckResult() {
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    CreditQuality rawCq = new CreditQuality();
    rawCq.setRatings(Map.of(AAA, ONE));
    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    CreditQualityData data = new CreditQualityData(Map.of(holding, rawCq), Map.of());

    Map<FixedIncomeCreditQuality, BigDecimal> map = Map.of(HIGH_YIELD, ONE);
    CreditQualityResult expected = CreditQualityResult.builder()
        .creditQuality(map)
        .warnings(List.of())
        .build();
    when(responseMapper.fromCalculatedValues(any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(service).perform(any(), any());
    doCallRealMethod().when(service).getFixedIncomeCreditQuality(any(), any());
    CreditQualityResult actual = service.perform(command, data);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetFixedIncomeCreditQuality_returnsFixedIncomeFromAllocations() {
    CreditQualityResponseMapper responseMapper = mock(CreditQualityResponseMapper.class);
    FixedIncomeCreditQualityService service = mockService(responseMapper);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(AssetAllocationRegionType.US_EQUITIES, TEN,
            AssetAllocationRegionType.FIXED_INCOME, HUNDRED))
        .build();
    CreditQualityData data = new CreditQualityData(Map.of(), Map.of(holding, allocation));

    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));

    doCallRealMethod().when(service).getFixedIncomeValue(any());
    doCallRealMethod().when(service).getFixedIncomeCreditQuality(any(), any());

    Map<PortfolioHolding, BigDecimal> actual = service.getFixedIncomeCreditQuality(command, data);

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

      mockedCalculationUtils.verify(() -> CalculationUtils.reScale(rescaled));
    }
  }

  @Test
  void shouldCalculate_returnsToFixedIncomeCreditQualityResult() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      FixedIncomeCreditQualityService service = mock(FixedIncomeCreditQualityService.class);
      Map<CreditQualityRatingType, BigDecimal> rescaled = Map.of(AAA, TEN);
      mockedCalculationUtils.when(() -> CalculationUtils.reScale(any())).thenReturn(rescaled);
      HashMap<FixedIncomeCreditQuality, BigDecimal> expected = new HashMap<>();
      when(service.toFixedIncomeCreditQuality(rescaled)).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any(), any());
      Map<FixedIncomeCreditQuality, BigDecimal> actual = service.calculate(
          List.of(mock(PortfolioHolding.class)), Map.of(), Map.of());

      assertSame(expected, actual);
    }
  }
}
