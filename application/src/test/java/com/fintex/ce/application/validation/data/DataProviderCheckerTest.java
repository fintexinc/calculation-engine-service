package com.fintex.ce.application.validation.data;

import com.fintex.ce.application.validation.DataProviderRequestHandlingValidator;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataProviderCheckerTest {

  DataProviderCheckerTest() {
  }

  @Test
  void check_verifyMethodCalls() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      final var sut = new DataProviderChecker();
      final var map = mock(Map.class);
      final var list = mock(List.class);
      when(map.values()).thenReturn(list);

      final var assetAllocationData = mock(AssetAllocationDataDTO.class);
      when(assetAllocationData.getBenchmarkIndexFdsResponse()).thenReturn(map);
      when(assetAllocationData.getEtfCanadaFdsResponse()).thenReturn(map);
      when(assetAllocationData.getEtfUsFdsResponse()).thenReturn(map);
      when(assetAllocationData.getMutualFundFdsResponse()).thenReturn(map);

      // ACT
      sut.check(list, assetAllocationData);

      // VERIFY
      verify(map, times(4)).values();

      mockedDataProviderRequestHandlingValidator.verify(() -> DataProviderRequestHandlingValidator
          .dataProviderCheckValidation(eq(list), eq(list), any()), Mockito.times(4));
    }
  }

  @Test
  void clearAssetAllocation_checkResult() {
    // SETUP
    final var sut = new DataProviderChecker();
    final HoldingAssetAllocation assetAllocation = mock(HoldingAssetAllocation.class);

    // ACT
    sut.clearAssetAllocation().apply(assetAllocation, null);

    // VERIFY
    verify(assetAllocation).setAllocations(argThat(Map::isEmpty));
  }

}