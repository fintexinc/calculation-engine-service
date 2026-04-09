package com.fintex.ce.application.validation.data;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.application.validation.DataProviderRequestHandlingValidator;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.sm.model.DataProvider;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataProviderCheckerTest {

  @Test
  void check_verifyMethodCalls() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      final var defaults = List.of(DataProvider.MORNINGSTAR);
      final var defaultDataProperties = new DefaultDataProperties(defaults);
      final var sut = new DataProviderChecker(defaultDataProperties);
      final var map = mock(Map.class);
      final var list = mock(List.class);
      when(map.values()).thenReturn(list);

      final var assetAllocationData = mock(AssetAllocationDataDTO.class);
      when(assetAllocationData.getBenchmarkIndexFdsResponse()).thenReturn(map);
      when(assetAllocationData.getEtfCanadaFdsResponse()).thenReturn(map);
      when(assetAllocationData.getEtfUsFdsResponse()).thenReturn(map);
      when(assetAllocationData.getMutualFundFdsResponse()).thenReturn(map);
      when(assetAllocationData.getCanadaPooledFundFdsResponse()).thenReturn(map);
      when(assetAllocationData.getCanadaHedgeFundsFdsResponse()).thenReturn(map);
      when(assetAllocationData.getUsFundsFdsResponse()).thenReturn(map);

      // ACT
      sut.check(list, assetAllocationData);

      // VERIFY
      verify(map, times(7)).values();

      mockedDataProviderRequestHandlingValidator.verify(() -> DataProviderRequestHandlingValidator
          .dataProviderCheckValidation(eq(list), eq(list), any(), eq(defaults), any()), Mockito.times(7));
    }
  }

  @Test
  void clearAssetAllocation_checkResult() {
    // SETUP
    final var sut = new DataProviderChecker(new DefaultDataProperties(List.of()));
    final HoldingAssetAllocation assetAllocation = mock(HoldingAssetAllocation.class);

    // ACT
    sut.clearAssetAllocation().apply(assetAllocation, null);

    // VERIFY
    verify(assetAllocation).setAllocations(argThat(Map::isEmpty));
  }

}
