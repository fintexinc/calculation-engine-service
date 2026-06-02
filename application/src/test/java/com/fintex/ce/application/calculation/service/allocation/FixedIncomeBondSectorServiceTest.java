package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class FixedIncomeBondSectorServiceTest {

  private FixedIncomeBondSectorService mockService(
      SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeFetcher,
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher) {
    return mock(FixedIncomeBondSectorService.class, withSettings()
        .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, DEFAULT_DATA_PROPERTIES));
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesZerosInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
      FixedIncomeBondSectorService service = mockService(fixedIncomeFetcher, assetAllocationFetcher);

      Map exposures = mock(Map.class);
      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<FixedIncomeSecuritiesAllocationType>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
      FixedIncomeBondSectorService service = mockService(fixedIncomeFetcher, assetAllocationFetcher);

      FixedIncomeSectorResult expected = FixedIncomeSectorResult.builder()
          .fixedIncomeSector(FixedIncomeBondSectorService.DEFAULT_MAP)
          .warnings(List.of())
          .build();
      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

      doCallRealMethod().when(service).calculate(any(), any());
      FixedIncomeSectorResult actual = service.calculate(new ExposureDataHolder<>(Map.of(), List.of()), List.of());

      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldFetch_whenCheckResult() {
    SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeFetcher = mock(SecurityDataFetcher.class);
    SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
    FixedIncomeBondSectorService service = mockService(fixedIncomeFetcher, assetAllocationFetcher);

    PortfolioHolding holding = mock(PortfolioHolding.class);
    FixedIncomeBondSecurities rawData = new FixedIncomeBondSecurities();
    rawData.setFixedIncomeBondSectors(Map.of(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, TEN));
    when(fixedIncomeFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawData));

    doCallRealMethod().when(service).fetchExposures(any());
    ExposureDataHolder<FixedIncomeSecuritiesAllocationType> result = service.fetchExposures(
        mock(PortfolioHoldingsCommand.class));

    assertEquals(1, result.allocations().size());
  }

  @Test
  void shouldCalculate_readsFixedIncomeAndCashFromAssetAllocation() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
      FixedIncomeBondSectorService service = mockService(fixedIncomeFetcher, assetAllocationFetcher);

      PortfolioHolding fundSeriesHolding = mock(PortfolioHolding.class);
      HoldingAssetAllocation allocation = HoldingAssetAllocation.builder()
          .allocations(Map.of(
              AssetAllocationRegionType.FIXED_INCOME, ONE,
              AssetAllocationRegionType.CASH, ONE))
          .build();
      when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of(fundSeriesHolding, allocation));

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(Map.of(), List.of()), List.of(fundSeriesHolding));

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(any()));
    }
  }
}
