package com.fintex.ce.application.validation.data;

import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationData;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.Warning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.error.ErrorCode.MISSING_ASSET_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationDataValidatorTest {

  @Test
  void validate_verifyValidateNonStock() {
    // SETUP
    final var validator = mock(AssetAllocationDataValidator.class);

    final var req = mock(AssetAllocationData.class);
    final var warnings = mock(List.class);

    final var etfUs = mock(Map.class);
    final var etfCanada = mock(Map.class);
    final var mutualFund = mock(Map.class);
    final var benchmarkIndex = mock(Map.class);

    when(req.getEtfUsFdsResponse()).thenReturn(etfUs);
    when(req.getEtfCanadaFdsResponse()).thenReturn(etfCanada);
    when(req.getMutualFundFdsResponse()).thenReturn(mutualFund);
    when(req.getBenchmarkIndexFdsResponse()).thenReturn(benchmarkIndex);

    doNothing().when(validator).validateNonStock(anyMap(), anyList());

    doCallRealMethod().when(validator).validate(any(), anyList());
    // ACT
    validator.validate(req, warnings);

    // VERIFY
    verify(validator).validateNonStock(etfUs, warnings);
    verify(validator).validateNonStock(etfCanada, warnings);
    verify(validator).validateNonStock(mutualFund, warnings);
    verify(validator).validateNonStock(benchmarkIndex, warnings);
  }

  @Test
  void validateNonStock_verifyValidateForEachHolding() {
    // SETUP
    final var validator = mock(AssetAllocationDataValidator.class);

    final var rAssetAllocation1 = mock(HoldingAssetAllocation.class);
    final var rAssetAllocation1Map = mock(Map.class);
    when(rAssetAllocation1.getAllocations()).thenReturn(rAssetAllocation1Map);
    final var rAssetAllocation2 = mock(HoldingAssetAllocation.class);
    final var rAssetAllocation2Map = mock(Map.class);
    when(rAssetAllocation2.getAllocations()).thenReturn(rAssetAllocation2Map);
    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var holdings = new HashMap<PortfolioHolding, HoldingAssetAllocation>();
    holdings.put(holding1, rAssetAllocation1);
    holdings.put(holding2, rAssetAllocation2);

    final var warnings = mock(List.class);

    doCallRealMethod().when(validator).validateNonStock(anyMap(), anyList());
    // ACT
    validator.validateNonStock(holdings, warnings);

    // VERIFY
    holdings.forEach((holding, rAssetAllocation) -> verify(validator).validate(holding, rAssetAllocation.getAllocations(),
        warnings));
  }

  @Test
  void validate_assetAllocationsIsEmpty() {
    // SETUP
    final var validator = mock(AssetAllocationDataValidator.class);

    final var holding = mock(PortfolioHolding.class);
    final var assetAllocation = new HashMap<String, BigDecimal>();
    final var warnings = mock(List.class);

    doCallRealMethod().when(validator).validate(any(), anyMap(), anyList());
    // ACT
    validator.validate(holding, assetAllocation, warnings);

    // VERIFY
    verify(validator).validateWhenAssetAllocationIsEmpty(holding, warnings);
  }

  @Test
  void validate_assetAllocationRegionIsIncorrect() {
    // SETUP
    final var validator = mock(AssetAllocationDataValidator.class);

    final var holding = mock(PortfolioHolding.class);
    final var assetAllocation = new HashMap<String, BigDecimal>();
    assetAllocation.put("AssetAllocationRegionThatDoesn'tExists", BigDecimal.TEN);
    assetAllocation.put("AssetAllocationRegionThatDoesn'tExists Either", BigDecimal.ONE);
    final var warnings = new ArrayList<Warning>();

    doCallRealMethod().when(validator).validate(any(), anyMap(), anyList());
    // ACT
    validator.validate(holding, assetAllocation, warnings);

    // VERIFY
    assertEquals(2, warnings.size());
    warnings.forEach(warning -> assertEquals(UNKNOWN_TYPE_FROM_DATA_POINT.getCode(), warning.getCode()));
  }

  @Test
  void validateWhenAssetAllocationIsEmpty_addWarningToWarningsList() {
    // SETUP
    final var validator = mock(AssetAllocationDataValidator.class);

    final var holding = mock(PortfolioHolding.class);
    final var warnings = new ArrayList<Warning>();

    when(holding.getIdsString()).thenReturn("generateUserIdentifier");

    doCallRealMethod().when(validator).validateWhenAssetAllocationIsEmpty(any(), anyList());
    // ACT
    validator.validateWhenAssetAllocationIsEmpty(holding, warnings);

    // VERIFY
    assertEquals(1, warnings.size());
    assertEquals(MISSING_ASSET_ALLOCATION.getCode(), warnings.get(0).getCode());
  }

}